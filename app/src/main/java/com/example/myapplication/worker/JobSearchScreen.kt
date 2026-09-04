package com.example.myapplication.worker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.Job
import com.example.myapplication.JobApplication
import com.example.myapplication.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSearchScreen(viewModel: MainViewModel, navController: NavController) {
    val jobs by viewModel.jobs.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()
    val applications by viewModel.applications.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSalaryRange by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    var showFilters by remember { mutableStateOf(false) }
    var currentBottomTab by remember { mutableIntStateOf(0) }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF)
    val textDark = Color(0xFF1E1B4B)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = softSurface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ) {
                    Column(modifier = Modifier.statusBarsPadding().padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Discovery 🌟", fontSize = 24.sp, fontWeight = FontWeight.Black, color = primaryPurple)
                                Text("Find your dream job", fontSize = 12.sp, color = primaryPurple.copy(alpha = 0.6f))
                            }
                            Surface(
                                onClick = { navController.navigate("worker_profile") },
                                shape = CircleShape,
                                color = primaryPurple,
                                shadowElevation = 4.dp
                            ) {
                                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search...", color = primaryPurple.copy(alpha = 0.4f)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryPurple) },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = primaryPurple)
                                        }
                                    }
                                    IconButton(onClick = { showFilters = !showFilters }) {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = "Filter",
                                            tint = if (showFilters || selectedSalaryRange != "All" || selectedTypeFilter != "All") primaryPurple else Color.Gray
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryPurple,
                                unfocusedBorderColor = Color(0xFFDED9FF),
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = textDark,
                                unfocusedTextColor = textDark
                            )
                        )

                        AnimatedVisibility(visible = showFilters) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Salary Range", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryPurple)
                                Spacer(modifier = Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("All", "< 3k", "3k - 10k", "> 10k").forEach { range ->
                                        item {
                                            FilterChip(
                                                selected = selectedSalaryRange == range,
                                                onClick = { selectedSalaryRange = range },
                                                label = { Text(range) }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Job Type", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryPurple)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("All", "Full-time", "Part-time").forEach { type ->
                                        FilterChip(
                                            selected = selectedTypeFilter == type,
                                            onClick = { selectedTypeFilter = type },
                                            label = { Text(type) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = softSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                ) {
                    listOf(
                        Triple(0, "Jobs", Icons.Default.Work),
                        Triple(1, "Saved", Icons.Default.Bookmark),
                        Triple(2, "My Job", Icons.AutoMirrored.Filled.Assignment),
                        Triple(3, "History", Icons.Default.History)
                    ).forEach { (idx, label, icon) ->
                        NavigationBarItem(
                            selected = currentBottomTab == idx,
                            onClick = { currentBottomTab = idx },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = primaryPurple,
                                indicatorColor = Color(0xFFDED9FF)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
                when (currentBottomTab) {
                    0 -> JobTab(
                        jobs = jobs,
                        applications = applications,
                        savedIds = savedJobIds,
                        query = searchQuery,
                        salaryRange = selectedSalaryRange,
                        selectedType = selectedTypeFilter,
                        viewModel = viewModel,
                        onResetFilters = {
                            searchQuery = ""
                            selectedSalaryRange = "All"
                            selectedTypeFilter = "All"
                        }
                    ) { navController.navigate("job_details/$it/-1") }
                    1 -> SavedTab(jobs, applications, savedJobIds, viewModel) { navController.navigate("job_details/$it/-1") }
                    2 -> AppsTab(applications, jobs, viewModel, showCompletedOnly = false) { navController.navigate("job_details/$it/-1") }
                    3 -> AppsTab(applications, jobs, viewModel, showCompletedOnly = true) { navController.navigate("job_details/$it/-1") }
                }
            }
        }
    }
}

private fun extractNumericSalary(raw: String): Int {
    val clean = raw.replace(",", "").replace(".00", "")
    val match = Regex("""\d+""").find(clean)
    return match?.value?.toIntOrNull() ?: 0
}

@Composable
fun JobTab(
    jobs: List<Job>,
    applications: List<JobApplication>,
    savedIds: Set<Int>,
    query: String,
    salaryRange: String,
    selectedType: String,
    viewModel: MainViewModel,
    onResetFilters: () -> Unit,
    onOpenJob: (Int) -> Unit
) {
    val filledJobIds = applications.filter { it.status == "Approved" || it.status == "Completed" }.map { it.jobId }.toSet()
    val filtered = jobs.filter { job ->
        val notFilled = job.id !in filledJobIds
        val matchSearch = job.title.contains(query, ignoreCase = true) || job.company.contains(query, ignoreCase = true)
        val salaryNum = extractNumericSalary(job.salary)
        val matchSalary = when (salaryRange) {
            "< 3k" -> salaryNum in 1..3000
            "3k - 10k" -> salaryNum in 3001..10000
            "> 10k" -> salaryNum > 10000
            else -> true
        }
        val matchType = if (selectedType == "All") true else job.type.equals(selectedType, ignoreCase = true)
        notFilled && matchSearch && matchSalary && matchType
    }

    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No jobs found 🧸", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onResetFilters,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Reset Filters 🔄", fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filtered) { job -> JobCard(job, savedIds.contains(job.id), false, viewModel, onOpenJob) }
        }
    }
}

@Composable
fun JobCard(job: Job, isSaved: Boolean, isClosed: Boolean = false, viewModel: MainViewModel, onOpenJob: (Int) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Text(job.company, fontSize = 14.sp, color = Color(0xFF7E57C2))
                }
                IconButton(onClick = { viewModel.toggleSaveJob(job.id) }) {
                    Icon(if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (isSaved) Color.Red else Color(0xFF7E57C2))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                    Text(job.salary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(8.dp)) {
                    Text(job.type, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF4338CA), fontSize = 12.sp)
                }
                if (isClosed) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = Color(0xFFFEE2E2), shape = RoundedCornerShape(8.dp)) {
                        Text("Closed 🔒", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onOpenJob(job.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isClosed) Color(0xFF9CA3AF) else Color(0xFF7E57C2))
            ) {
                Text(if (isClosed) "Position Closed 🔒" else "View Details ✨", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SavedTab(jobs: List<Job>, applications: List<JobApplication>, savedIds: Set<Int>, viewModel: MainViewModel, onOpenJob: (Int) -> Unit) {
    val filledJobIds = applications.filter { it.status == "Approved" || it.status == "Completed" }.map { it.jobId }.toSet()
    val saved = jobs.filter { savedIds.contains(it.id) }
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Saved for Later 💖", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        if (saved.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Your list is empty 🧺", color = Color.White.copy(alpha = 0.7f)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(saved) { job ->
                    val isClosed = job.id in filledJobIds
                    JobCard(job, true, isClosed, viewModel, onOpenJob)
                }
            }
        }
    }
}

