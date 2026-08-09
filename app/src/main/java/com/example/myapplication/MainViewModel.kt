package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class UserAccount(
    val name: String,
    val email: String,
    val password: String,
    val role: String
)

data class Job(
    val id: Int,
    val title: String,
    val company: String,
    val salary: String,
    val description: String = "Full-time / Part-time position with competitive salary and flexible hours."
)

data class JobApplication(
    val id: Int,
    val jobId: Int,
    val workerName: String,
    val workerEmail: String,
    var status: String
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("jobboom_data", Context.MODE_PRIVATE)

    private val _registeredUsers = MutableStateFlow<List<UserAccount>>(emptyList())
    val registeredUsers: StateFlow<List<UserAccount>> = _registeredUsers.asStateFlow()

    var currentUser by mutableStateOf<UserAccount?>(null)
        private set

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()


    init {
        loadDataFromLocal()
    }


    fun updateUserRole(newRole: String) {
        val activeUser = currentUser ?: return
        val updatedUser = activeUser.copy(role = newRole)
        currentUser = updatedUser

        _registeredUsers.value = _registeredUsers.value.map {
            if (it.email == activeUser.email || it.name == activeUser.name) updatedUser else it
        }
        saveAllDataToLocal()
    }

    fun registerUser(name: String, email: String, password: String, role: String): Result<String> {
        val existing = _registeredUsers.value.find {
            it.email.equals(email.trim(), ignoreCase = true) ||
                    it.name.equals(name.trim(), ignoreCase = true)
        }
        if (existing != null) {
            return Result.failure(Exception("Username or Email already registered!"))
        }

        val newUser = UserAccount(name.trim(), email.trim(), password.trim(), role)
        _registeredUsers.value = _registeredUsers.value + newUser
        saveAllDataToLocal()
        return Result.success("Registration successful! Please log in.")
    }

    fun loginUser(accountInput: String, passwordInput: String, targetRole: String): Result<UserAccount> {
        val user = _registeredUsers.value.find {
            (it.email.equals(accountInput.trim(), ignoreCase = true) ||
                    it.name.equals(accountInput.trim(), ignoreCase = true)) &&
                    it.password == passwordInput.trim()
        }

        return if (user != null) {
            val updatedUser = user.copy(role = targetRole)
            currentUser = updatedUser
            saveAllDataToLocal()
            Result.success(updatedUser)
        } else {
            Result.failure(Exception("Account not registered or invalid password!"))
        }
    }

    fun updateUserProfile(newName: String, newEmail: String, newPassword: String) {
        val activeUser = currentUser ?: return
        val updatedUser = activeUser.copy(
            name = newName.ifBlank { activeUser.name },
            email = newEmail.ifBlank { activeUser.email },
            password = if (newPassword.isNotBlank()) newPassword else activeUser.password
        )

        currentUser = updatedUser
        _registeredUsers.value = _registeredUsers.value.map {
            if (it.email == activeUser.email || it.name == activeUser.name) updatedUser else it
        }

        saveAllDataToLocal()
    }

    fun applyForJob(jobId: Int): Boolean {
        val user = currentUser ?: return false

        val alreadyApplied = _applications.value.any {
            it.jobId == jobId && it.workerEmail == user.email
        }
        if (alreadyApplied) return false

        val newApp = JobApplication(
            id = _applications.value.size + 1,
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
        val current = _savedJobIds.value
        _savedJobIds.value = if (current.contains(jobId)) current - jobId else current + jobId
        saveAllDataToLocal()
    }

    fun updateApplicationStatus(appId: Int, newStatus: String) {
        _applications.value = _applications.value.map { app ->
            if (app.id == appId) app.copy(status = newStatus) else app
        }
        saveAllDataToLocal()
    }



    fun postJob(title: String, company: String, salary: String, description: String) {
        val newJob = Job(
            id = _jobs.value.size + 1,
            title = title,
            company = company,
            salary = salary,
            description = description.ifBlank { "No description provided." }
        )
        _jobs.value = _jobs.value + newJob
        saveAllDataToLocal()
    }

    private fun saveAllDataToLocal() {
        val editor = prefs.edit()


        val usersJson = JSONArray()
        _registeredUsers.value.forEach { u ->
            val obj = JSONObject()
            obj.put("name", u.name)
            obj.put("email", u.email)
            obj.put("password", u.password)
            obj.put("role", u.role)
            usersJson.put(obj)
        }
        editor.putString("registered_users", usersJson.toString())

        currentUser?.let { u ->
            val obj = JSONObject()
            obj.put("name", u.name)
            obj.put("email", u.email)
            obj.put("password", u.password)
            obj.put("role", u.role)
            editor.putString("current_user", obj.toString())
        } ?: editor.remove("current_user")

        val appsJson = JSONArray()
        _applications.value.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("jobId", a.jobId)
            obj.put("workerName", a.workerName)
            obj.put("workerEmail", a.workerEmail)
            obj.put("status", a.status)
            appsJson.put(obj)
        }
        editor.putString("applications", appsJson.toString())

        val savedSet = _savedJobIds.value.map { it.toString() }.toSet()
        editor.putStringSet("saved_job_ids", savedSet)

        editor.apply()
    }

    private fun loadDataFromLocal() {
        _jobs.value = listOf(
            Job(1, "Software Engineer", "Google", "$150,000", "Develop high quality Android applications. Full-time"),
            Job(2, "Product Manager", "Meta", "$140,000", "Lead cross-functional teams to build products. Full-time"),
            Job(3, "UI/UX Designer", "Apple", "$130,000", "Design intuitive and beautiful user interfaces. Part-time"),
            Job(4, "Delivery Helper", "GreenGro", "RM 15 - 20 / Hour", "Assist with daily store logistics. Part-time"),
            Job(5, "Cashier", "Fresh Market", "RM 12 - 15 / Hour", "Manage checkout line and customer service. Part-time")
        )

        val usersStr = prefs.getString("registered_users", null)
        if (!usersStr.isNullOrEmpty()) {
            val list = mutableListOf<UserAccount>()
            val jsonArray = JSONArray(usersStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(UserAccount(obj.getString("name"), obj.getString("email"), obj.getString("password"), obj.getString("role")))
            }
            _registeredUsers.value = list
        } else {
            _registeredUsers.value = listOf(
                UserAccount("Derrick Tan", "derrick@example.com", "123456", "Worker"),
                UserAccount("Chong Liang", "chong@example.com", "123456", "Worker"),
                UserAccount("Boss Dennis", "boss@company.com", "123456", "Employer")
            )
        }

        val currentUserStr = prefs.getString("current_user", null)
        if (!currentUserStr.isNullOrEmpty()) {
            val obj = JSONObject(currentUserStr)
            currentUser = UserAccount(obj.getString("name"), obj.getString("email"), obj.getString("password"), obj.getString("role"))
        }

        val appsStr = prefs.getString("applications", null)
        if (!appsStr.isNullOrEmpty()) {
            val list = mutableListOf<JobApplication>()
            val jsonArray = JSONArray(appsStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(JobApplication(obj.getInt("id"), obj.getInt("jobId"), obj.getString("workerName"), obj.getString("workerEmail"), obj.getString("status")))
            }
            _applications.value = list
        } else {
            _applications.value = listOf(
                JobApplication(1, 1, "Derrick Tan", "derrick@example.com", "Pending")
            )
        }

        val savedSet = prefs.getStringSet("saved_job_ids", emptySet()) ?: emptySet()
        _savedJobIds.value = savedSet.mapNotNull { it.toIntOrNull() }.toSet()
    }
}