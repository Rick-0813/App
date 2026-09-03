package com.example.myapplication.employer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
import com.example.myapplication.ReviewDirection
import com.example.myapplication.myfeature.EmployerReviewWorkerDialog
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerScreen(viewModel: MainViewModel, navController: NavController) {
    val applications by viewModel.applications.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    var currentBottomTab by remember { mutableIntStateOf(0) }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    val currentBossEmail = viewModel.currentUser?.email ?: ""
    val myPostedJobIds = remember(jobs, currentBossEmail) {
        jobs.filter { it.employerEmail.equals(currentBossEmail, ignoreCase = true) }
            .map { it.id }
            .toSet()
    }
    val myApplications = remember(applications, myPostedJobIds) {
        applications.filter { it.jobId in myPostedJobIds }
    }

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
                // 🚀 底部导航栏扩展为 4 个 Tab：Apps, Post, Reviews, History
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
                    NavigationBarItem(
                        selected = currentBottomTab == 3,
                        onClick = { currentBottomTab = 3 },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("History", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = primaryPurple, indicatorColor = Color(0xFFDED9FF))
                    )
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                when (currentBottomTab) {
                    0 -> ManageAppsTab(myApplications, jobs, viewModel) { jobId, applicationId ->
                        navController.navigate("job_details/$jobId/$applicationId")
                    }
                    1 -> PostJobTab(viewModel)
                    2 -> ManageAppsTab(
                        apps = myApplications,
                        jobs = jobs,
                        viewModel = viewModel,
                        title = "Review Workers ⭐",
                        completedOnly = true,
                        onOpenJob = { jobId, applicationId ->
                            navController.navigate("job_details/$jobId/$applicationId")
                        }
                    )
                    3 -> BossHistoryTab(jobs = jobs, applications = myApplications, viewModel = viewModel) { jobId, applicationId ->
                        navController.navigate("job_details/$jobId/$applicationId")
                    }
                }
            }
        }
    }
}

