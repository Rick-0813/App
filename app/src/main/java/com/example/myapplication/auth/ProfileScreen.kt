package com.example.myapplication.auth

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val user = viewModel.currentUser
    val applications by viewModel.applications.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()
    val jobs by viewModel.jobs.collectAsState()

    val isEmployer = user?.role == "Employer"

    val userApplications = applications.filter { it.workerEmail == (user?.email ?: "") }
    val appliedCount = userApplications.size
    val savedCount = savedJobIds.size
    val workerRating = viewModel.workerRating(user?.email ?: "")
    val earnedBadges = viewModel.skillBadgesForWorker(user?.email ?: "")

    val myPostedJobs = jobs.filter { it.employerEmail.equals(user?.email ?: "", ignoreCase = true) }
    val postedCount = myPostedJobs.size
    val myPostedJobIds = myPostedJobs.map { it.id }.toSet()
    val receivedApplicantsCount = applications.count { it.jobId in myPostedJobIds }
    val companyName = myPostedJobs.firstOrNull()?.company ?: "${user?.name ?: "Your Name"}'s Company"
    val companyRating = viewModel.companyRating(companyName)

    var showEditDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) } // 注销确认弹窗状态

    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editPhone by remember { mutableStateOf(user?.phone ?: "") }
    var editPassword by remember { mutableStateOf("") }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("My Nest 🏠", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackToMenu) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
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
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = primaryPurple,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(4.dp, Color.White)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user?.name ?: "U").take(1).uppercase(),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = user?.name ?: "Guest", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = user?.email ?: "", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))

                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (isEmployer) "Role: Employer 👑" else "Role: Job Hunter 🌈",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = softSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isEmployer) {
                                StatsCard("Posted", postedCount.toString(), Color(0xFFE0E7FF), modifier = Modifier.weight(1f))
                                StatsCard("Applicants", receivedApplicantsCount.toString(), Color(0xFFFEF3C7), modifier = Modifier.weight(1f))
                                StatsCard(
                                    label = "Company ★",
                                    value = if (companyRating == 0f) "New" else String.format(Locale.US, "★ %.1f", companyRating),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                StatsCard("Applied", appliedCount.toString(), Color(0xFFE0E7FF), modifier = Modifier.weight(1f))
                                StatsCard("Saved", savedCount.toString(), Color(0xFFFEF3C7), modifier = Modifier.weight(1f))
                                StatsCard(
                                    label = "Rating",
                                    value = if (workerRating == 0f) "New" else String.format(Locale.US, "★ %.1f", workerRating),
                                    color = Color(0xFFDCFCE7),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (!isEmployer) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "My Skill Badges 🏆 (${earnedBadges.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1B4B)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (earnedBadges.isEmpty()) {
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No skill badges earned yet. Complete jobs to receive badges from employers! 🌟",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(earnedBadges) { badge ->
                                        Surface(
                                            color = Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(50),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(text = badge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        val switchLabel = if (isEmployer) "Switch to Hunter Mode 🌈" else "Switch to Boss Mode 👑"

                        ProfileButton(switchLabel, Icons.Default.SwitchAccount, onClick = {
                            val newRole = if (isEmployer) "Worker" else "Employer"
                            viewModel.updateUserRole(newRole)
                            onBackToMenu()
                        })

                        ProfileButton("Edit Profile ✨", Icons.Default.Edit, onClick = {
                            editName = user?.name ?: ""
                            editPhone = user?.phone ?: ""
                            showEditDialog = true
                        })

                        ProfileButton("Terms of Service 📜", Icons.Default.Gavel, onClick = { showTermsDialog = true })
                        ProfileButton("Privacy Policy 🔐", Icons.Default.Lock, onClick = { showPrivacyDialog = true })

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = primaryPurple.copy(alpha = 0.1f))

                        ProfileButton("Delete Account 🗑️", Icons.Default.DeleteForever, isDanger = true, onClick = {
                            showDeleteAccountDialog = true
                        })

                        ProfileButton("Logout 🚪", Icons.AutoMirrored.Filled.ExitToApp, isDanger = false, onClick = onLogout)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        if (showEditDialog) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                shape = RoundedCornerShape(28.dp),
                containerColor = softSurface,
                title = { Text("Update Details 🌈", fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Name") },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone") },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                        )
                        OutlinedTextField(
                            value = editPassword,
                            onValueChange = { editPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryPurple)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                viewModel.updateUserProfile(editName, user?.email ?: "", editPhone, editPassword)
                                showEditDialog = false
                            }
                        }
                    ) { Text("Save Changes ✨", fontWeight = FontWeight.Bold, color = primaryPurple) }
                }
            )
        }

        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteAccountDialog = false },
                shape = RoundedCornerShape(22.dp),
                containerColor = Color.White,
                title = { Text("Delete Account? ⚠️", fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B)) },
                text = { Text("Are you sure you want to delete your account? All your profile information and saved items will be permanently erased. This cannot be undone.", color = Color(0xFF4B5563)) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCurrentUserAccount()
                            showDeleteAccountDialog = false
                            onLogout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) { Text("Delete Forever", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }

        if (showTermsDialog) {
            CuteInfoDialog(
                title = "Terms of Service 📜",
                content = "By using JobBoom, you agree to treat everyone with respect and provide honest info. Let's grow together!",
                onDismiss = { showTermsDialog = false }
            )
        }

        if (showPrivacyDialog) {
            CuteInfoDialog(
                title = "Privacy Policy 🔐",
                content = "We value your trust! Your data is protected and used only to find your dream jobs. Magic secrets stay safe.",
                onDismiss = { showPrivacyDialog = false }
            )
        }
    }
}

@Composable
fun StatsCard(label: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF4B5563))
        }
    }
}

@Composable
fun ProfileButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isDanger: Boolean = false, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = if (isDanger) Color(0xFFFFE4E6) else Color(0xFFEDE9FE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = if (isDanger) Color.Red else Color(0xFF7E57C2), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, color = if (isDanger) Color.Red else Color(0xFF1E1B4B), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
        }
    }
}