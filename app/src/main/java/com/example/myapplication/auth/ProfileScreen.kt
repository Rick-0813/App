package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.MainViewModel
import com.example.myapplication.ui.components.CuteInfoDialog
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUser = viewModel.currentUser
    val applications by viewModel.applications.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    val isEmployer = currentUser?.role == "Employer"

    val userApplications = applications.filter { it.workerEmail == (currentUser?.email ?: "") }
    val appliedCount = userApplications.size
    val savedCount = savedJobIds.size
    val workerRating = viewModel.workerRating(currentUser?.email ?: "")
    val earnedBadges = viewModel.skillBadgesForWorker(currentUser?.email ?: "")

    val myPostedJobs = jobs.filter { it.employerEmail.equals(currentUser?.email ?: "", ignoreCase = true) }
    val postedCount = myPostedJobs.size
    val myPostedJobIds = myPostedJobs.map { it.id }.toSet()
    val receivedApplicantsCount = applications.count { it.jobId in myPostedJobIds }
    val companyName = myPostedJobs.firstOrNull()?.company ?: "${currentUser?.name ?: "Your Name"}'s Company"
    val companyRating = viewModel.companyRating(companyName)

    var showEditDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showLinkGmailDialog by remember { mutableStateOf(false) }
    var gmailInput by remember { mutableStateOf("") }
    var linkError by remember { mutableStateOf("") }

    var newName by remember { mutableStateOf(currentUser?.name ?: "") }
    var newPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var newPassword by remember { mutableStateOf("") }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF)
    val textDark = Color(0xFF1E1B4B)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Profile & Settings ✨", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackToMenu) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape,
                    color = primaryPurple,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (currentUser?.name ?: "U").take(1).uppercase(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = currentUser?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(text = currentUser?.email.takeIf { !it.isNullOrBlank() } ?: "No email linked", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (isEmployer) "Role: Employer 👑" else "Role: Job Hunter 🌈",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = softSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (isEmployer) {
                                StatsCard("Posted", postedCount.toString(), Color(0xFFE0E7FF), modifier = Modifier.weight(1f))
                                StatsCard("Applicants", receivedApplicantsCount.toString(), Color(0xFFFEF3C7), modifier = Modifier.weight(1f))
                                StatsCard("Company ★", if (companyRating == 0f) "New" else String.format(Locale.US, "★ %.1f", companyRating), Color(0xFFDCFCE7), modifier = Modifier.weight(1f))
                            } else {
                                StatsCard("Applied", appliedCount.toString(), Color(0xFFE0E7FF), modifier = Modifier.weight(1f))
                                StatsCard("Saved", savedCount.toString(), Color(0xFFFEF3C7), modifier = Modifier.weight(1f))
                                StatsCard("Rating", if (workerRating == 0f) "New" else String.format(Locale.US, "★ %.1f", workerRating), Color(0xFFDCFCE7), modifier = Modifier.weight(1f))
                            }
                        }

                        if (!isEmployer) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(text = "My Skill Badges 🏆 (${earnedBadges.size})", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textDark)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (earnedBadges.isEmpty()) {
                                Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                    Text("No badges earned yet. Complete jobs to unlock verified badges! 🌟", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(12.dp))
                                }
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(earnedBadges) { badge ->
                                        Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(50), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))) {
                                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = badge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        ProfileMenuItem(
                            title = if (isEmployer) "Switch to Hunter Mode 🌈" else "Switch to Boss Mode 👑",
                            icon = Icons.Default.SwapHoriz,
                            iconTint = primaryPurple
                        ) {
                            val newRole = if (isEmployer) "Worker" else "Employer"
                            viewModel.updateUserRole(newRole)
                            onBackToMenu()
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileMenuItem(
                            title = "Link Google Account (Gmail)",
                            icon = Icons.Default.Link,
                            iconTint = Color(0xFF2563EB)
                        ) {
                            gmailInput = currentUser?.email ?: ""
                            linkError = ""
                            showLinkGmailDialog = true
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileMenuItem(
                            title = "Edit Profile ✨",
                            icon = Icons.Default.Edit,
                            iconTint = primaryPurple
                        ) {
                            newName = currentUser?.name ?: ""
                            newPhone = currentUser?.phone ?: ""
                            newPassword = ""
                            showEditDialog = true
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileMenuItem(title = "Terms of Service 📜", icon = Icons.Default.Description, iconTint = Color(0xFF16A34A)) { showTermsDialog = true }
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileMenuItem(title = "Privacy Policy 🔐", icon = Icons.Default.Lock, iconTint = Color(0xFFD97706)) { showPrivacyDialog = true }
                        Spacer(modifier = Modifier.height(10.dp))

                        ProfileMenuItem(title = "Delete Account 🗑️", icon = Icons.Default.DeleteForever, iconTint = Color.Red) { showDeleteConfirm = true }
                        Spacer(modifier = Modifier.height(10.dp))
                        ProfileMenuItem(title = "Log Out 🚪", icon = Icons.AutoMirrored.Filled.ExitToApp, iconTint = Color(0xFFEF4444)) { onLogout() }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        if (showLinkGmailDialog) {
            AlertDialog(
                onDismissRequest = { showLinkGmailDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Link Your Gmail ✨", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = gmailInput,
                            onValueChange = { gmailInput = it; linkError = "" },
                            label = { Text("Enter Gmail Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            textStyle = TextStyle(color = textDark, fontSize = 16.sp)
                        )
                        if (linkError.isNotEmpty()) {
                            Text(text = linkError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (gmailInput.contains("@gmail.com")) {
                                scope.launch {
                                    viewModel.linkGoogleAccount(gmailInput, currentUser?.name ?: "User")
                                    showLinkGmailDialog = false
                                    gmailInput = ""
                                }
                            } else {
                                linkError = "Please enter a valid @gmail.com address"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Confirm Link") }
                },
                dismissButton = {
                    TextButton(onClick = { showLinkGmailDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Edit Profile ✏️", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = textDark, fontSize = 16.sp))
                        OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = textDark, fontSize = 16.sp))
                        OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("New Password (Optional)") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), textStyle = TextStyle(color = textDark, fontSize = 16.sp))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.updateUserProfile(newName, currentUser?.email ?: "", newPhone, newPassword)
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                    ) { Text("Save Changes") }
                },
                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.Gray) } }
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Delete Account? ⚠️", fontWeight = FontWeight.Bold, color = textDark) },
                text = { Text("Are you sure you want to delete your account? All profile information will be permanently erased locally and from Supabase.", color = Color(0xFF4B5563)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCurrentUserAccount()
                            showDeleteConfirm = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("Delete Forever", color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = Color.Gray) } }
            )
        }

        if (showTermsDialog) {
            CuteInfoDialog(title = "Terms of Service 📜", content = "Welcome to JobBoom! Treat everyone with respect and provide honest information.", onDismiss = { showTermsDialog = false })
        }
        if (showPrivacyDialog) {
            CuteInfoDialog(title = "Privacy Policy 🔐", content = "Your data is strictly protected and synced securely with cloud standards.", onDismiss = { showPrivacyDialog = false })
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF4B5563))
        }
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = iconTint.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp), modifier = Modifier.size(38.dp)) {
                    Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp)) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        }
    }
}