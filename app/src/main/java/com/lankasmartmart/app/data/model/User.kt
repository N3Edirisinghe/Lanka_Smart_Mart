package com.lankasmartmart.app.data.model

import com.google.firebase.Timestamp

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val phoneNumber: String = "",
    val addresses: List<Address> = emptyList(),
    val favorites: List<String> = emptyList(),
    val loyaltyPoints: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)
