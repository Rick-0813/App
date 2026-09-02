package com.example.myapplication.employer

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
import com.example.myapplication.ReviewDirection
import com.example.myapplication.myfeature.EmployerReviewWorkerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerScreen(viewModel: MainViewModel, navController: NavController) {
    val applications by viewModel.applications.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    var currentBottomTab by remember { mutableIntStateOf(0) }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF) // 统一浅紫色

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = softSurface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    TopAppBar(
                        title = { Text("Boss Panel 👑", fontWeight = FontWeight.Black, color = primaryPurple) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        actions = {
                            IconButton(onClick = { navController.navigate("company_details") }) {
                                Icon(Icons.Default.Business, contentDescription = "Company Details", tint = primaryPurple)
                            }

                            IconButton(onClick = { navController.navigate("worker_profile") }) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = primaryPurple)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = softSurface,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                ) {
                    NavigationBarItem(
                        selected = currentBottomTab == 0,
                        onClick = { currentBottomTab = 0 },
                        icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
                        label = { Text("Apps", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, indicatorColor = Color(0xFFDED9FF))
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == 1,
                        onClick = { currentBottomTab = 1 },
                        icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                        label = { Text("Post", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, indicatorColor = Color(0xFFDED9FF))
                    )
                    NavigationBarItem(
                        selected = currentBottomTab == 2,
                        onClick = { currentBottomTab = 2 },
                        icon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                        label = { Text("Reviews", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, indicatorColor = Color(0xFFDED9FF))
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                when (currentBottomTab) {
                    0 -> ManageAppsTab(applications, jobs, viewModel) { jobId, applicationId ->
                        navController.navigate("job_details/$jobId/$applicationId")
                    }
                    1 -> PostJobTab(viewModel)
                    2 -> ManageAppsTab(
                        apps = applications,
                        jobs = jobs,
                        viewModel = viewModel,
                        title = "Review Workers ⭐",
                        completedOnly = true,
                        onOpenJob = { jobId, applicationId ->
                            navController.navigate("job_details/$jobId/$applicationId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageAppsTab(
    apps: List<com.example.myapplication.JobApplication>,
    jobs: List<com.example.myapplication.Job>,
    viewModel: MainViewModel,
    title: String = "Pending Talents ✨",
    completedOnly: Boolean = false,
    onOpenJob: (Int, Int) -> Unit
) {
    val reviews by viewModel.reviews.collectAsState()
    val visibleApps = if (completedOnly) apps.filter { it.status == "Completed" } else apps
    var reviewApplication by remember {
        mutableStateOf<com.example.myapplication.JobApplication?>(null)
    }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
<<<<<<< Updated upstream
        
        if (visibleApps.isEmpty()) {
=======

        if (apps.isEmpty()) {
>>>>>>> Stashed changes
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (completedOnly) "No completed workers to review yet" else "No applications yet 🧸",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(visibleApps) { app ->
                    val jobTitle = jobs.find { it.id == app.jobId }?.title ?: "Job"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = app.workerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                            Text(text = "Wants to be a $jobTitle", fontSize = 14.sp, color = Color(0xFF7E57C2))

                            Spacer(modifier = Modifier.height(16.dp))

                            if (app.status == "Pending") {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { viewModel.updateApplicationStatus(app.id, "Approved") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Approve ✅") }

                                    Button(
                                        onClick = { viewModel.updateApplicationStatus(app.id, "Rejected") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Decline ❌") }
                                }
                            } else {
                                Surface(
                                    color = when (app.status) {
                                        "Approved", "Completed" -> Color(0xFFDCFCE7)
                                        else -> Color(0xFFFEE2E2)
                                    },
                                    shape = CircleShape
                                ) {
                                    Text(
                                        app.status,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = if (app.status == "Approved" || app.status == "Completed") Color(0xFF16A34A) else Color(0xFFEF4444)
                                    )
                                }

                                if (app.status == "Approved") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { viewModel.updateApplicationStatus(app.id, "Completed") },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Mark Job Completed ✅", fontWeight = FontWeight.Bold) }
                                }

                                if (app.status == "Completed") {
                                    val alreadyReviewed = reviews.any {
                                        it.applicationId == app.id &&
                                            it.direction == ReviewDirection.EMPLOYER_TO_WORKER
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { reviewApplication = app },
                                        enabled = !alreadyReviewed,
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            if (alreadyReviewed) "Worker Review Submitted ✅" else "Review Worker ⭐",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onOpenJob(app.jobId, app.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("View Job Details", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    reviewApplication?.let { application ->
        jobs.find { it.id == application.jobId }?.let { job ->
            EmployerReviewWorkerDialog(
                viewModel = viewModel,
                job = job,
                application = application,
                onDismiss = { reviewApplication = null }
            )
        }
    }
}

@Composable
fun PostJobTab(viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    val primaryPurple = Color(0xFF7E57C2)

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Create magic post 🪄", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                if (showSuccess) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Job Posted Successfully! 🎉",
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showSuccess = false },
                    label = { Text("Job Title (e.g. Software Engineer)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it; showSuccess = false },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = salary,
                    onValueChange = { salary = it; showSuccess = false },
                    label = { Text("Salary (e.g. RM 4,500 / Month)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; showSuccess = false },
                    label = { Text("Job Description & Requirements") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && company.isNotBlank() && salary.isNotBlank()) {
                            viewModel.postJob(title, company, salary, description)
                            title = ""
                            company = ""
                            salary = ""
                            description = ""
                            showSuccess = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                    enabled = title.isNotBlank() && company.isNotBlank()
                ) {
                    Text("Publish Now 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}