package com.example.data.dao

import androidx.room.*
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteHisabDao {
    // Workers queries
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkersFlow(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE status = 'Active' ORDER BY name ASC")
    fun getActiveWorkersFlow(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE workerId = :workerId")
    suspend fun getWorkerById(workerId: Int): Worker?

    @Query("SELECT * FROM workers WHERE phone = :phone LIMIT 1")
    suspend fun getWorkerByPhone(phone: String): Worker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Update
    suspend fun updateWorker(worker: Worker)

    // Attendance queries
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceForDate(date: String): List<Attendance>

    @Query("SELECT * FROM attendance WHERE workerId = :workerId")
    fun getAttendanceForWorkerFlow(workerId: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>)

    @Query("DELETE FROM attendance WHERE workerId = :workerId AND date = :date")
    suspend fun deleteAttendanceForWorkerOnDate(workerId: Int, date: String)

    // Payments queries
    @Query("SELECT * FROM payments WHERE workerId = :workerId ORDER BY date DESC")
    fun getPaymentsForWorkerFlow(workerId: Int): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPaymentsFlow(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE transactionId = :transactionId")
    suspend fun deletePaymentById(transactionId: Int)
}