@Composable
fun AppsTab(
    apps: List<JobApplication>,
    jobs: List<Job>,
    viewModel: MainViewModel,
    showCompletedOnly: Boolean = false,
    onOpenJob: (Int) -> Unit
) {
    val myAppsAll = apps.filter { it.workerEmail == viewModel.currentUser?.email }
    val myApps = if (showCompletedOnly) myAppsAll.filter { it.status == "Completed" } else myAppsAll
    var applicationToCancel by remember { mutableStateOf<JobApplication?>(null) }

    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(if (showCompletedOnly) "Completed History 📜" else "My Applications 🌈", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        if (myApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(if (showCompletedOnly) "No completed jobs yet 🎈" else "No applications yet 🎈", color = Color.White.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(myApps) { app ->
                    val job = jobs.find { it.id == app.jobId }
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenJob(app.jobId) }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(job?.title ?: "Job", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text(job?.company ?: "Company", fontSize = 12.sp, color = Color(0xFF7E57C2))
                                if (app.message.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Note: “${app.message}”", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color(0xFF6B7280), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Surface(color = when(app.status) { "Approved", "Completed" -> Color(0xFFDCFCE7); "Rejected" -> Color(0xFFFEE2E2); else -> Color(0xFFFEF9C3) }, shape = CircleShape) {
                                    Text(app.status, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = when(app.status) { "Approved", "Completed" -> Color(0xFF16A34A); "Rejected" -> Color.Red; else -> Color(0xFF854D0E) })
                                }
                                if (app.status == "Pending") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Withdraw", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { applicationToCancel = app }.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    applicationToCancel?.let { targetApp ->
        AlertDialog(
            onDismissRequest = { applicationToCancel = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = { Text("Withdraw Application? ⚠️", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B)) },
            text = { Text("Are you sure you want to cancel your job application?", color = Color(0xFF4B5563)) },
            confirmButton = {
                Button(onClick = { viewModel.cancelApplication(targetApp.id); applicationToCancel = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))) { Text("Confirm Withdraw", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { applicationToCancel = null }) { Text("Keep Application", color = Color.Gray) } }
        )
    }
}