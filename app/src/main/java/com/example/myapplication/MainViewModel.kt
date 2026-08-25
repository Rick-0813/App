package com.example.myapplication

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "JobBoom_Log"

data class UserAccount(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val role: String = "Worker",
    val savedJobs: List<Int> = emptyList()
)

data class Job(
    val id: Int = 0,
    val title: String = "",
    val company: String = "",
    val salary: String = "",
    val description: String = ""
)

data class JobApplication(
    val id: Int = 0,
    val jobId: Int = 0,
    val workerName: String = "",
    val workerEmail: String = "",
    var status: String = "Pending"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("jobboom_local_data", Context.MODE_PRIVATE)

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserAccount>>(emptyList())

    var currentUser by mutableStateOf<UserAccount?>(null)
        private set

    init {
        loadDataFromLocal()
    }

    // 同时支持 Email, Name, Phone 检查
    suspend fun checkUserExists(account: String): UserAccount? {
        val cleanAccount = account.trim()
        Log.d(TAG, "Checking account: $cleanAccount")
        val user = _allUsers.value.find { 
            it.email.equals(cleanAccount, ignoreCase = true) || 
            it.name.equals(cleanAccount, ignoreCase = true) ||
            it.phone == cleanAccount
        }
        return user
    }

    suspend fun registerUser(name: String, email: String, password: String, role: String): Result<Unit> {
        if (checkUserExists(email) != null) return Result.failure(Exception("Account already registered!"))

        val newUser = UserAccount(name.trim(), email.trim(), "", password.trim(), role)
        _allUsers.value = _allUsers.value + newUser
        currentUser = newUser
        saveAllDataToLocal()
        return Result.success(Unit)
    }

    suspend fun loginUser(accountInput: String, passwordInput: String): Result<Unit> {
        val cleanAccount = accountInput.trim()
        val user = _allUsers.value.find { 
            (it.email.equals(cleanAccount, ignoreCase = true) || it.phone == cleanAccount || it.name.equals(cleanAccount, ignoreCase = true)) 
            && it.password == passwordInput.trim() 
        }
        return if (user != null) {
            currentUser = user
            _savedJobIds.value = user.savedJobs.toSet()
            saveAllDataToLocal()
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid account or password!"))
        }
    }

    suspend fun loginWithGoogle(email: String, name: String): Result<Unit> {
        Log.d(TAG, "Google Login attempt: $email")
        val existing = checkUserExists(email)
        return if (existing != null) {
            currentUser = existing
            _savedJobIds.value = existing.savedJobs.toSet()
            saveAllDataToLocal()
            Log.d(TAG, "Google Login successful for ${existing.name}")
            Result.success(Unit)
        } else {
            // 如果是第一次通过 Google 登录，自动创建一个账户
            val newUser = UserAccount(name, email, "", "G-AUTH-PASS", "Worker")
            _allUsers.value = _allUsers.value + newUser
            currentUser = newUser
            saveAllDataToLocal()
            Log.d(TAG, "Google Register+Login successful")
            Result.success(Unit)
        }
    }

    fun applyForJob(jobId: Int): Boolean {
        val user = currentUser ?: return false
        val newApp = JobApplication(
            id = (System.currentTimeMillis() % 100000).toInt(),
            jobId = jobId,
            workerName = user.name,
            workerEmail = user.email,
            status = "Pending"
        )
        _applications.value = _applications.value + newApp
        saveAllDataToLocal()
        return true
    }

    fun toggleSaveJob(jobId: Int) {
        val user = currentUser ?: return
        val currentSet = _savedJobIds.value
        val newSet = if (currentSet.contains(jobId)) currentSet - jobId else currentSet + jobId
        _savedJobIds.value = newSet
        
        _allUsers.value = _allUsers.value.map {
            if (it.email == user.email) it.copy(savedJobs = newSet.toList()) else it
        }
        saveAllDataToLocal()
    }

    fun updateApplicationStatus(appId: Int, newStatus: String) {
        _applications.value = _applications.value.map {
            if (it.id == appId) it.copy(status = newStatus) else it
        }
        saveAllDataToLocal()
    }

    fun logout() {
        currentUser = null
        _savedJobIds.value = emptySet()
        saveAllDataToLocal()
    }

    suspend fun updateUserProfile(newName: String, newEmail: String, newPhone: String, newPassword: String) {
        val user = currentUser ?: return
        val updatedUser = user.copy(
            name = newName.ifBlank { user.name },
            phone = newPhone.trim(),
            password = if (newPassword.isNotBlank()) newPassword else user.password
        )
        currentUser = updatedUser
        _allUsers.value = _allUsers.value.map {
            if (it.email == user.email) updatedUser else it
        }
        saveAllDataToLocal()
    }

    fun updateUserRole(newRole: String) {
        val user = currentUser ?: return
        val updatedUser = user.copy(role = newRole)
        currentUser = updatedUser
        _allUsers.value = _allUsers.value.map {
            if (it.email == user.email) updatedUser else it
        }
        saveAllDataToLocal()
    }

    fun postJob(title: String, company: String, salary: String, description: String) {
        val newJob = Job(
            id = (_jobs.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            company = company,
            salary = salary,
            description = description
        )
        _jobs.value = _jobs.value + newJob
        saveAllDataToLocal()
    }

    private fun saveAllDataToLocal() {
        val editor = prefs.edit()
        val usersArr = JSONArray()
        _allUsers.value.forEach { u ->
            val obj = JSONObject()
            obj.put("name", u.name)
            obj.put("email", u.email)
            obj.put("phone", u.phone)
            obj.put("password", u.password)
            obj.put("role", u.role)
            obj.put("savedJobs", JSONArray(u.savedJobs))
            usersArr.put(obj)
        }
        editor.putString("all_users", usersArr.toString())

        currentUser?.let { u ->
            editor.putString("active_email", u.email)
        } ?: editor.remove("active_email")

        val jobsArr = JSONArray()
        _jobs.value.forEach { j ->
            val obj = JSONObject()
            obj.put("id", j.id)
            obj.put("title", j.title)
            obj.put("company", j.company)
            obj.put("salary", j.salary)
            obj.put("description", j.description)
            jobsArr.put(obj)
        }
        editor.putString("all_jobs", jobsArr.toString())

        val appsArr = JSONArray()
        _applications.value.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("jobId", a.jobId)
            obj.put("workerName", a.workerName)
            obj.put("workerEmail", a.workerEmail)
            obj.put("status", a.status)
            appsArr.put(obj)
        }
        editor.putString("all_apps", appsArr.toString())

        editor.apply()
        Log.d(TAG, "Persistence saved successfully.")
    }

    private fun loadDataFromLocal() {
        val usersStr = prefs.getString("all_users", null)
        if (!usersStr.isNullOrEmpty()) {
            val list = mutableListOf<UserAccount>()
            val arr = JSONArray(usersStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val savedArr = obj.getJSONArray("savedJobs")
                val savedList = mutableListOf<Int>()
                for (j in 0 until savedArr.length()) savedList.add(savedArr.getInt(j))
                
                list.add(UserAccount(
                    obj.getString("name"), obj.getString("email"), 
                    obj.getString("phone"), obj.getString("password"), 
                    obj.getString("role"), savedList
                ))
            }
            _allUsers.value = list
        } else {
            _allUsers.value = listOf(
                UserAccount("Derrick Tan", "derrick@test.com", "012345678", "123456", "Worker"),
                UserAccount("Boss Dennis", "boss@test.com", "019876543", "123456", "Employer")
            )
        }

        val activeEmail = prefs.getString("active_email", null)
        currentUser = _allUsers.value.find { it.email == activeEmail }
        _savedJobIds.value = currentUser?.savedJobs?.toSet() ?: emptySet()

        val jobsStr = prefs.getString("all_jobs", null)
        if (!jobsStr.isNullOrEmpty()) {
            val list = mutableListOf<Job>()
            val arr = JSONArray(jobsStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(Job(obj.getInt("id"), obj.getString("title"), obj.getString("company"), obj.getString("salary"), obj.getString("description")))
            }
            _jobs.value = list
        } else {
            _jobs.value = listOf(
                Job(1, "Software Engineer", "Google", "$150,000", "Full-time Dev"),
                Job(2, "Product Manager", "Meta", "$140,000", "Full-time PM"),
                Job(3, "UI/UX Designer", "Apple", "$130,000", "Part-time Design"),
                Job(4, "Delivery Helper", "GreenGro", "RM 15/Hr", "Flexible hours"),
                Job(5, "Cashier", "Fresh Market", "RM 12/Hr", "Retail shift")
            )
        }

        val appsStr = prefs.getString("all_apps", null)
        if (!appsStr.isNullOrEmpty()) {
            val list = mutableListOf<JobApplication>()
            val arr = JSONArray(appsStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(JobApplication(obj.getInt("id"), obj.getInt("jobId"), obj.getString("workerName"), obj.getString("workerEmail"), obj.getString("status")))
            }
            _applications.value = list
        }
    }
}
