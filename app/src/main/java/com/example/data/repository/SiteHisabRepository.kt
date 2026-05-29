package com.example.data.repository

import com.example.data.dao.SiteHisabDao
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import kotlinx.coroutines.flow.Flow

class SiteHisabRepository(private val dao: SiteHisabDao) {
    val allWorkers: Flow<List<Worker>> = dao.getAllWorkersFlow()
    val activeWorkers: Flow<List<Worker>> = dao.getActiveWorkersFlow()
    val allPayments: Flow<List<Payment>> = dao.getAllPaymentsFlow()

    suspend fun getWorkerById(workerId: Int): Worker? = dao.getWorkerById(workerId)
    suspend fun getWorkerByPhone(phone: String): Worker? = dao.getWorkerByPhone(phone)
    suspend fun insertWorker(worker: Worker): Long = dao.insertWorker(worker)
    suspend fun updateWorker(worker: Worker) = dao.updateWorker(worker)

    fun getAttendanceForDateFlow(date: String): Flow<List<Attendance>> = dao.getAttendanceForDateFlow(date)
    suspend fun getAttendanceForDate(date: String): List<Attendance> = dao.getAttendanceForDate(date)
    fun getAttendanceForWorkerFlow(workerId: Int): Flow<List<Attendance>> = dao.getAttendanceForWorkerFlow(workerId)
    
    suspend fun insertAttendance(attendance: Attendance): Long = dao.insertAttendance(attendance)
    suspend fun insertAttendanceList(attendanceList: List<Attendance>) = dao.insertAttendanceList(attendanceList)
    suspend fun deleteAttendanceForWorkerOnDate(workerId: Int, date: String) = dao.deleteAttendanceForWorkerOnDate(workerId, date)

    fun getPaymentsForWorkerFlow(workerId: Int): Flow<List<Payment>> = dao.getPaymentsForWorkerFlow(workerId)
    suspend fun insertPayment(payment: Payment): Long = dao.insertPayment(payment)
    suspend fun updatePayment(payment: Payment) = dao.updatePayment(payment)
    suspend fun deletePaymentById(transactionId: Int) = dao.deletePaymentById(transactionId)
}
