package com.lankasmartmart.app.data.model

data class Address(
    val id: String = "",
    val label: String = "", // e.g. "Home", "Work"
    val name: String = "", // Recipient Name
    val street: String = "",
    val city: String = "",
    val phone: String = "",
    val isDefault: Boolean = false
)
