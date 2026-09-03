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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val description: String = "",
    val type: String = "Full-time",
    val employerEmail: String = "",
    val requirements: String = ""
)

data class JobApplication(
    val id: Int = 0,
    val jobId: Int = 0,
    val workerName: String = "",
    val workerEmail: String = "",
    val message: String = "",
    var status: String = "Pending"
)

enum class ReviewDirection {
    WORKER_TO_COMPANY,
    EMPLOYER_TO_WORKER
}

data class JobReview(
    val id: Int = 0,
    val jobId: Int = 0,
    val applicationId: Int = 0,
    val direction: ReviewDirection = ReviewDirection.WORKER_TO_COMPANY,
    val reviewerName: String = "",
    val subjectName: String = "",
    val subjectKey: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val date: String = "",
    val skillBadge: String = ""
)

internal fun calculateAverageRating(ratings: List<Int>): Float =
    if (ratings.isEmpty()) 0f else ratings.sum().toFloat() / ratings.size

internal fun normalizedCompanyKey(companyName: String): String =
    "company:${companyName.trim().lowercase(Locale.ROOT)}"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = getApplication<Application>().getSharedPreferences("jobboom_local_data", Context.MODE_PRIVATE)

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    private val _applications = MutableStateFlow<List<JobApplication>>(emptyList())
    val applications: StateFlow<List<JobApplication>> = _applications.asStateFlow()

    private val _savedJobIds = MutableStateFlow<Set<Int>>(emptySet())
    val savedJobIds: StateFlow<Set<Int>> = _savedJobIds.asStateFlow()

    private val _reviews = MutableStateFlow<List<JobReview>>(emptyList())
    val reviews: StateFlow<List<JobReview>> = _reviews.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserAccount>>(emptyList())

    var currentUser by mutableStateOf<UserAccount?>(null)
        private set

    init {
        loadDataFromLocal()
    }

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
            val newUser = UserAccount(name, email, "", "G-AUTH-PASS", "Worker")
            _allUsers.value = _allUsers.value + newUser
            currentUser = newUser
            saveAllDataToLocal()
            Log.d(TAG, "Google Register+Login successful")
            Result.success(Unit)
        }
    }

    fun applyForJob(jobId: Int, message: String = ""): Boolean {
        val user = currentUser ?: return false
        if (user.role != "Worker") return false
        if (_applications.value.any { it.jobId == jobId && it.workerEmail == user.email && it.status != "Rejected" }) {
            return false
        }
        val newApp = JobApplication(
            id = (System.currentTimeMillis() % 100000).toInt(),
            jobId = jobId,
            workerName = user.name,
            workerEmail = user.email,
            message = message.trim(),
            status = "Pending"
        )
        _applications.value = _applications.value + newApp
        saveAllDataToLocal()
        return true
    }

    fun cancelApplication(applicationId: Int): Boolean {
        val target = _applications.value.find { it.id == applicationId } ?: return false
        if (target.status != "Pending") return false
        _applications.value = _applications.value.filter { it.id != applicationId }
        saveAllDataToLocal()
        return true
    }

    fun applicationForCurrentWorker(jobId: Int): JobApplication? {
        val email = currentUser?.email ?: return null
        return _applications.value
            .filter { it.jobId == jobId && it.workerEmail == email }
            .maxByOrNull { it.id }
    }

    fun completedApplicationsForJob(jobId: Int): List<JobApplication> =
        _applications.value.filter { it.jobId == jobId && it.status == "Completed" }

    fun hasSubmittedReview(applicationId: Int, direction: ReviewDirection): Boolean =
        _reviews.value.any { it.applicationId == applicationId && it.direction == direction }

    // 级联注销账号（只保留这一个）
    fun deleteCurrentUserAccount() {
        val user = currentUser ?: return
        val userEmail = user.email

        _allUsers.value = _allUsers.value.filter { it.email != userEmail }
        _applications.value = _applications.value.filter { it.workerEmail != userEmail }

        val myJobIds = _jobs.value.filter { it.employerEmail.equals(userEmail, ignoreCase = true) }.map { it.id }.toSet()
        if (myJobIds.isNotEmpty()) {
            _jobs.value = _jobs.value.filter { it.id !in myJobIds }
            _applications.value = _applications.value.filter { it.jobId !in myJobIds }
        }

        currentUser = null
        _savedJobIds.value = emptySet()
        saveAllDataToLocal()
    }

    fun submitReview(
        jobId: Int,
        applicationId: Int,
        rating: Int,
        comment: String,
        skillBadge: String = ""
    ): Boolean {
        val user = currentUser ?: return false
        val job = _jobs.value.find { it.id == jobId } ?: return false
        val application = _applications.value.find { it.id == applicationId && it.jobId == jobId } ?: return false
        if (application.status != "Completed" || comment.isBlank()) return false

        val direction = if (user.role == "Employer") {
            ReviewDirection.EMPLOYER_TO_WORKER
        } else {
            if (application.workerEmail != user.email) return false
            ReviewDirection.WORKER_TO_COMPANY
        }
        if (hasSubmittedReview(applicationId, direction)) return false

        val review = JobReview(
            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
            jobId = jobId,
            applicationId = applicationId,
            direction = direction,
            reviewerName = user.name,
            subjectName = if (direction == ReviewDirection.WORKER_TO_COMPANY) job.company else application.workerName,
            subjectKey = if (direction == ReviewDirection.WORKER_TO_COMPANY) {
                normalizedCompanyKey(job.company)
            } else {
                application.workerEmail
            },
            rating = rating.coerceIn(1, 5),
            comment = comment.trim(),
            date = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date()),
            skillBadge = if (direction == ReviewDirection.EMPLOYER_TO_WORKER) skillBadge else ""
        )
        _reviews.value = listOf(review) + _reviews.value
        saveAllDataToLocal()
        return true
    }

    fun companyReviews(companyName: String): List<JobReview> =
        _reviews.value.filter { review ->
            review.direction == ReviewDirection.WORKER_TO_COMPANY &&
                    (review.subjectKey == normalizedCompanyKey(companyName) ||
                            review.subjectName.trim().equals(companyName.trim(), ignoreCase = true))
        }

    fun companyRating(companyName: String): Float =
        calculateAverageRating(companyReviews(companyName).map { it.rating })

    fun companyReviewCount(companyName: String): Int = companyReviews(companyName).size

    fun workerRating(workerEmail: String): Float {
        val values = _reviews.value.filter {
            it.subjectKey == workerEmail && it.direction == ReviewDirection.EMPLOYER_TO_WORKER
        }
        return if (values.isEmpty()) 0f else values.map { it.rating }.average().toFloat()
    }

    fun skillBadgesForWorker(workerEmail: String): List<String> =
        _reviews.value
            .filter { it.subjectKey == workerEmail && it.direction == ReviewDirection.EMPLOYER_TO_WORKER }
            .map { it.skillBadge }
            .filter { it.isNotBlank() }
            .distinct()

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

    fun postJob(title: String, company: String, salary: String, description: String, type: String, requirements: String) {
        val userEmail = currentUser?.email ?: ""
        val newJob = Job(
            id = (_jobs.value.maxOfOrNull { it.id } ?: 0) + 1,
            title = title,
            company = company,
            salary = salary,
            description = description,
            requirements = requirements,
            type = type,
            employerEmail = userEmail
        )
        _jobs.value = _jobs.value + newJob
        saveAllDataToLocal()
    }

    fun updateJob(jobId: Int, newTitle: String, newSalary: String, newDescription: String, newType: String, newRequirements: String) {
        _jobs.value = _jobs.value.map { job ->
            if (job.id == jobId) {
                job.copy(
                    title = newTitle.trim(),
                    salary = newSalary.trim(),
                    description = newDescription.trim(),
                    type = newType.trim(),
                    requirements = newRequirements.trim()
                )
            } else job
        }
        saveAllDataToLocal()
    }

    fun deleteJob(jobId: Int) {
        _jobs.value = _jobs.value.filter { it.id != jobId }
        _applications.value = _applications.value.filter { it.jobId != jobId }
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
            obj.put("requirements", j.requirements)
            obj.put("type", j.type)
            obj.put("employerEmail", j.employerEmail)
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
            obj.put("message", a.message)
            obj.put("status", a.status)
            appsArr.put(obj)
        }
        editor.putString("all_apps", appsArr.toString())

        val reviewsArr = JSONArray()
        _reviews.value.forEach { review ->
            val obj = JSONObject()
            obj.put("id", review.id)
            obj.put("jobId", review.jobId)
            obj.put("applicationId", review.applicationId)
            obj.put("direction", review.direction.name)
            obj.put("reviewerName", review.reviewerName)
            obj.put("subjectName", review.subjectName)
            obj.put("subjectKey", review.subjectKey)
            obj.put("rating", review.rating)
            obj.put("comment", review.comment)
            obj.put("date", review.date)
            obj.put("skillBadge", review.skillBadge)
            reviewsArr.put(obj)
        }
        editor.putString("all_reviews", reviewsArr.toString())

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
                list.add(Job(
                    id = obj.getInt("id"),
                    title = obj.getString("title"),
                    company = obj.getString("company"),
                    salary = obj.getString("salary"),
                    description = obj.getString("description"),
                    requirements = obj.optString("requirements", ""),
                    type = obj.optString("type", "Full-time"),
                    employerEmail = obj.optString("employerEmail", "")
                ))
            }
            _jobs.value = list
        } else {
            _jobs.value = listOf(
                Job(
                    id = 1,
                    title = "Software Engineer",
                    company = "Google",
                    salary = "$150,000",
                    description = "Build cutting-edge mobile apps using Jetpack Compose and Kotlin.",
                    requirements = "Proficient in Kotlin and Jetpack Compose\nExperience with Git version control\nStrong problem-solving and algorithmic thinking",
                    type = "Full-time",
                    employerEmail = "derrick.t@gmail.com"
                ),
                Job(
                    id = 2,
                    title = "Product Manager",
                    company = "Meta",
                    salary = "$140,000",
                    description = "Lead multidisciplinary product teams and drive product delivery.",
                    requirements = "Experience with Agile and Scrum methodologies\nProven track record in roadmap definition\nExcellent interpersonal and presentation skills",
                    type = "Full-time",
                    employerEmail = "derrick.t@gmail.com"
                ),
                Job(
                    id = 3,
                    title = "UI/UX Designer",
                    company = "Apple",
                    salary = "$130,000",
                    description = "Design intuitive user interfaces and polished design systems.",
                    requirements = "Proficiency with Figma and modern wireframing tools\nPortfolio showcasing clean mobile application UX\nStrong eye for typography, layouts, and accessibility",
                    type = "Part-time",
                    employerEmail = "derrick.t@gmail.com"
                ),
                Job(
                    id = 4,
                    title = "Delivery Helper",
                    company = "GreenGro",
                    salary = "RM 2,500.00",
                    description = "Assist drivers with daily grocery load distribution and drop-offs.",
                    requirements = "Punctual, physically fit, and dependable\nPossess a valid B2 motorcycle license\nFriendly customer service attitude",
                    type = "Part-time",
                    employerEmail = "jobboom.pro@gmail.com"
                ),
                Job(
                    id = 5,
                    title = "Cashier",
                    company = "Fresh Market",
                    salary = "RM 1,800.00",
                    description = "Handle point-of-sale checkout and manage cashier drawer reconciliations.",
                    requirements = "Basic numerical literacy and mental math\nHonest, disciplined, and customer-oriented\nComfortable working rotating weekend shifts",
                    type = "Part-time",
                    employerEmail = "jobboom.pro@gmail.com"
                )
            )
        }

        val appsStr = prefs.getString("all_apps", null)
        if (!appsStr.isNullOrEmpty()) {
            val list = mutableListOf<JobApplication>()
            val arr = JSONArray(appsStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    JobApplication(
                        id = obj.getInt("id"),
                        jobId = obj.getInt("jobId"),
                        workerName = obj.getString("workerName"),
                        workerEmail = obj.getString("workerEmail"),
                        message = obj.optString("message", ""),
                        status = obj.getString("status")
                    )
                )
            }
            _applications.value = list.ifEmpty {
                listOf(JobApplication(100, 4, "Derrick Tan", "derrick@test.com", "I have relevant experience and am available immediately.", "Completed"))
            }
        } else {
            _applications.value = listOf(
                JobApplication(100, 4, "Derrick Tan", "derrick@test.com", "I have relevant experience and am available immediately.", "Completed")
            )
        }

        val reviewsStr = prefs.getString("all_reviews", null)
        if (!reviewsStr.isNullOrEmpty()) {
            val list = mutableListOf<JobReview>()
            val arr = JSONArray(reviewsStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val direction = runCatching {
                    ReviewDirection.valueOf(obj.getString("direction"))
                }.getOrDefault(ReviewDirection.WORKER_TO_COMPANY)
                list.add(
                    JobReview(
                        id = obj.getInt("id"),
                        jobId = obj.getInt("jobId"),
                        applicationId = obj.getInt("applicationId"),
                        direction = direction,
                        reviewerName = obj.getString("reviewerName"),
                        subjectName = obj.getString("subjectName"),
                        subjectKey = obj.getString("subjectKey"),
                        rating = obj.getInt("rating"),
                        comment = obj.getString("comment"),
                        date = obj.optString("date"),
                        skillBadge = obj.optString("skillBadge")
                    )
                )
            }
            _reviews.value = list
        }
    }
}