package com.example.myapplication.employer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(viewModel: MainViewModel, navController: NavController) {
    val user = viewModel.currentUser

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
                    text = "${user?.name ?: "Your Name"}'s Company",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Empowering Decent Work & Economic Growth",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(32.dp))

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

                        CompanyInfoRow(icon = Icons.Default.Business, title = "Industry", value = "Technology & Services")
                    }
                }
            }
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