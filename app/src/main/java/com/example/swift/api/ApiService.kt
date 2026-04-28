package com.example.swift.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.swift.models.*

interface ApiService {
    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @FormUrlEncoded
    @POST("register.php")
    fun register(
        @Field("full_name") fullName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("password") password: String
    ): Call<AuthResponse>

    @retrofit2.http.GET("get_schedules.php")
    fun getSchedules(
        @retrofit2.http.Query("origin") origin: String,
        @retrofit2.http.Query("destination") destination: String
    ): Call<List<TrainSchedule>>

    @retrofit2.http.POST("create_booking.php")
    fun createBooking(@retrofit2.http.Body bookingRequest: Map<String, Any>): Call<Map<String, Any>>

    @retrofit2.http.GET("get_passengers.php")
    fun getSavedPassengers(@retrofit2.http.Query("user_id") userId: Int): Call<SavedPassengersResponse>

    @FormUrlEncoded
    @POST("save_passenger.php")
    fun savePassenger(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("identity_type") identityType: String,
        @Field("identity_number") identityNumber: String,
        @Field("gender") gender: String,
        @Field("date_of_birth") dateOfBirth: String,
        @Field("phone") phone: String,
        @Field("email") email: String
    ): Call<Map<String, Any>>

    @GET("get_seats.php")
    fun getSeats(
        @Query("schedule_id") scheduleId: Int,
        @Query("coach_id") coachId: String
    ): Call<OccupiedSeatsResponse>

    @POST("book_seats.php")
    fun bookSeats(@Body request: BookSeatRequest): Call<Map<String, Any>>

    @POST("lock_seat.php")
    fun lockSeat(@Body lockRequest: Map<String, Any>): Call<Map<String, Any>>
}
