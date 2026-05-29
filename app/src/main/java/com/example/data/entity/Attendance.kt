package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance",
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
data class Attendance(
    @PrimaryKey(autoGenerate = true) val attendanceId: Int = 0,
    val workerId: Int,
    val date: String, // format yyyy-MM-dd
    val status: String // "Present", "Absent", "Half-Day"
)
