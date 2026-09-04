package com.example.myapplication.employer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(viewModel: MainViewModel, navController: NavController) {
    val user = viewModel.currentUser
    val jobs by viewModel.jobs.collectAsState()
    val scope = rememberCoroutineScope()

    var showEditDialog by remember { mutableStateOf(false) }

    val companyName = remember(jobs, user) {
        jobs.find { it.employerEmail.equals(user?.email, ignoreCase = true) }?.company
            ?: "${user?.name ?: "Your Name"}'s Company"
    }

    val companyRating = viewModel.companyRating(companyName)
    val companyReviewCount = viewModel.companyReviewCount(companyName)

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
                    title = { Text("Company Profile 🏢", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Contact Info", tint = Color.White)
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Business, contentDescription = null, tint = primaryPurple, modifier = Modifier.size(50.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = companyName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Empowering Decent Work & Economic Growth",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = softSurface,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (companyReviewCount == 0) "No Rating" else String.format(Locale.US, "%.1f", companyRating),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textDark
                                )
                            }
                            Text("Overall Rating", fontSize = 11.sp, color = Color.Gray)
                        }

                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFDED9FF))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$companyReviewCount",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = textDark
                            )
                            Text("Reviews Received", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = softSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Contact Information", fontSize = 18.sp, fontWeight = FontWeight.Black, color = textDark)
                        Spacer(modifier = Modifier.height(16.dp))

                        CompanyInfoRow(icon = Icons.Default.Email, title = "Business Email", value = user?.email ?: "Not provided")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = primaryPurple.copy(alpha = 0.1f))

                        CompanyInfoRow(icon = Icons.Default.Phone, title = "Business Phone", value = user?.phone?.ifEmpty { "Not provided" } ?: "Not provided")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = primaryPurple.copy(alpha = 0.1f))

                        CompanyInfoRow(
                            icon = Icons.Default.Business,
                            title = "Industry",
                            value = user?.industry?.ifEmpty { "Technology & Services" } ?: "Technology & Services"
                        )
                    }
                }
            }
        }

        if (showEditDialog) {
            var editPhone by remember(user) { mutableStateOf(user?.phone ?: "") }
            var editIndustry by remember(user) { mutableStateOf(user?.industry?.ifEmpty { "Technology & Services" } ?: "Technology & Services") }
            var expanded by remember { mutableStateOf(false) }

            val industries = listOf(
                "Technology & Services",
                "Healthcare & Medicine",
                "Education & Training",
                "Food & Beverage",
                "Finance & Banking",
                "Arts, Entertainment & Media",
            )

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                title = { Text("Edit Contact Info ✏️", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Note: Email cannot be changed as it securely links your active jobs and reviews together.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        OutlinedTextField(
                            value = user?.email ?: "",
                            onValueChange = {},
                            label = { Text("Business Email") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            textStyle = TextStyle(color = Color.Gray, fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Gray,
                                disabledBorderColor = Color(0xFFF3E8FF),
                                disabledLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Business Phone") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textDark,
                                unfocusedTextColor = textDark,
                                focusedBorderColor = primaryPurple,
                                unfocusedBorderColor = Color(0xFFF3E8FF)
                            )
                        )


                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = editIndustry,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Industry") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textDark,
                                    unfocusedTextColor = textDark,
                                    focusedBorderColor = primaryPurple,
                                    unfocusedBorderColor = Color(0xFFF3E8FF)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                industries.forEach { selection ->
                                    DropdownMenuItem(
                                        text = { Text(selection, color = textDark) },
                                        onClick = {
                                            editIndustry = selection
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.updateUserProfile(
                                    newName = user?.name ?: "",
                                    newEmail = user?.email ?: "",
                                    newPhone = editPhone,
                                    newPassword = "",
                                    newIndustry = editIndustry
                                )
                                showEditDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                    ) { Text("Save Changes") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.Gray) }
                }
            )
        }
    }
}

@Composable
fun CompanyInfoRow(icon: ImageVector, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFEDE9FE),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E1B4B))
        }
    }
}