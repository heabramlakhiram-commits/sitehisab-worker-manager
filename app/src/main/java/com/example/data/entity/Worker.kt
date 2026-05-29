package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val workerId: Int = 0,
    val name: String,
    val phone: String,
    val dailyWage: Double,
    val joinDate: String, // format yyyy-MM-dd
    val status: String // "Active" or "Left"
) : Serializable
