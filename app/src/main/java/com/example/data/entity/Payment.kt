package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Worker::class,
            parentColumns = ["workerId"],
            childColumns = ["workerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workerId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val transactionId: Int = 0,
    val workerId: Int,
    val amount: Double,
    val type: String, // "Advance" or "Final Salary"
    val date: String, // format yyyy-MM-dd
    val status: String // "Pending" or "Success"
)