// 🚀 新增：Boss 端专属的 History 页面（按公司归类展示自己发布过和已完成的工作）
@Composable
fun BossHistoryTab(
    jobs: List<com.example.myapplication.Job>,
    applications: List<com.example.myapplication.JobApplication>,
    viewModel: MainViewModel,
    onOpenJob: (Int, Int) -> Unit
) {
    val currentBossEmail = viewModel.currentUser?.email ?: ""
    val myJobs = jobs.filter { it.employerEmail.equals(currentBossEmail, ignoreCase = true) }

    // 按照公司名称进行分组
    val groupedByCompany = myJobs.groupBy { it.company.ifBlank { "My Company" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Employer History 📜", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text("All your posted jobs and completed records grouped by company", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
        Spacer(modifier = Modifier.height(16.dp))

        if (myJobs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                Text("No job history yet 🧸", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
            }
        } else {
            groupedByCompany.forEach { (companyName, companyJobs) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = companyName, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFE0E7FF))
                        Spacer(modifier = Modifier.height(12.dp))

                        companyJobs.forEach { job ->
                            // 查找该职位对应的申请记录
                            val jobApps = applications.filter { it.jobId == job.id }
                            val completedApp = jobApps.find { it.status == "Completed" }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable {
                                        onOpenJob(job.id, completedApp?.id ?: jobApps.firstOrNull()?.id ?: -1)
                                    },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = job.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E1B4B))
                                    Text(text = "${job.type} • ${job.salary}", fontSize = 12.sp, color = Color.Gray)
                                }

                                Surface(
                                    color = if (completedApp != null) Color(0xFFDCFCE7) else Color(0xFFE0E7FF),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (completedApp != null) "Completed ⭐" else "${jobApps.size} Applicants",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (completedApp != null) Color(0xFF16A34A) else Color(0xFF4338CA)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
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

        if (visibleApps.isEmpty()) {
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
                    val workerRating = viewModel.workerRating(app.workerEmail)
                    val badges = viewModel.skillBadgesForWorker(app.workerEmail)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = app.workerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (workerRating == 0f) "New Worker" else String.format(Locale.US, "%.1f", workerRating),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1B4B)
                                    )
                                }
                            }

                            Text(text = "Wants to be a $jobTitle", fontSize = 14.sp, color = Color(0xFF7E57C2))

                            if (badges.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(badges) { badge ->
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                text = "$badge ✓",
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                }
                            }

                            if (app.message.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDED9FF))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Worker Message:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E57C2))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "“${app.message}”",
                                            fontSize = 13.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = Color(0xFF374151)
                                        )
                                    }
                                }
                            }

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
    var requirements by remember { mutableStateOf("") }
    val jobs by viewModel.jobs.collectAsState()
    val currentUser = viewModel.currentUser

    var title by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var rawSalary by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("Full-time") }
    var showSuccess by remember { mutableStateOf(false) }

    var jobToDelete by remember { mutableStateOf<com.example.myapplication.Job?>(null) }
    var jobToEdit by remember { mutableStateOf<com.example.myapplication.Job?>(null) }

    val primaryPurple = Color(0xFF7E57C2)
    val textDark = Color(0xFF1E1B4B)
    val myPostedJobs = jobs.filter { it.employerEmail == currentUser?.email }

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = jobType == "Full-time",
                        onClick = { jobType = "Full-time" },
                        label = { Text("Full-time") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryPurple, selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = jobType == "Part-time",
                        onClick = { jobType = "Part-time" },
                        label = { Text("Part-time") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryPurple, selectedLabelColor = Color.White)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; showSuccess = false },
                    label = { Text("Job Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it; showSuccess = false },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawSalary,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            rawSalary = input
                            showSuccess = false
                        }
                    },
                    label = { Text("Salary Amount (Numbers Only)") },
                    leadingIcon = { Text("RM ", modifier = Modifier.padding(start = 16.dp), fontWeight = FontWeight.Bold) },
                    trailingIcon = { Text(".00", modifier = Modifier.padding(end = 16.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it; showSuccess = false },
                    label = { Text("Job Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                )

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it; showSuccess = false },
                    label = { Text("Job Requirements (Enter each on a new line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                )

                Button(
                    onClick = {
                        if (title.isNotBlank() && company.isNotBlank() && rawSalary.isNotBlank()) {
                            val formattedSalary = "RM $rawSalary.00"
                            viewModel.postJob(title, company, formattedSalary, description, jobType, requirements)
                            title = ""
                            company = ""
                            rawSalary = ""
                            description = ""
                            requirements = ""
                            showSuccess = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                    enabled = title.isNotBlank() && company.isNotBlank() && rawSalary.isNotBlank()
                ) {
                    Text("Publish Now 🚀", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }

        if (myPostedJobs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Manage Active Listings", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            myPostedJobs.forEach { job ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(job.title, fontWeight = FontWeight.Bold, color = textDark)
                            Text("${job.type} • ${job.salary}", fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { jobToEdit = job }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Job", tint = primaryPurple)
                            }
                            IconButton(onClick = { jobToDelete = job }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    jobToDelete?.let { targetJob ->
        AlertDialog(
            onDismissRequest = { jobToDelete = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Delete Listing? 🗑️", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text("Are you sure you want to remove \"${targetJob.title}\"?", color = Color(0xFF4B5563)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteJob(targetJob.id)
                        jobToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { jobToDelete = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    jobToEdit?.let { targetJob ->
        var editTitle by remember(targetJob) { mutableStateOf(targetJob.title) }
        var editSalary by remember(targetJob) { mutableStateOf(targetJob.salary) }
        var editDesc by remember(targetJob) { mutableStateOf(targetJob.description) }
        var editType by remember(targetJob) { mutableStateOf(targetJob.type) }
        var editReqs by remember(targetJob) { mutableStateOf(targetJob.requirements) }

        AlertDialog(
            onDismissRequest = { jobToEdit = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Edit Job Listing ✏️", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = editType == "Full-time", onClick = { editType = "Full-time" }, label = { Text("Full-time") })
                        FilterChip(selected = editType == "Part-time", onClick = { editType = "Part-time" }, label = { Text("Part-time") })
                    }
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Job Title") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                    OutlinedTextField(
                        value = editSalary,
                        onValueChange = { editSalary = it },
                        label = { Text("Salary") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Job Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                    OutlinedTextField(
                        value = editReqs,
                        onValueChange = { editReqs = it },
                        label = { Text("Requirements (each on new line)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateJob(targetJob.id, editTitle, editSalary, editDesc, editType, editReqs)
                        jobToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) { Text("Save Changes") }
            },
            dismissButton = {
                TextButton(onClick = { jobToEdit = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }
}