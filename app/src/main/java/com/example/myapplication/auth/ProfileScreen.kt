package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val user = viewModel.currentUser
    val applications by viewModel.applications.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()

    val userApplications = applications.filter { it.workerName == (user?.name ?: "") }
    val appliedCount = userApplications.size
    val approvedCount = userApplications.count { it.status == "Approved" }
    val savedCount = savedJobIds.size

    var showEditDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editEmail by remember { mutableStateOf(user?.email ?: "") }
    var editPassword by remember { mutableStateOf(user?.password ?: "") }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    )

    val darkTextColor = Color(0xFF0F172A)
    val subTextColor = Color(0xFF475569)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold, color = darkTextColor) },
                navigationIcon = {
                    IconButton(onClick = onBackToMenu) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Menu", tint = darkTextColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (user?.name ?: "U").take(1).uppercase(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.name ?: "Guest User",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = darkTextColor
                )

                Text(
                    text = user?.email ?: "guest@example.com",
                    fontSize = 14.sp,
                    color = subTextColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        editName = user?.name ?: ""
                        editEmail = user?.email ?: ""
                        editPassword = user?.password ?: ""
                        showEditDialog = true
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.height(38.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = darkTextColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = darkTextColor)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile & Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$appliedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                            Text(text = "Applied", fontSize = 12.sp, color = subTextColor)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFE2E8F0))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$savedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = darkTextColor)
                            Text(text = "Saved", fontSize = 12.sp, color = subTextColor)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp).width(1.dp), color = Color(0xFFE2E8F0))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$approvedCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            Text(text = "Approved", fontSize = 12.sp, color = subTextColor)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBox, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("Account Role", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = darkTextColor)
                            }
                            Text(user?.role ?: "Worker", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("Job Notifications", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = darkTextColor)
                            }
                            Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTermsDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("Terms of Service", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = darkTextColor)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showPrivacyDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("Privacy Policy", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = darkTextColor)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
                        }

                        HorizontalDivider(color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAboutDialog = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(14.dp))
                                Text("About JobBoom", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = darkTextColor)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = darkTextColor,
            unfocusedTextColor = darkTextColor,
            focusedLabelColor = Color(0xFF2563EB),
            unfocusedLabelColor = subTextColor
        )

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = Color.White,
                title = { Text("Edit Profile & Password", fontWeight = FontWeight.Bold, color = darkTextColor) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            colors = textFieldColors,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("New Password") },
                            colors = textFieldColors,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserProfile(editName, editEmail, editPassword)
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Cancel", color = subTextColor)
                    }
                }
            )
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                containerColor = Color.White,
                title = { Text("About JobBoom", fontWeight = FontWeight.Bold, color = darkTextColor) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Origin & Mission", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("JobBoom was created to bridge the gap between flexible job seekers and local employers in need of trusted talent.", fontSize = 13.sp, color = darkTextColor)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Why We Built JobBoom?", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("1. Empower Workers: Find flexible jobs easily.\n2. Support Employers: Post jobs and hire fast.", fontSize = 13.sp, color = darkTextColor)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text("Got it", fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            )
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color.White,
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold, color = darkTextColor) },
                text = {
                    Text(
                        "Users must provide accurate information. Fraudulent activities are strictly prohibited on JobBoom.",
                        fontSize = 14.sp,
                        color = darkTextColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("I Agree", color = Color.White)
                    }
                }
            )
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                containerColor = Color.White,
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = darkTextColor) },
                text = {
                    Text(
                        "Your information is strictly protected and used exclusively for job matching purposes on JobBoom.",
                        fontSize = 14.sp,
                        color = darkTextColor
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Understood", color = Color.White)
                    }
                }
            )
        }
    }
}