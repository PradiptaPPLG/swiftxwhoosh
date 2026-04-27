package com.example.swift.models

import com.google.gson.annotations.SerializedName

enum class Station(val displayName: String) {
    TEGALLUAR("Tegalluar"),
    PADALARANG("Padalarang"),
    KARAWANG("Karawang"),
    HALIM("Halim")
}

enum class CoachClass(val displayName: String) {
    FIRST("First Class"),
    BUSINESS("Business Class"),
    PREMIUM_ECONOMY("Premium Economy Class")
}

data class BookingData(
    val bookingCode: String = "",
    val passengerName: String = "",
    val passengerEmail: String = "",
    val passengers: List<PassengerDetail> = emptyList(),
    val origin: Station = Station.HALIM,
    val destination: Station = Station.TEGALLUAR,
    val ticketCount: Int = 1,
    val departureDate: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val travelDuration: Int = 0,
    val coachClass: CoachClass = CoachClass.PREMIUM_ECONOMY,
    val selectedCoachId: String = "",
    val selectedSeats: List<String> = emptyList(),
    val pricePerTicket: Int = 0,
    val totalPrice: Int = 0
)
object TicketPricing {
    fun getPrice(coach: CoachClass, ticketCount: Int = 1): Int {
        val basePrice = when (coach) {
            CoachClass.PREMIUM_ECONOMY -> 250_000
            CoachClass.BUSINESS -> 450_000
            CoachClass.FIRST -> 600_000
        }
        
        // Dynamic pricing based on ticket count
        return when {
            ticketCount >= 9 -> (basePrice * 0.85).toInt() // 15% discount
            ticketCount >= 6 -> (basePrice * 0.90).toInt() // 10% discount
            ticketCount >= 3 -> (basePrice * 0.95).toInt() // 5% discount
            else -> basePrice
        }
    }
}

object TravelTime {
    private val timeMap = mapOf(
        Pair(Station.TEGALLUAR, Station.PADALARANG) to 17,
        Pair(Station.TEGALLUAR, Station.KARAWANG) to 35,
        Pair(Station.TEGALLUAR, Station.HALIM) to 52,
        Pair(Station.PADALARANG, Station.TEGALLUAR) to 22,
        Pair(Station.PADALARANG, Station.KARAWANG) to 18,
        Pair(Station.PADALARANG, Station.HALIM) to 35,
        Pair(Station.KARAWANG, Station.TEGALLUAR) to 41,
        Pair(Station.KARAWANG, Station.PADALARANG) to 19,
        Pair(Station.KARAWANG, Station.HALIM) to 17,
        Pair(Station.HALIM, Station.TEGALLUAR) to 52,
        Pair(Station.HALIM, Station.PADALARANG) to 30,
        Pair(Station.HALIM, Station.KARAWANG) to 11
    )

    fun getDuration(from: Station, to: Station): Int {
        return timeMap[Pair(from, to)] ?: 0
    }
}

object DepartureSchedule {
    val times: List<String> = buildList {
        for (hour in 6..21) {
            add("%02d:00".format(hour))
            add("%02d:30".format(hour))
        }
    }

    fun calculateArrival(departureTime: String, durationMinutes: Int): String {
        return try {
            val parts = departureTime.split(":")
            if (parts.size < 2) return departureTime
            val depHour = parts[0].toInt()
            val depMin = parts[1].toInt()

            var totalMinutes = depHour * 60 + depMin + durationMinutes
            val arrivalHour = (totalMinutes / 60) % 24
            val arrivalMin = totalMinutes % 60

            String.format("%02d:%02d", arrivalHour, arrivalMin)
        } catch (e: Exception) {
            departureTime
        }
    }
}

enum class PassengerType(val displayName: String) {
    ADULT("Adult"),
    CHILD("Child")
}

enum class IdentityType(val displayName: String) {
    ID_CARD("Identity No."),
    PASSPORT("Passport")
}

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female")
}

data class PassengerDetail(
    val name: String = "",
    val gender: Gender = Gender.MALE,
    val dateOfBirth: String = "",
    val passengerType: PassengerType = PassengerType.ADULT,
    val discountType: String = "none",
    val countryRegion: String = "Indonesia",
    val identityType: IdentityType = IdentityType.ID_CARD,
    val identityNumber: String = "",
    val expiryDate: String = "31 Dec 2099",
    val whatsapp: String = "",
    val email: String = "" 
)

data class Seat(
    val id: String = "", // e.g. "1A", "13F"
    val isAvailable: Boolean = true
)

data class Coach(
    val id: String = "", // e.g. "01", "02"
    val seats: List<Seat> = emptyList()
)

data class ScheduleDocument(
    val routeId: String = "",
    val departureDate: String = "",
    val departureTime: String = ""
)

data class SavedPassengersResponse(
    val status: String,
    val data: List<PassengerDetail>
)

data class OccupiedSeatsResponse(
    val status: String,
    @SerializedName("occupied_seats") val occupiedSeats: List<String>
)
