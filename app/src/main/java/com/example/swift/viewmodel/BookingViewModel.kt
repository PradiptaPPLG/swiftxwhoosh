package com.example.swift.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swift.api.ApiService
import com.example.swift.api.RetrofitClient
import com.example.swift.models.*
import com.example.swift.utils.EmailSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call

class BookingViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance

    // Search Params
    var origin by mutableStateOf(Station.HALIM)
    var destination by mutableStateOf(Station.TEGALLUAR)
    var departureDate by mutableStateOf("")
    var departureDateMillis by mutableStateOf<Long?>(null)
    
    // Selection
    var selectedTime by mutableStateOf<String?>(null)
    var selectedArrivalTime by mutableStateOf<String?>(null)
    var selectedCoachClass by mutableStateOf(CoachClass.PREMIUM_ECONOMY)
    var selectedCoachId by mutableStateOf("01")
    var passengers = mutableStateListOf<PassengerDetail>(PassengerDetail())
    var selectedSeats by mutableStateOf<List<String>>(emptyList())
    
    // Seat Selection State
    var coaches = mutableStateListOf<Coach>()
    var isLoadingSeats by mutableStateOf(false)
    
    // Booking State
    var isProcessingBooking by mutableStateOf(false)
    var bookingComplete by mutableStateOf(false)
    var currentBooking by mutableStateOf<com.example.swift.models.BookingData?>(null)
    var bookingErrorMessage by mutableStateOf<String?>(null)
    var ticketEmailSent by mutableStateOf(false)
    
    // Payment State
    var isWaitingForPayment by mutableStateOf(false)
    var paymentTimerText by mutableStateOf("15:00")
    var paymentTimeout by mutableStateOf(false)
    var pendingBookingId by mutableStateOf<Int?>(null)
    private var timerJob: Job? = null

    // Saved Passengers
    var savedPassengers by mutableStateOf<List<PassengerDetail>>(emptyList())
    var isLoadingSavedPassengers by mutableStateOf(false)

    // User Bookings
    var userBookings by mutableStateOf<List<UserBooking>>(emptyList())
    var isLoadingUserBookings by mutableStateOf(false)
    var editingPassenger by mutableStateOf<PassengerDetail?>(null)

    // Computed Properties
    val ticketCount: Int get() = passengers.size
    val travelDuration: Int get() = TravelTime.getDuration(origin, destination)
    val arrivalTime: String get() = selectedArrivalTime ?: DepartureSchedule.calculateArrival(selectedTime ?: "08:00", travelDuration)
    val pricePerTicket: Int get() = TicketPricing.getPrice(selectedCoachClass, passengers.size)
    val totalPrice: Int get() = pricePerTicket * passengers.size
    fun isValidRoute(): Boolean = origin != destination

    // Booking Status
    enum class BookingStatus { ACTIVE, CANCELLED, REFUNDED, RESCHEDULED, PENDING }
    var bookingStatus by mutableStateOf(BookingStatus.ACTIVE)
    
    // Reschedule State
    var isRescheduling by mutableStateOf(false)
    var bookingToReschedule by mutableStateOf<com.example.swift.models.BookingData?>(null)

    // Real API State
    var schedules by mutableStateOf<List<com.example.swift.api.TrainSchedule>>(emptyList())
    var isLoadingSchedules by mutableStateOf(false)

    fun fetchSchedules() {
        isLoadingSchedules = true
        apiService.getSchedules(origin.displayName, destination.displayName, departureDate).enqueue(object : retrofit2.Callback<List<com.example.swift.api.TrainSchedule>> {
            override fun onResponse(call: Call<List<com.example.swift.api.TrainSchedule>>, response: retrofit2.Response<List<com.example.swift.api.TrainSchedule>>) {
                isLoadingSchedules = false
                if (response.isSuccessful) {
                    schedules = response.body() ?: emptyList()
                } else {
                    schedules = emptyList() // Clear on failure to show empty state
                }
            }

            override fun onFailure(call: Call<List<com.example.swift.api.TrainSchedule>>, t: Throwable) {
                isLoadingSchedules = false
                schedules = emptyList() // Clear on network failure
            }
        })
    }

    fun swapStations() {
        val temp = origin
        origin = destination
        destination = temp
    }

    fun setDate(millis: Long) {
        departureDateMillis = millis
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        departureDate = sdf.format(java.util.Date(millis))
    }

    fun changeDateByDays(days: Int) {
        val currentMillis = departureDateMillis ?: System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = currentMillis
        calendar.add(java.util.Calendar.DAY_OF_YEAR, days)
        setDate(calendar.timeInMillis)
    }

    fun prepareSeatingAndFetch(scheduleId: Int) {
        isLoadingSeats = true
        viewModelScope.launch {
            try {
                // Fetch occupied seats from real API
                val response = withContext(Dispatchers.IO) {
                    apiService.getSeats(scheduleId, selectedCoachId).execute().body()
                }
                val occupied = response?.occupiedSeats ?: emptyList()
                
                delay(800)
                val newCoaches = mutableListOf<Coach>()
                
                // Define which coaches belong to which class
                val coachRange = when(selectedCoachClass) {
                    CoachClass.FIRST -> 1..1
                    CoachClass.BUSINESS -> 2..2
                    CoachClass.PREMIUM_ECONOMY -> 3..8
                }

                // Ensure selectedCoachId is valid for the selected class
                if (!coachRange.contains(selectedCoachId.toIntOrNull() ?: 0)) {
                    selectedCoachId = String.format("%02d", coachRange.first)
                }

                for (i in coachRange) {
                    val coachNum = String.format("%02d", i)
                    val seats = mutableListOf<Seat>()
                    
                    // Layout configuration per class
                    val (rowCount, columns) = when(selectedCoachClass) {
                        CoachClass.FIRST -> 7 to listOf("A", "C", "F") // 2-1 Layout
                        CoachClass.BUSINESS -> 10 to listOf("A", "C", "D", "F") // 2-2 Layout
                        CoachClass.PREMIUM_ECONOMY -> 15 to listOf("A", "B", "C", "D", "F") // 3-2 Layout
                    }

                    for (row in 1..rowCount) {
                        for (col in columns) {
                            val seatId = "$row$col"
                            val isAvailable = !occupied.contains(seatId)
                            seats.add(Seat(seatId, isAvailable))
                        }
                    }
                    newCoaches.add(Coach(coachNum, seats))
                }
                coaches.clear()
                coaches.addAll(newCoaches)
                isLoadingSeats = false
            } catch (e: Exception) {
                isLoadingSeats = false
            }
        }
    }

    fun toggleSeatSelection(seatId: String, scheduleId: Int) {
        val current = selectedSeats.toMutableList()
        if (current.contains(seatId)) {
            current.remove(seatId)
        } else {
            if (current.size < passengers.size) {
                current.add(seatId)
            }
        }
        selectedSeats = current
    }

    fun fetchUserBookings(userId: Int) {
        isLoadingUserBookings = true
        apiService.getUserBookings(userId).enqueue(object : retrofit2.Callback<UserBookingsResponse> {
            override fun onResponse(call: Call<UserBookingsResponse>, response: retrofit2.Response<UserBookingsResponse>) {
                isLoadingUserBookings = false
                if (response.isSuccessful) {
                    userBookings = response.body()?.bookings ?: emptyList()
                }
            }
            override fun onFailure(call: Call<UserBookingsResponse>, t: Throwable) {
                isLoadingUserBookings = false
            }
        })
    }

    fun addPassenger(detail: PassengerDetail) {
        if (passengers.size < 5) {
            passengers.add(detail)
        }
    }

    fun removePassenger(index: Int) {
        if (passengers.size > 1) {
            passengers.removeAt(index)
        }
    }

    fun togglePassengerSelection(savedPassenger: PassengerDetail, userId: Int?) {
        val index = passengers.indexOfFirst { it.identityNumber == savedPassenger.identityNumber }
        if (index != -1) {
            if (passengers.size > 1) {
                passengers.removeAt(index)
            }
        } else {
            if (passengers.size < 5) {
                passengers.add(savedPassenger)
            }
        }
    }

    fun resetBookingProcess() {
        bookingComplete = false
        ticketEmailSent = false
        bookingErrorMessage = null
        passengers.clear()
        passengers.add(PassengerDetail())
        selectedSeats = emptyList()
        bookingStatus = BookingStatus.ACTIVE
        isRescheduling = false
        bookingToReschedule = null
    }

    fun resetBooking() {
        resetBookingProcess()
    }

    fun formatCurrency(amount: Int): String {
        val formatted = String.format("%,d", amount).replace(",", ".")
        return "Rp $formatted"
    }

    fun processFinalBooking(userId: Int, accountEmail: String) {
        isProcessingBooking = true
        viewModelScope.launch {
            try {
                val bookingRequest = mapOf(
                    "user_id" to userId,
                    "schedule_id" to (schedules.find { it.departureTime.contains(selectedTime ?: "") }?.scheduleId?.toIntOrNull() ?: 1),
                    "coach_id" to selectedCoachId,
                    "seats" to selectedSeats.toList(),
                    "passenger_names" to passengers.map { it.name },
                    "seat_class" to selectedCoachClass.name,
                    "total_price" to totalPrice
                )
                
                val response = withContext(Dispatchers.IO) {
                    apiService.createBooking(bookingRequest).execute().body()
                }

                withContext(Dispatchers.Main) {
                    if (response?.get("status") == "success") {
                        val bId = (response["booking_id"] as? Double)?.toInt() ?: 0
                        pendingBookingId = bId
                        currentBooking = BookingData(
                            bookingId = bId,
                            bookingCode = response["booking_code"] as? String ?: "SWIFT-PAY",
                            passengerName = passengers.firstOrNull()?.name ?: "User",
                            passengerEmail = accountEmail,
                            totalPrice = totalPrice,
                            origin = origin,
                            destination = destination,
                            departureDate = departureDate,
                            departureTime = selectedTime ?: "08:00",
                            arrivalTime = arrivalTime,
                            travelDuration = travelDuration,
                            ticketCount = passengers.size,
                            selectedCoachId = selectedCoachId,
                            selectedSeats = selectedSeats.toList(),
                            coachClass = selectedCoachClass
                        )
                        isWaitingForPayment = true
                        startPaymentTimer()
                    } else {
                        bookingErrorMessage = response?.get("message") as? String ?: "Booking failed"
                    }
                    isProcessingBooking = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    bookingErrorMessage = "Error: ${e.message}"
                    isProcessingBooking = false
                }
            }
        }
    }

    fun confirmBookingPayment(onComplete: (Boolean) -> Unit) {
        val bId = pendingBookingId ?: return
        isProcessingBooking = true
        viewModelScope.launch {
            try {
                delay(1500)
                val response = withContext(Dispatchers.IO) {
                    apiService.confirmPayment(mapOf("booking_id" to bId))
                }
                withContext(Dispatchers.Main) {
                    isProcessingBooking = false
                    if (response["status"] == "success") {
                        timerJob?.cancel()
                        isWaitingForPayment = false
                        bookingComplete = true
                        sendFinalTicketEmail()
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                isProcessingBooking = false
                onComplete(false)
            }
        }
    }

    fun refundBooking(bookingId: Int, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                isProcessingBooking = true
                val response = withContext(Dispatchers.IO) {
                    apiService.refundBooking(mapOf("booking_id" to bookingId))
                }
                withContext(Dispatchers.Main) {
                    isProcessingBooking = false
                    if (response["status"] == "success") {
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                isProcessingBooking = false
                onComplete(false)
            }
        }
    }

    fun rescheduleBooking(newScheduleId: Int, onComplete: (Boolean) -> Unit) {
        val bId = bookingToReschedule?.bookingId ?: return
        viewModelScope.launch {
            try {
                isProcessingBooking = true
                val request = mapOf(
                    "booking_id" to bId,
                    "new_schedule_id" to newScheduleId,
                    "seats" to selectedSeats.toList(),
                    "coach_id" to selectedCoachId
                )
                val response = withContext(Dispatchers.IO) {
                    apiService.rescheduleBooking(request)
                }
                withContext(Dispatchers.Main) {
                    isProcessingBooking = false
                    if (response["status"] == "success") {
                        isRescheduling = false
                        bookingToReschedule = null
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                isProcessingBooking = false
                onComplete(false)
            }
        }
    }

    fun fetchSavedPassengers(userId: Int) {
        isLoadingSavedPassengers = true
        apiService.getSavedPassengers(userId).enqueue(object : retrofit2.Callback<SavedPassengersResponse> {
            override fun onResponse(call: Call<SavedPassengersResponse>, response: retrofit2.Response<SavedPassengersResponse>) {
                isLoadingSavedPassengers = false
                if (response.isSuccessful) {
                    savedPassengers = response.body()?.passengers?.map { dto ->
                        PassengerDetail(
                            id = dto.id,
                            name = dto.name,
                            gender = if (dto.gender == "Male") Gender.MALE else Gender.FEMALE,
                            identityType = if (dto.identityType == "Passport") IdentityType.PASSPORT else IdentityType.ID_CARD,
                            identityNumber = dto.identityNumber
                        )
                    } ?: emptyList()
                }
            }
            override fun onFailure(call: Call<SavedPassengersResponse>, t: Throwable) {
                isLoadingSavedPassengers = false
            }
        })
    }

    fun sendFinalTicketEmail() {
        if (ticketEmailSent) return
        val booking = currentBooking ?: return
        val formattedPrice = formatCurrency(booking.totalPrice)
        ticketEmailSent = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                EmailSender.sendTicketEmail(booking, formattedPrice)
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error sending email: ${e.message}")
            }
        }
    }

    fun updateSavedPassenger(passenger: PassengerDetail, onComplete: (Boolean) -> Unit) {
        val body = mapOf(
            "id" to passenger.id,
            "name" to passenger.name,
            "identity_type" to passenger.identityType.displayName,
            "identity_number" to passenger.identityNumber,
            "gender" to passenger.gender.displayName
        )
        apiService.updateSavedPassenger(body).enqueue(object : retrofit2.Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: retrofit2.Response<Map<String, Any>>) {
                if (response.isSuccessful && response.body()?.get("status") == "success") {
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            }
            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                onComplete(false)
            }
        })
    }

    fun startPaymentTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var secondsLeft = 15 * 60
            while (secondsLeft > 0) {
                val mins = secondsLeft / 60
                val secs = secondsLeft % 60
                paymentTimerText = String.format("%02d:%02d", mins, secs)
                kotlinx.coroutines.delay(1000)
                secondsLeft--
            }
            paymentTimeout = true
        }
    }

    fun cancelReschedule() {
        isRescheduling = false
        bookingToReschedule = null
    }
}
