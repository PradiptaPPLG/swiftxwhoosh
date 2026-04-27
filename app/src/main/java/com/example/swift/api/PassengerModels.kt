package com.example.swift.api

import com.example.swift.models.PassengerDetail
import com.example.swift.models.PassengerType
import com.example.swift.models.IdentityType
import com.example.swift.models.Gender

data class SavedPassengersResponse(
    val status: String,
    val message: String?,
    val data: List<PassengerDetail>
)
