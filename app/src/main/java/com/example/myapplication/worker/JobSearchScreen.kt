package com.example.myapplication.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var showSearchField by remember { mutableStateOf(false) }
    var currentBottomTab by remember { mutableIntStateOf(0) }

    var selectedCategory by remember { mutableStateOf("All Jobs") }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var recommendTab by remember { mutableIntStateOf(0) }
    var selectedLocation by remember { mutableStateOf("Kuala Lumpur") }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    var selectedJobType by remember { mutableStateOf("All") } 

    val filteredJobs = jobs.filter { job ->
        val matchesCategory = if (selectedCategory == "All Jobs") true else job.title.contains(selectedCategory, ignoreCase = true)
        val matchesSearch = job.title.contains(searchQuery, ignoreCase = true) || job.company.contains(searchQuery, ignoreCase = true)
        val matchesType = if (selectedJobType == "All") true else job.description.contains(selectedJobType, ignoreCase = true)
        matchesCategory && matchesSearch && matchesType
    }

    val darkTextColor = Color(0xFF0F172A)

    Scaffold(
        topBar = {
            Surface(
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCategoryDropdown = true }
                        ) {
                            Text(
                                text = selectedCategory,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = darkTextColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE0F2FE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Full-time", fontSize = 11.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false }
                        ) {
                            listOf("All Jobs", "Software", "Manager", "Designer", "Helper", "Cashier").forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, fontWeight = FontWeight.Medium) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryDropdown = false
                                    }
                                )
                            }
                        }

                        IconButton(onClick = { showSearchField = !showSearchField }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = darkTextColor)
                        }
                    }

                    if (showSearchField) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search title, company...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFF1F5F9))
                                .padding(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (recommendTab == 0) Color.White else Color.Transparent)
                                    .clickable { recommendTab = 0 }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Recommended for You",
                                    fontSize = 12.sp,
                                    fontWeight = if (recommendTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (recommendTab == 0) darkTextColor else Color(0xFF64748B)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (recommendTab == 1) Color.White else Color.Transparent)
                                    .clickable { recommendTab = 1 }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "news",
                                    fontSize = 12.sp,
                                    fontWeight = if (recommendTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (recommendTab == 1) darkTextColor else Color(0xFF64748B)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF8FAFC))
                                .clickable { showLocationDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedLocation,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                        }

                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Requirements Filter",
                                tint = darkTextColor
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentBottomTab == 0,
                    onClick = { currentBottomTab = 0 },
                    icon = { Icon(Icons.Default.Work, contentDescription = null) },
                    label = { Text("Find Jobs", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentBottomTab == 1,
                    onClick = { currentBottomTab = 1 },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) },
                    label = { Text("Saved", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentBottomTab == 2,
                    onClick = { currentBottomTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null) },
                    label = { Text("Applications", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("worker_profile") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Profile", fontSize = 11.sp) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when (currentBottomTab) {
                0 -> {
                    if (filteredJobs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No jobs match your selected filters.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(filteredJobs) { job ->
                                val isSaved = savedJobIds.contains(job.id)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                                                Text(text = job.company, color = Color(0xFF64748B), fontSize = 14.sp)
                                            }
                                            IconButton(onClick = { viewModel.toggleSaveJob(job.id) }) {
                                                Icon(
                                                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = null,
                                                    tint = if (isSaved) Color.Red else Color.Gray
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = job.salary, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = job.description, color = Color(0xFF475569), fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = { viewModel.applyForJob(job.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                        ) {
                                            Text("Apply Now", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val savedJobs = jobs.filter { savedJobIds.contains(it.id) }
                    Column {
                        Text("Saved Jobs (${savedJobs.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (savedJobs.isEmpty()) {
                            Text("No saved jobs yet.", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(savedJobs) { job ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                                            Text(text = job.company, color = Color(0xFF64748B))
                                            Text(text = job.salary, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    val activeWorkerName = viewModel.currentUser?.name ?: ""
                    val myApps = applications.filter { it.workerName == activeWorkerName }

                    Column {
                        Text("My Applications (${myApps.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        if (myApps.isEmpty()) {
                            Text("You haven't applied for any jobs yet.", color = Color.Gray)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(myApps) { app ->
                                    val jobTitle = jobs.find { it.id == app.jobId }?.title ?: "Job"
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(text = "Job: $jobTitle", fontWeight = FontWeight.Bold, color = darkTextColor)
                                            Text(
                                                text = "Status: ${app.status}",
                                                color = when(app.status) {
                                                    "Approved" -> Color(0xFF16A34A)
                                                    "Rejected" -> Color.Red
                                                    else -> Color(0xFFEAB308)
                                                },
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = { Text("Select Location", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        listOf("Kuala Lumpur", "Johor Bahru", "Penang", "Selangor", "All Regions").forEach { location ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedLocation = location
                                        showLocationDialog = false
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = location, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLocationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showFilterDialog) {
            AlertDialog(
                onDismissRequest = { showFilterDialog = false },
                title = { Text("Filter Requirements", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Job Type", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(6.dp))
                        listOf("All", "Full-time", "Part-time").forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedJobType = type }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedJobType == type,
                                    onClick = { selectedJobType = type }
                                )
                                Text(text = type, fontSize = 14.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showFilterDialog = false }) {
                        Text("Apply Filter")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFilterDialog = false }) {
                        Text("Reset")
                    }
                }
            )
        }
    }
}