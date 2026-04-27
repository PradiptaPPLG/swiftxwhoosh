package com.example.swift.api

import com.google.gson.annotations.SerializedName

data class TrainSchedule(
    @SerializedName("schedule_id") val scheduleId: String,
    @SerializedName("train_name") val trainName: String,
    @SerializedName("train_code") val trainCode: String,
    @SerializedName("origin_name") val originName: String,
    @SerializedName("destination_name") val destinationName: String,
    @SerializedName("departure_time") val departureTime: String,
    @SerializedName("arrival_time") val arrivalTime: String,
    @SerializedName("price") val price: String
)
