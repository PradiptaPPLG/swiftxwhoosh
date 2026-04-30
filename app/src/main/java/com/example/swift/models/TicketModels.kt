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
            // Jika departureTime berisi tanggal (misal: "2026-04-28 08:00"), ambil bagian waktunya saja
            val timeOnly = if (departureTime.contains(" ")) departureTime.split(" ")[1] else departureTime
            
            val parts = timeOnly.split(":")
            if (parts.size < 2) return departureTime
            
            // Ambil hanya angka (regex) untuk jaga-jaga ada karakter aneh
            val depHour = parts[0].filter { it.isDigit() }.toInt()
            val depMin = parts[1].filter { it.isDigit() }.toInt()

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

data class PassengerDTO(
    val name: String,
    @SerializedName("identity_type") val identityType: String,
    @SerializedName("identity_number") val identityNumber: String,
    val gender: String
)

data class SavedPassengersResponse(
    val status: String,
    val passengers: List<PassengerDTO>
)

data class OccupiedSeatsResponse(
    val status: String,
    @SerializedName("occupied_seats") val occupiedSeats: List<String>
)
data class BookSeatRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("schedule_id") val scheduleId: Int,
    @SerializedName("coach_id") val coachId: String,
    @SerializedName("seats") val seats: List<String>,
    @SerializedName("passenger_names") val passengerNames: List<String>,
    @SerializedName("total_price") val totalPrice: Int
)

// Real Dynamic Booking Models
data class UserBooking(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("booking_code") val bookingCode: String,
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("status") val status: String, // 'paid', 'unpaid', 'cancelled', 'refunded'
    @SerializedName("order_time") val orderTime: String,
    @SerializedName("origin_station") val originStation: String,
    @SerializedName("destination_station") val destinationStation: String,
    @SerializedName("departure_time") val departureTime: String, // Format: YYYY-MM-DD HH:mm:ss
    @SerializedName("train_number") val trainNumber: String,
    @SerializedName("passenger_names") val passengerNames: String,
    @SerializedName("ticket_count") val ticketCount: Int
) {
    // Logika 2 Jam: Wajib dilakukan minimal 2 jam sebelum keberangkatan
    fun canBeModified(): Boolean {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val departureDate = sdf.parse(departureTime)
            val now = java.util.Date()
            
            if (departureDate == null) return false
            
            // Selisih dalam milidetik
            val diff = departureDate.time - now.time
            val twoHoursInMs = 2 * 60 * 60 * 1000L
            
            diff > twoHoursInMs
        } catch (e: Exception) {
            false
        }
    }
}

data class UserBookingsResponse(
    val status: String,
    val bookings: List<UserBooking> = emptyList(),
    val message: String? = null
)
