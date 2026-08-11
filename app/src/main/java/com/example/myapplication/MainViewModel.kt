package com.example.myapplication

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class UserAccount(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Worker",
    val savedJobs: List<Int> = emptyList()
)

data class Job(
    val id: Int = 0,
    val title: String = "",
    val company: String = "",
    val salary: String = "",
    val description: String = "",
    val postedByEmail: String = "admin@jobboom.com"
)

data class JobApplication(
    val id: Int = 0,
    val jobId: Int = 0,
    val workerName: String = "",
    val workerEmail: String = "",
    var status: String = "Pending"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()

    var currentUser by mutableStateOf<UserAccount?>(null)
        private set

    init {
        observeJobs()
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // 这里使用监听器来保持状态同步
            setupUserListener(firebaseUser.email ?: "")
        }
    }

    private fun setupUserListener(email: String) {
        db.collection("users").document(email).addSnapshotListener { doc, _ ->
            val user = doc?.toObject(UserAccount::class.java)
            if (user != null) {
                currentUser = user
                _savedJobIds.value = user.savedJobs.toSet()
                observeApplications(user)
            }
        }
    }

    private fun observeJobs() {
        db.collection("jobs").addSnapshotListener { value, _ ->
            val jobList = value?.toObjects(Job::class.java) ?: emptyList()
            if (jobList.isEmpty()) seedInitialJobs() else _jobs.value = jobList
        }
    }

    private fun observeApplications(user: UserAccount?) {
        if (user == null) return
        if (user.role == "Worker") {
            db.collection("applications")
                .whereEqualTo("workerEmail", user.email)
                .addSnapshotListener { value, _ ->
                    _applications.value = value?.toObjects(JobApplication::class.java) ?: emptyList()
                }
        } else {
            db.collection("applications").addSnapshotListener { value, _ ->
                _applications.value = value?.toObjects(JobApplication::class.java) ?: emptyList()
            }
        }
    }

    private fun seedInitialJobs() {
        val initialJobs = listOf(
            Job(1, "Software Engineer", "Google", "$150,000", "Full-time Dev"),
            Job(2, "Product Manager", "Meta", "$140,000", "Full-time PM"),
            Job(3, "UI/UX Designer", "Apple", "$130,000", "Part-time Design"),
            Job(4, "Delivery Helper", "GreenGro", "RM 15/Hr", "Flexible hours"),
            Job(5, "Cashier", "Fresh Market", "RM 12/Hr", "Retail shift")
        )
        initialJobs.forEach { db.collection("jobs").document(it.id.toString()).set(it) }
    }

    suspend fun checkUserExists(email: String): UserAccount? {
        return try {
            val doc = db.collection("users").document(email.trim()).get().await()
            doc.toObject(UserAccount::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registerUser(name: String, email: String, password: String, role: String): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email.trim(), password.trim()).await()
            val newUser = UserAccount(name.trim(), email.trim(), "", role)
            db.collection("users").document(email.trim()).set(newUser).await()
            currentUser = newUser
            setupUserListener(email.trim())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(emailInput: String, passwordInput: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(emailInput.trim(), passwordInput.trim()).await()
            // 确保在返回前获取到用户信息
            val doc = db.collection("users").document(emailInput.trim()).get().await()
            val user = doc.toObject(UserAccount::class.java)
            if (user != null) {
                currentUser = user
                setupUserListener(emailInput.trim())
                Result.success(Unit)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun applyForJob(jobId: Int): Boolean {
        val user = currentUser ?: return false
        val newAppId = (applications.value.maxOfOrNull { it.id } ?: 0) + (System.currentTimeMillis() % 10000).toInt()
        val newApp = JobApplication(
            id = newAppId,
            jobId = jobId,
            workerName = user.name,
            workerEmail = user.email,
            status = "Pending"
        )
        db.collection("applications").document(newAppId.toString()).set(newApp)
        return true
    }

    fun toggleSaveJob(jobId: Int) {
        val user = currentUser ?: return
        val currentSet = _savedJobIds.value
        val newSet = if (currentSet.contains(jobId)) currentSet - jobId else currentSet + jobId
        _savedJobIds.value = newSet
        db.collection("users").document(user.email).update("savedJobs", newSet.toList())
    }

    fun updateApplicationStatus(appId: Int, newStatus: String) {
        db.collection("applications").document(appId.toString()).update("status", newStatus)
    }

    fun logout() {
        auth.signOut()
        currentUser = null
        _applications.value = emptyList()
        _savedJobIds.value = emptySet()
    }

    suspend fun updateUserProfile(newName: String, newEmail: String, newPhone: String, newPassword: String) {
        val user = currentUser ?: return
        val updatedUser = user.copy(name = newName, phone = newPhone)
        try {
            db.collection("users").document(user.email).set(updatedUser).await()
            currentUser = updatedUser
            if (newPassword.isNotBlank()) {
                auth.currentUser?.updatePassword(newPassword)?.await()
            }
        } catch (e: Exception) {
            // Error handling
        }
    }

    fun updateUserRole(newRole: String) {
        val user = currentUser ?: return
        db.collection("users").document(user.email).update("role", newRole)
    }

    fun postJob(title: String, company: String, salary: String, description: String) {
        val newId = (_jobs.value.maxOfOrNull { it.id } ?: 0) + 1
        val newJob = Job(newId, title, company, salary, description, currentUser?.email ?: "")
        db.collection("jobs").document(newId.toString()).set(newJob)
    }
}
