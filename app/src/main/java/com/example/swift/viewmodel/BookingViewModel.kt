package com.example.swift.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swift.data.FirestoreRepository
import com.example.swift.models.*
import com.example.swift.utils.EmailSender
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BookingViewModel : ViewModel() {
    private val firestoreRepo = FirestoreRepository()

    var origin by mutableStateOf(Station.HALIM)
    var destination by mutableStateOf(Station.TEGALLUAR)
    val ticketCount: Int get() = if (passengers.isEmpty()) 1 else passengers.size
    var departureDate by mutableStateOf("")
    var departureDateMillis by mutableStateOf<Long?>(null)
    var selectedTime by mutableStateOf<String?>(null)
    var selectedCoach by mutableStateOf<CoachClass?>(null)
    
    // Seat Booking State
    val passengers = androidx.compose.runtime.mutableStateListOf(PassengerDetail())
    var coaches by mutableStateOf<List<Coach>>(emptyList())
    var selectedCoachId by mutableStateOf("01")
    var selectedSeats by mutableStateOf<List<String>>(emptyList())
    var isLoadingSeats by mutableStateOf(false)
    var isProcessingBooking by mutableStateOf(false)
    var bookingErrorMessage by mutableStateOf<String?>(null)

    var bookingComplete by mutableStateOf(false)
    var currentBooking by mutableStateOf<BookingData?>(null)

    val travelDuration: Int get() = TravelTime.getDuration(origin, destination)

    val arrivalTime: String
        get() = selectedTime?.let {
            DepartureSchedule.calculateArrival(it, travelDuration)
        } ?: ""

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

    fun toggleSeatSelection(seatId: String) {
        val current = selectedSeats.toMutableList()
        if (current.contains(seatId)) {
            current.remove(seatId)
        } else {
            if (current.size < ticketCount) {
                current.add(seatId)
            } else {
                // If it's already full, replace the last clicked one? Or do nothing. 
                // Alternatively, pop the first one and add new one.
                current.removeAt(0)
                current.add(seatId)
            }
        }
        selectedSeats = current
    }

    private fun generateBookingCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return "SWF-" + (1..8).map { chars.random() }.joinToString("")
    }

    // Prepare Firestore Data for seating selection
    fun prepareSeatingAndFetch() {
        val time = selectedTime ?: return
        if (departureDate.isEmpty()) return
        
        val scheduleId = firestoreRepo.getScheduleId(origin, destination, departureDate, time)
        isLoadingSeats = true
        
        viewModelScope.launch {
            firestoreRepo.checkAndSeedData(scheduleId) {
                // After checking/seeding, start listening
                firestoreRepo.getCoachesFlow(scheduleId) { updatedCoaches ->
                    coaches = updatedCoaches
                    isLoadingSeats = false
                }
            }
        }
    }

    fun processFinalBooking() {
        val isPassengersValid = passengers.all { it.name.isNotBlank() && it.identityNumber.isNotBlank() }
        val isFirstPassengerEmailValid = passengers.firstOrNull()?.email?.isNotBlank() == true

        if (selectedSeats.size != ticketCount || !isPassengersValid || !isFirstPassengerEmailValid) {
            bookingErrorMessage = "Please complete all passenger details and contact email."
            return
        }
        
        val time = selectedTime ?: return
        val scheduleId = firestoreRepo.getScheduleId(origin, destination, departureDate, time)
        
        isProcessingBooking = true
        bookingErrorMessage = null
        
        viewModelScope.launch {
            val success = firestoreRepo.bookSeatsTransaction(scheduleId, selectedCoachId, selectedSeats)
            if (success) {
                val code = generateBookingCode()
                val completeBk = BookingData(
                    bookingCode = code,
                    passengerName = passengers.first().name,
                    passengerEmail = passengers.first().email,
                    passengers = passengers.toList(),
                    origin = origin,
                    destination = destination,
                    ticketCount = ticketCount,
                    departureDate = departureDate,
                    departureTime = time,
                    arrivalTime = arrivalTime,
                    travelDuration = travelDuration,
                    coachClass = selectedCoach ?: CoachClass.PREMIUM_ECONOMY,
                    selectedCoachId = selectedCoachId,
                    selectedSeats = selectedSeats,
                    pricePerTicket = pricePerTicket,
                    totalPrice = totalPrice
                )
                currentBooking = completeBk
                bookingComplete = true
                isProcessingBooking = false

                // Try to send email automatically in background thread
                EmailSender.sendTicketEmail(completeBk, formatCurrency(totalPrice))
            } else {
                isProcessingBooking = false
                bookingErrorMessage = "Sorry, one or more selected seats have been booked. Please select other seats."
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
    }

    fun formatCurrency(amount: Int): String {
        val formatted = String.format("%,d", amount).replace(",", ".")
        return "Rp $formatted"
    }
}
