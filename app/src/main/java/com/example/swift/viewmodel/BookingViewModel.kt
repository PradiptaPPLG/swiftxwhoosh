package com.example.swift.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swift.models.*
import com.example.swift.models.OccupiedSeatsResponse
import com.example.swift.utils.EmailSender
import com.example.swift.api.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class BookingViewModel : ViewModel() {

    private val apiService = RetrofitClient.instance

    var origin by mutableStateOf(Station.HALIM)
    var destination by mutableStateOf(Station.TEGALLUAR)
    val ticketCount: Int get() = if (passengers.isEmpty()) 1 else passengers.size
    var departureDate by mutableStateOf("")
    var departureDateMillis by mutableStateOf<Long?>(null)
    var selectedTime by mutableStateOf<String?>(null)
    var selectedCoach by mutableStateOf<CoachClass?>(null)
    
    // Seat Booking State
    val passengers = mutableStateListOf<PassengerDetail>()
    var coaches by mutableStateOf<List<Coach>>(emptyList())
    var selectedCoachId by mutableStateOf("01")
    var selectedSeats by mutableStateOf<List<String>>(emptyList())
    var isLoadingSeats by mutableStateOf(false)
    var isProcessingBooking by mutableStateOf(false)
    var bookingErrorMessage by mutableStateOf<String?>(null)

    var bookingComplete by mutableStateOf(false)
    var ticketEmailSent by mutableStateOf(false)
    var currentBooking by mutableStateOf<BookingData?>(null)

    // Saved Passengers
    var savedPassengers by mutableStateOf<List<PassengerDetail>>(emptyList())
    var isLoadingSavedPassengers by mutableStateOf(false)

    // Booking action status — prevents conflicting actions
    enum class BookingStatus { ACTIVE, CANCELLED, REFUNDED, RESCHEDULED }
    var bookingStatus by mutableStateOf(BookingStatus.ACTIVE)
    
    fun resetBookingProcess() {
        bookingComplete = false
        ticketEmailSent = false
        bookingErrorMessage = null
        passengers.clear()
        selectedSeats = emptyList()
        bookingStatus = BookingStatus.ACTIVE
        // Note: we don't clear currentBooking here because it's used to show the last ticket
    }

    // Real API State
    var schedules by mutableStateOf<List<com.example.swift.api.TrainSchedule>>(emptyList())
    var isLoadingSchedules by mutableStateOf(false)

    fun fetchSchedules() {
        resetBookingProcess()
        isLoadingSchedules = true
        apiService.getSchedules(origin.displayName, destination.displayName)
            .enqueue(object : retrofit2.Callback<List<com.example.swift.api.TrainSchedule>> {
                override fun onResponse(
                    call: retrofit2.Call<List<com.example.swift.api.TrainSchedule>>,
                    response: retrofit2.Response<List<com.example.swift.api.TrainSchedule>>
                ) {
                    isLoadingSchedules = false
                    if (response.isSuccessful) {
                        schedules = response.body() ?: emptyList()
                    }
                }

                override fun onFailure(call: retrofit2.Call<List<com.example.swift.api.TrainSchedule>>, t: Throwable) {
                    isLoadingSchedules = false
                    bookingErrorMessage = t.message
                }
            })
    }

    fun fetchSavedPassengers(userId: Int) {
        isLoadingSavedPassengers = true
        viewModelScope.launch {
            try {
                val responseBody = withContext(Dispatchers.IO) {
                    apiService.getSavedPassengers(userId).execute().body()
                }
                responseBody?.let { resp ->
                    if (resp.status == "success") {
                        val mapped = resp.passengers.map { p: com.example.swift.models.PassengerDTO ->
                            PassengerDetail(
                                name = p.name,
                                identityType = if (p.identityType == "Passport") IdentityType.PASSPORT else IdentityType.ID_CARD,
                                identityNumber = p.identityNumber,
                                gender = if (p.gender.equals("Female", ignoreCase = true)) Gender.FEMALE else Gender.MALE,
                                email = "" // Will be handled by fallback in processFinalBooking
                            )
                        }
                        withContext(Dispatchers.Main) {
                            savedPassengers = mapped
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSavedPassengers = false
            }
        }
    }

    val travelDuration: Int get() = TravelTime.getDuration(origin, destination)

    var selectedArrivalTime: String = ""

    val arrivalTime: String
        get() = if (selectedArrivalTime.isNotEmpty()) selectedArrivalTime 
                else selectedTime?.let { DepartureSchedule.calculateArrival(it, travelDuration) } ?: ""

    val pricePerTicket: Int
        get() = selectedCoach?.let {
            TicketPricing.getPrice(it, ticketCount)
        } ?: 0

    val totalPrice: Int get() {
        val basePrice = pricePerTicket
        return passengers.sumOf { p ->
            if (p.passengerType == PassengerType.CHILD) {
                maxOf(0, basePrice - 50_000) 
            } else {
                basePrice
            }
        }
    }

    fun updatePassengerData(index: Int, data: PassengerDetail) {
        if (index in passengers.indices) {
            passengers[index] = data
        }
    }

    fun swapStations() {
        val temp = origin
        origin = destination
        destination = temp
    }

    fun isValidRoute(): Boolean = origin != destination

    fun setDate(millis: Long) {
        departureDateMillis = millis
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("id", "ID"))
        departureDate = sdf.format(Date(millis))
    }

    fun addPassenger(passenger: PassengerDetail) {
        if (passengers.size < 15) {
            passengers.add(passenger)
        }
    }

    fun removePassenger(index: Int) {
        if (index in passengers.indices) {
            passengers.removeAt(index)
            if (selectedSeats.size > ticketCount) {
                selectedSeats = selectedSeats.take(ticketCount)
            }
        }
    }

    fun toggleSeatSelection(seatId: String, scheduleId: Int) {
        val current = selectedSeats.toMutableList()
        if (current.contains(seatId)) {
            current.remove(seatId)
            // Unlock on server
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    apiService.lockSeat(mapOf("schedule_id" to scheduleId, "seat_id" to seatId, "action" to "unlock")).execute()
                } catch (e: Exception) { e.printStackTrace() }
            }
        } else {
            if (current.size < ticketCount) {
                current.add(seatId)
                // Lock on server
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        apiService.lockSeat(mapOf("schedule_id" to scheduleId, "seat_id" to seatId, "action" to "lock")).execute()
                    } catch (e: Exception) { e.printStackTrace() }
                }
            } else {
                val oldSeat = current.removeAt(0)
                // Unlock old seat
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        apiService.lockSeat(mapOf("schedule_id" to scheduleId, "seat_id" to oldSeat, "action" to "unlock")).execute()
                    } catch (e: Exception) { e.printStackTrace() }
                }
                current.add(seatId)
                // Lock new seat
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        apiService.lockSeat(mapOf("schedule_id" to scheduleId, "seat_id" to seatId, "action" to "lock")).execute()
                    } catch (e: Exception) { e.printStackTrace() }
                }
            }
        }
        selectedSeats = current
    }

    fun togglePassengerSelection(passenger: PassengerDetail, userId: Int) {
        val existing = passengers.find { it.identityNumber == passenger.identityNumber }
        if (existing != null) {
            passengers.remove(existing)
        } else {
            if (passengers.size < 15) {
                passengers.add(passenger)
            }
        }
    }

    fun prepareSeatingAndFetch(scheduleId: Int) {
        isLoadingSeats = true
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getSeats(scheduleId, selectedCoachId).execute().body()
                }
                val occupied = response?.occupiedSeats ?: emptyList<String>()
                
                val seatLetters = listOf("A", "B", "C", "D", "F")
                val mockCoaches = (1..8).map { coachNum ->
                    val coachId = String.format("%02d", coachNum)
                    val seats = (1..13).flatMap { row ->
                        seatLetters.map { letter ->
                            val seatId = "$row$letter"
                            // Check if this specific seat in this specific coach is occupied
                            val isOccupied = occupied.contains("$coachId-$seatId")
                            Seat(seatId, isAvailable = !isOccupied)
                        }
                    }
                    Coach(coachId, seats)
                }
                coaches = mockCoaches
                if (selectedCoachId.isEmpty() && coaches.isNotEmpty()) {
                    selectedCoachId = coaches.first().id
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingSeats = false
            }
        }
    }

    private fun generateBookingCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "SWF-" + (1..8).map { chars.random() }.joinToString("")
    }

    fun processFinalBooking(userIdParam: Int, accountEmailFallback: String = "") {
        if (userIdParam <= 0) {
            bookingErrorMessage = "Session error: User not logged in. Please logout and login again."
            return
        }
        val userId = userIdParam
        val isPassengersValid = passengers.all { it.name.isNotBlank() && it.identityNumber.isNotBlank() }
        if (passengers.isEmpty() || passengers.size != ticketCount || !isPassengersValid) {
            bookingErrorMessage = "Please complete all passenger details."
            return
        }

        isProcessingBooking = true
        bookingErrorMessage = null

        viewModelScope.launch {
            try {
                // 1. Save all passengers to database
                withContext(Dispatchers.IO) {
                    passengers.forEach { p ->
                        val resp = apiService.savePassenger(
                            userId = userId,
                            name = p.name,
                            identityType = p.identityType.displayName,
                            identityNumber = p.identityNumber,
                            gender = p.gender.displayName,
                            dateOfBirth = p.dateOfBirth,
                            phone = p.whatsapp,
                            email = p.email
                        ).execute()
                        
                        if (!resp.isSuccessful) {
                            println("DEBUG: Save passenger failed: ${resp.errorBody()?.string()}")
                        }
                    }
                }

                // 2. Book the seats officially (Set to paid)
                val currentScheduleId = schedules.find { it.departureTime == selectedTime }?.scheduleId?.toIntOrNull() ?: 17
                
                val seatRequest = BookSeatRequest(
                    userId = userId,
                    scheduleId = currentScheduleId, 
                    coachId = selectedCoachId,
                    seats = selectedSeats,
                    totalPrice = totalPrice
                )

                val bookingResponse = withContext(Dispatchers.IO) {
                    apiService.bookSeats(seatRequest).execute().body()
                }

                if (bookingResponse?.get("status") == "success") {
                    val code = bookingResponse["code"]?.toString() ?: bookingResponse["booking_code"]?.toString() ?: generateBookingCode()
                    var targetEmail = passengers.firstOrNull()?.email ?: ""
                    if (targetEmail.isBlank()) {
                        Log.d("BookingViewModel", "Passenger email empty, falling back to account email: $accountEmailFallback")
                        targetEmail = accountEmailFallback
                    }
                    
                    val completeBk = BookingData(
                        bookingCode = code,
                        passengerName = passengers.firstOrNull()?.name ?: "Passenger",
                        passengerEmail = targetEmail,
                        passengers = passengers.toList(),
                        origin = origin,
                        destination = destination,
                        ticketCount = ticketCount,
                        departureDate = departureDate,
                        departureTime = selectedTime ?: "00:00",
                        arrivalTime = arrivalTime,
                        travelDuration = travelDuration,
                        coachClass = selectedCoach ?: CoachClass.PREMIUM_ECONOMY,
                        selectedCoachId = selectedCoachId,
                        selectedSeats = selectedSeats,
                        pricePerTicket = pricePerTicket,
                        totalPrice = totalPrice
                    )
                    
                    withContext(Dispatchers.Main) {
                        currentBooking = completeBk
                        bookingComplete = true
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        bookingErrorMessage = bookingResponse?.get("message")?.toString() ?: "Failed to complete booking."
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    bookingErrorMessage = "Booking failed: ${e.message}"
                }
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    isProcessingBooking = false
                }
            }
        }
    }

    fun resetBooking() {
        origin = Station.HALIM
        destination = Station.TEGALLUAR
        departureDate = ""
        departureDateMillis = null
        selectedTime = null
        selectedCoach = null
        passengers.clear()
        passengers.add(PassengerDetail())
        selectedCoachId = "01"
        selectedSeats = emptyList()
        bookingErrorMessage = null
        bookingComplete = false
        currentBooking = null
        bookingStatus = BookingStatus.ACTIVE
    }

    fun formatCurrency(amount: Int): String {
        val formatted = String.format("%,d", amount).replace(",", ".")
        return "Rp $formatted"
    }

    fun rescheduleBooking(bookingCode: String, newDate: String) {
        viewModelScope.launch {
            try {
                val request = mapOf(
                    "booking_code" to bookingCode,
                    "new_date" to newDate
                )
                withContext(Dispatchers.IO) {
                    apiService.rescheduleBooking(request).execute()
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Reschedule API error: ${e.message}")
            }
        }
    }

    fun sendFinalTicketEmail() {
        if (ticketEmailSent) return
        val booking = currentBooking ?: return
        val formattedPrice = formatCurrency(booking.totalPrice)
        ticketEmailSent = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("BookingViewModel", "Sending Final Ticket email to: ${booking.passengerEmail}")
                val result = EmailSender.sendTicketEmail(booking, formattedPrice)
                Log.d("BookingViewModel", "Final Ticket email result: $result")
                if (result) {
                     // We don't have snackbarHostState here, so we rely on the one in OrderDetailsScreen
                     // or we can just log it.
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error sending final ticket email: ${e.message}")
            }
        }
    }
}
