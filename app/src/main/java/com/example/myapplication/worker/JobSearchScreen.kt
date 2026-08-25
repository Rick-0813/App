package com.example.myapplication.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobSearchScreen(viewModel: MainViewModel, navController: NavController) {
    val jobs by viewModel.jobs.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()
    val applications by viewModel.applications.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var currentBottomTab by remember { mutableIntStateOf(0) }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF) // 统一的浅紫表面色
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
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryPurple,
                                unfocusedBorderColor = Color(0xFFDED9FF),
                                focusedContainerColor = Color.White.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.5f),
                                focusedTextColor = textDark
                            )
                        )
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
                        Triple(2, "Apps", Icons.AutoMirrored.Filled.Assignment)
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
                    0 -> JobTab(jobs, savedJobIds, searchQuery, viewModel) { jobId ->
                        navController.navigate("job_details/$jobId/-1")
                    }
                    1 -> SavedTab(jobs, savedJobIds, viewModel) { jobId ->
                        navController.navigate("job_details/$jobId/-1")
                    }
                    2 -> AppsTab(applications, jobs, viewModel) { jobId ->
                        navController.navigate("job_details/$jobId/-1")
                    }
                }
            }
        }
    }
}

@Composable
fun JobTab(
    jobs: List<com.example.myapplication.Job>,
    savedIds: Set<Int>,
    query: String,
    viewModel: MainViewModel,
    onOpenJob: (Int) -> Unit
) {
    val filtered = jobs.filter { it.title.contains(query, true) || it.company.contains(query, true) }
    
    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No jobs found 🧸", color = Color.White, fontWeight = FontWeight.Bold)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filtered) { job ->
                JobCard(job, savedIds.contains(job.id), viewModel, onOpenJob)
            }
        }
    }
}

@Composable
fun JobCard(
    job: com.example.myapplication.Job,
    isSaved: Boolean,
    viewModel: MainViewModel,
    onOpenJob: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)), // 浅紫色卡片
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                    Text(job.company, fontSize = 14.sp, color = Color(0xFF7E57C2))
                }
                IconButton(onClick = { viewModel.toggleSaveJob(job.id) }) {
                    Icon(
                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isSaved) Color.Red else Color(0xFF7E57C2)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(8.dp)) {
                    Text(job.salary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(color = Color(0xFFE0E7FF), shape = RoundedCornerShape(8.dp)) {
                    Text("Full-time", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF4338CA), fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onOpenJob(job.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Text("View Details ✨", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SavedTab(
    jobs: List<com.example.myapplication.Job>,
    savedIds: Set<Int>,
    viewModel: MainViewModel,
    onOpenJob: (Int) -> Unit
) {
    val saved = jobs.filter { savedIds.contains(it.id) }
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("Saved for Later 💖", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        if (saved.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your list is empty 🧺", color = Color.White.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(saved) { job -> JobCard(job, true, viewModel, onOpenJob) }
            }
        }
    }
}

@Composable
fun AppsTab(
    apps: List<com.example.myapplication.JobApplication>,
    jobs: List<com.example.myapplication.Job>,
    viewModel: MainViewModel,
    onOpenJob: (Int) -> Unit
) {
    val myApps = apps.filter { it.workerEmail == viewModel.currentUser?.email }
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text("My Journey 🌈", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        if (myApps.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No applications yet 🎈", color = Color.White.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(myApps) { app ->
                    val job = jobs.find { it.id == app.jobId }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenJob(app.jobId) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(job?.title ?: "Job", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                                Text(job?.company ?: "Company", fontSize = 12.sp, color = Color(0xFF7E57C2))
                            }
                            Surface(
                                color = when(app.status) {
                                    "Approved", "Completed" -> Color(0xFFDCFCE7)
                                    "Rejected" -> Color(0xFFFEE2E2)
                                    else -> Color(0xFFFEF9C3)
                                },
                                shape = CircleShape
                            ) {
                                Text(
                                    app.status,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when(app.status) {
                                        "Approved", "Completed" -> Color(0xFF16A34A)
                                        "Rejected" -> Color.Red
                                        else -> Color(0xFF854D0E)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
