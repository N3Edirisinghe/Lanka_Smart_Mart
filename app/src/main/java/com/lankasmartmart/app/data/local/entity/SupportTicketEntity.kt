package com.lankasmartmart.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val subject: String,
    val description: String,
    val status: String,
    val createdAt: Long
)
