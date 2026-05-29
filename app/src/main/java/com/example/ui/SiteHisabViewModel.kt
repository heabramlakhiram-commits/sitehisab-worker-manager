package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SiteHisabDatabase
import com.example.data.entity.Attendance
import com.example.data.entity.Payment
import com.example.data.entity.Worker
import com.example.data.repository.SiteHisabRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class AuthState {
    object LoggedOut : AuthState()
    object Manager : AuthState()
    data class WorkerSession(val worker: Worker) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NavigationState {
    object Login : NavigationState()
    object ManagerDashboard : NavigationState()
    object AddWorker : NavigationState()
    data class WorkerDetails(val workerId: Int) : NavigationState()
    data class WorkerDashboard(val workerId: Int) : NavigationState()
}

class SiteHisabViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SiteHisabRepository
    
    // Auth State
    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Navigation State
    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState.Login)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // Backstack for manual navigation
    private val backStack = mutableListOf<NavigationState>()

    // Core Data Lists
    val allWorkers: StateFlow<List<Worker>>
    val activeWorkers: StateFlow<List<Worker>>
    
    // Search & Filters for Manager
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _showOnlyActive = MutableStateFlow(true)
    val showOnlyActive: StateFlow<Boolean> = _showOnlyActive.asStateFlow()

    // Selected Date for Manager Attendance Taking (defaults to today)
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Map of workerId -> Attendance for the selected date
    private val _attendanceMap = MutableStateFlow<Map<Int, Attendance>>(emptyMap())
    val attendanceMap: StateFlow<Map<Int, Attendance>> = _attendanceMap.asStateFlow()

    // Payments lists
    val allPayments: StateFlow<List<Payment>>

    init {
        val database = SiteHisabDatabase.getDatabase(application)
        repository = SiteHisabRepository(database.siteHisabDao())
        
        allWorkers = repository.allWorkers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        activeWorkers = repository.activeWorkers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allPayments = repository.allPayments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Hydrate default data if database is empty
        viewModelScope.launch {
            repository.allWorkers.first().let { currentList ->
                if (currentList.isEmpty()) {
                    demoOnboarding()
                }
            }
        }

        // Keep attendance status updated for selected date
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadAttendanceForDate(date)
            }
        }
    }

    // Helper to format date
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    private suspend fun demoOnboarding() {
        val today = getTodayDateString()
        
        // Setup past dates to populate demo history
        val cal = Calendar.getInstance()
        val datesList = mutableListOf<String>()
        for (i in 1..5) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            datesList.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time))
        }

        val worker1Id = repository.insertWorker(Worker(0, "Ramesh Kumar", "9811111111", 450.0, datesList.last(), "Active")).toInt()
        val worker2Id = repository.insertWorker(Worker(0, "Shanti Devi", "9811111112", 400.0, datesList.last(), "Active")).toInt()
        val worker3Id = repository.insertWorker(Worker(0, "Amit Singh", "9811111113", 500.0, datesList.last(), "Active")).toInt()
        val worker4Id = repository.insertWorker(Worker(0, "Vijay Yadav", "9811111114", 420.0, datesList.last(), "Left")).toInt()

        // Core past attendance
        datesList.forEachIndexed { index, date ->
            // Ramesh: 4 Present, 1 Half-Day
            val status1 = if (index == 2) "Half-Day" else "Present"
            repository.insertAttendance(Attendance(0, worker1Id, date, status1))

            // Shanti: 3 Present, 1 Absent, 1 Half-Day
            val status2 = when(index) {
                0 -> "Absent"
                1 -> "Half-Day"
                else -> "Present"
            }
            repository.insertAttendance(Attendance(0, worker2Id, date, status2))

            // Amit: 5 Present
            repository.insertAttendance(Attendance(0, worker3Id, date, "Present"))

            // Vijay: Left (no attendance since he left)
        }

        // Payments
        repository.insertPayment(Payment(0, worker1Id, 500.0, "Advance", datesList[1], "Success"))
        repository.insertPayment(Payment(0, worker1Id, 200.0, "Advance", today, "Pending"))
        repository.insertPayment(Payment(0, worker2Id, 300.0, "Advance", datesList[0], "Success"))
        repository.insertPayment(Payment(0, worker3Id, 1000.0, "Advance", datesList[2], "Success"))
    }

    private suspend fun loadAttendanceForDate(date: String) {
        val list = repository.getAttendanceForDate(date)
        _attendanceMap.value = list.associateBy { it.workerId }
    }

    // Navigation helper
    fun navigateTo(screen: NavigationState) {
        backStack.add(_navigationState.value)
        _navigationState.value = screen
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            _navigationState.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        return false
    }

    // Login Action
    fun login(phone: String, otp: String) {
        viewModelScope.launch {
            if (phone == "9876543210") {
                if (otp == "123456") {
                    _authState.value = AuthState.Manager
                    _navigationState.value = NavigationState.ManagerDashboard
                } else {
                    _authState.value = AuthState.Error("Invalid Admin OTP. Hint: Use 123456")
                }
            } else {
                val worker = repository.getWorkerByPhone(phone)
                if (worker != null) {
                    if (otp == "123456" || otp.isNotEmpty()) { // standard OTP pass
                        _authState.value = AuthState.WorkerSession(worker)
                        _navigationState.value = NavigationState.WorkerDashboard(worker.workerId)
                    } else {
                        _authState.value = AuthState.Error("Please enter safety OTP (e.g., 123456)")
                    }
                } else {
                    _authState.value = AuthState.Error("This mobile number is not registered. Please ask the Manager to add your phone.")
                }
            }
        }
    }

    fun logout() {
        _authState.value = AuthState.LoggedOut
        _navigationState.value = NavigationState.Login
        backStack.clear()
        _searchQuery.value = ""
    }

    fun clearAuthError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.LoggedOut
        }
    }

    // Worker creation & Updates
    fun addWorker(name: String, phone: String, dailyWage: Double) {
        viewModelScope.launch {
            val joinDate = getTodayDateString()
            val newWorker = Worker(0, name, phone, dailyWage, joinDate, "Active")
            repository.insertWorker(newWorker)
            navigateBack()
        }
    }

    fun toggleWorkerStatus(workerId: Int, isLeft: Boolean) {
        viewModelScope.launch {
            val worker = repository.getWorkerById(workerId)
            if (worker != null) {
                val updated = worker.copy(status = if (isLeft) "Left" else "Active")
                repository.updateWorker(updated)
            }
        }
    }

    fun updateWorkerDetails(worker: Worker) {
        viewModelScope.launch {
            repository.updateWorker(worker)
        }
    }

    // Attendance taking
    fun setAttendance(workerId: Int, status: String) {
        viewModelScope.launch {
            val date = _selectedDate.value
            repository.deleteAttendanceForWorkerOnDate(workerId, date)
            val newAttendance = Attendance(0, workerId, date, status)
            repository.insertAttendance(newAttendance)
            loadAttendanceForDate(date)
        }
    }

    fun setDate(date: String) {
        _selectedDate.value = date
    }

    // Payments tracking
    fun recordPayment(workerId: Int, amount: Double, type: String, status: String = "Success") {
        viewModelScope.launch {
            val today = getTodayDateString()
            val payment = Payment(0, workerId, amount, type, today, status)
            repository.insertPayment(payment)
        }
    }

    fun deletePayment(paymentId: Int) {
        viewModelScope.launch {
            repository.deletePaymentById(paymentId)
        }
    }

    fun updatePaymentStatus(payment: Payment, newStatus: String) {
        viewModelScope.launch {
            repository.updatePayment(payment.copy(status = newStatus))
        }
    }

    // Search query setter
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowOnlyActive(active: Boolean) {
        _showOnlyActive.value = active
    }

    // Financial queries and mappings
    fun getFinancialSummary(worker: Worker, attendanceList: List<Attendance>, paymentsList: List<Payment>): WorkerFinancialSummary {
        val totalPresent = attendanceList.count { it.status == "Present" }
        val totalHalfDays = attendanceList.count { it.status == "Half-Day" }
        val totalAbsent = attendanceList.count { it.status == "Absent" }
        
        // Formula: Earnings = (Present * wage) + (Half * (wage / 2))
        val totalEarnings = (totalPresent * worker.dailyWage) + (totalHalfDays * (worker.dailyWage / 2.0))
        
        // Sum Advance taken
        val totalAdvance = paymentsList.filter { it.type == "Advance" && it.status == "Success" }.sumOf { it.amount }
        
        // Sum Final Salary successfully paid
        val totalFinalPaid = paymentsList.filter { it.type == "Final Salary" && it.status == "Success" }.sumOf { it.amount }
        
        val netPayable = totalEarnings - totalAdvance - totalFinalPaid
        
        return WorkerFinancialSummary(
            totalPresentDays = totalPresent,
            totalHalfDays = totalHalfDays,
            totalAbsentDays = totalAbsent,
            totalEarnings = totalEarnings,
            totalAdvanceTaken = totalAdvance,
            totalFinalPaid = totalFinalPaid,
            netPayable = netPayable
        )
    }

    // Observe specific worker attendance data
    fun getAttendanceForWorker(workerId: Int): Flow<List<Attendance>> {
        return repository.getAttendanceForWorkerFlow(workerId)
    }

    // Observe specific worker payments data
    fun getPaymentsForWorker(workerId: Int): Flow<List<Payment>> {
        return repository.getPaymentsForWorkerFlow(workerId)
    }
    
    // Fetch individual worker object as Flow/State
    fun getWorkerFlow(workerId: Int): Flow<Worker?> {
        return allWorkers.map { workers -> workers.firstOrNull { it.workerId == workerId } }
    }
}

data class WorkerFinancialSummary(
    val totalPresentDays: Int,
    val totalHalfDays: Int,
    val totalAbsentDays: Int,
    val totalEarnings: Double,
    val totalAdvanceTaken: Double,
    val totalFinalPaid: Double,
    val netPayable: Double
)
