package com.example.myapplication.employer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerScreen(viewModel: MainViewModel, navController: NavController) {
    val applications by viewModel.applications.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    var currentBottomTab by remember { mutableIntStateOf(0) }
    var selectedTopTab by remember { mutableIntStateOf(0) }

    var postJobTitle by remember { mutableStateOf("") }
    var postCompany by remember { mutableStateOf("") }
    var postSalary by remember { mutableStateOf("") }
    var postDescription by remember { mutableStateOf("") }
    var postSuccessMsg by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Employer Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentBottomTab == 0,
                    onClick = { currentBottomTab = 0 },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Find Work") },
                    label = { Text("Find Work", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentBottomTab == 1,
                    onClick = { currentBottomTab = 1 },
                    icon = { Icon(Icons.Default.AddBox, contentDescription = "Post Job") },
                    label = { Text("Post Job", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentBottomTab == 2,
                    onClick = { currentBottomTab = 2 },
                    icon = { Icon(Icons.Default.Domain, contentDescription = "Company Detail") },
                    label = { Text("Company Detail", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentBottomTab == 3,
                    onClick = { navController.navigate("worker_profile") },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentBottomTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        PrimaryTabRow(selectedTabIndex = selectedTopTab) {
                            Tab(
                                selected = selectedTopTab == 0,
                                onClick = { selectedTopTab = 0 },
                                text = { Text("Applications (${applications.size})", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTopTab == 1,
                                onClick = { selectedTopTab = 1 },
                                text = { Text("All Jobs (${jobs.size})", fontWeight = FontWeight.Bold) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedTopTab) {
                            0 -> {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "Manage Applications",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (applications.isEmpty()) {
                                        Text("No applications yet.", color = Color.Gray)
                                    } else {
                                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            items(applications) { app ->
                                                val jobTitle = jobs.find { it.id == app.jobId }?.title ?: "Unknown Job"

                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(16.dp),
                                                    elevation = CardDefaults.cardElevation(2.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(16.dp)) {
                                                        Text(text = "Applicant: ${app.workerName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                        Text(text = "Applied for: $jobTitle", color = Color(0xFF475569))
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(
                                                            text = "Status: ${app.status}",
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = when (app.status) {
                                                                "Approved" -> Color(0xFF16A34A)
                                                                "Rejected" -> Color.Red
                                                                else -> Color(0xFFEAB308)
                                                            }
                                                        )

                                                        if (app.status == "Pending") {
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(top = 12.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                            ) {
                                                                Button(
                                                                    onClick = { viewModel.updateApplicationStatus(app.id, "Approved") },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Text("Approve")
                                                                }

                                                                Button(
                                                                    onClick = { viewModel.updateApplicationStatus(app.id, "Rejected") },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                                    modifier = Modifier.weight(1f)
                                                                ) {
                                                                    Text("Reject")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            1 -> {
                                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "Available Jobs",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        items(jobs) { job ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(16.dp),
                                                elevation = CardDefaults.cardElevation(2.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(text = job.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                    Text(text = job.company, color = Color(0xFF64748B), fontSize = 14.sp)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(text = job.salary, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(text = job.description, color = Color(0xFF334155), fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Post a New Job", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Spacer(modifier = Modifier.height(16.dp))

                        if (postSuccessMsg.isNotEmpty()) {
                            Text(text = postSuccessMsg, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        OutlinedTextField(
                            value = postJobTitle,
                            onValueChange = { postJobTitle = it },
                            label = { Text("Job Title") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = postCompany,
                            onValueChange = { postCompany = it },
                            label = { Text("Company Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = postSalary,
                            onValueChange = { postSalary = it },
                            label = { Text("Salary (e.g. $100,000 / year)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = postDescription,
                            onValueChange = { postDescription = it },
                            label = { Text("Job Description") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                if (postJobTitle.isNotBlank()) {
                                    postSuccessMsg = "Job posted successfully!"
                                    postJobTitle = ""
                                    postCompany = ""
                                    postSalary = ""
                                    postDescription = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Submit Job", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                2 -> {
                    val currentName = viewModel.currentUser?.name ?: "Employer"
                    val currentEmail = viewModel.currentUser?.email ?: "employer@company.com"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(text = "Company Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(text = "Employer Name: $currentName", fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Email: $currentEmail", color = Color(0xFF64748B))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Active Job Listings: ${jobs.size}", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Manage your hiring preferences, view candidate statistics, and promote your company's open positions on JobBoom.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}