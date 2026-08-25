package com.example.myapplication.myfeature

// MY RESPONSIBLE FEATURE FILE
// Job Details + Apply Request + Favourite + Two-Way Review/Rating + Skill Badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.Job
import com.example.myapplication.JobApplication
import com.example.myapplication.JobReview
import com.example.myapplication.MainViewModel
import com.example.myapplication.ReviewDirection
import java.util.Locale

private val Navy = Color(0xFF0B1C30)
private val BrandBlue = Color(0xFF005EB8)
private val DeepBlue = Color(0xFF00478D)
private val SuccessGreen = Color(0xFF007432)
private val BrightGreen = Color(0xFF6BFF8F)
private val AppBackground = Color(0xFFF8F9FF)
private val CardBorder = Color(0xFFC2C6D4)
private val SoftBlue = Color(0xFFEFF4FF)
private val MutedText = Color(0xFF424752)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun MyJobDetailsScreen(
    viewModel: MainViewModel,
    jobId: Int,
    applicationId: Int,
    onBack: () -> Unit
) {
    val jobs by viewModel.jobs.collectAsState()
    val applications by viewModel.applications.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val savedJobIds by viewModel.savedJobIds.collectAsState()
    val job = jobs.find { it.id == jobId }
    val user = viewModel.currentUser

    if (job == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Job not found", color = Navy)
        }
        return
    }

    val isEmployer = user?.role == "Employer"
    val currentApplication = if (isEmployer) {
        applications.firstOrNull { it.id == applicationId && it.jobId == jobId }
            ?: applications.firstOrNull { it.jobId == jobId && it.status == "Completed" }
            ?: applications.firstOrNull { it.jobId == jobId }
    } else {
        applications.filter { it.jobId == jobId && it.workerEmail == user?.email }.maxByOrNull { it.id }
    }
    val completedApplication = currentApplication?.takeIf { it.status == "Completed" }
    val reviewDirection = if (isEmployer) {
        ReviewDirection.EMPLOYER_TO_WORKER
    } else {
        ReviewDirection.WORKER_TO_COMPANY
    }
    val alreadyReviewed = completedApplication?.let {
        viewModel.hasSubmittedReview(it.id, reviewDirection)
    } ?: false
    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = AppBackground,
        topBar = {
            TopAppBar(
                title = { Text("JobBoom", color = DeepBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = DeepBlue)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSaveJob(job.id) }) {
                        Icon(
                            if (job.id in savedJobIds) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            if (job.id in savedJobIds) "Remove favourite" else "Save favourite",
                            tint = BrandBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBackground)
            )
        },
        bottomBar = {
            JobActionBar(
                isEmployer = isEmployer,
                application = currentApplication,
                onApply = { viewModel.applyForJob(job.id) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { JobHeader(job, currentApplication) }
            item { AboutJob(job) }
            item { RequirementsCard() }
            item {
                ReviewsAndRatings(
                    viewModel = viewModel,
                    job = job,
                    application = completedApplication,
                    isEmployer = isEmployer,
                    alreadyReviewed = alreadyReviewed,
                    reviews = reviews,
                    onReview = { showReviewDialog = true }
                )
            }
        }
    }

    if (showReviewDialog && completedApplication != null) {
        ReviewFormDialog(
            isEmployer = isEmployer,
            job = job,
            application = completedApplication,
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment, badge ->
                viewModel.submitReview(job.id, completedApplication.id, rating, comment, badge)
                showReviewDialog = false
            }
        )
    }
}

@Composable
private fun JobHeader(job: Job, application: JobApplication?) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(job.title, color = Navy, fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(job.company, color = MutedText, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Filled.Star, null, tint = BrightGreen, modifier = Modifier.size(21.dp))
            Text(" 4.8", color = Navy, fontWeight = FontWeight.Bold)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { StatusPill(job.salary) }
            item { StatusPill("Part-Time") }
            application?.let { item { StatusPill(it.status, green = it.status == "Completed") } }
        }
    }
}

@Composable
private fun AboutJob(job: Job) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("About the Job")
        Text(job.description.ifBlank { "Contact the employer for complete job information." }, color = MutedText, lineHeight = 23.sp)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SoftBlue,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0xFFD5E2F7))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Payment", color = DeepBlue, fontWeight = FontWeight.Bold)
                Text(job.salary, color = Navy, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Exact location and schedule will be confirmed by the employer.", color = MutedText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RequirementsCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SoftBlue,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFC9DCFF))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            SectionTitle("Requirements")
            listOf("Be punctual and reliable.", "Communicate clearly with the employer.", "Follow workplace safety instructions.").forEach {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(it, color = MutedText)
                }
            }
        }
    }
}

@Composable
private fun ReviewsAndRatings(
    viewModel: MainViewModel,
    job: Job,
    application: JobApplication?,
    isEmployer: Boolean,
    alreadyReviewed: Boolean,
    reviews: List<JobReview>,
    onReview: () -> Unit
) {
    val companyReviews = reviews.filter { it.jobId == job.id && it.direction == ReviewDirection.WORKER_TO_COMPANY }
    val workerEmail = application?.workerEmail.orEmpty()
    val workerReviews = reviews.filter {
        it.subjectKey == workerEmail && it.direction == ReviewDirection.EMPLOYER_TO_WORKER
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Reviews & Ratings")
        Text("Two-way reviews build trust between workers and employers.", color = MutedText)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RatingSummary(
                modifier = Modifier.weight(1f),
                title = "Company",
                rating = viewModel.companyRating(job.id),
                count = companyReviews.size,
                company = true
            )
            RatingSummary(
                modifier = Modifier.weight(1f),
                title = "Worker",
                rating = if (workerEmail.isBlank()) 0f else viewModel.workerRating(workerEmail),
                count = workerReviews.size,
                company = false
            )
        }

        if (application == null) {
            LockedReview("Reviews unlock after the employer marks the application as Completed.")
        } else {
            Text("Worker Skill Badges", color = Navy, fontWeight = FontWeight.Bold)
            SkillBadgeRow(viewModel.skillBadgesForWorker(application.workerEmail))
            Button(
                onClick = onReview,
                enabled = !alreadyReviewed,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = if (isEmployer) SuccessGreen else BrandBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(if (isEmployer) Icons.Outlined.Person else Icons.Outlined.Business, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (alreadyReviewed) "Review Submitted" else if (isEmployer) "Employer → Review Worker" else "Worker → Review Company",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        ReviewGroup("Company Reviews", "Written by workers", companyReviews)
        ReviewGroup("Worker Reviews", "Written by employers", workerReviews)
    }
}

@Composable
private fun RatingSummary(
    modifier: Modifier,
    title: String,
    rating: Float,
    count: Int,
    company: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(if (company) Icons.Outlined.Business else Icons.Outlined.Person, null, tint = DeepBlue)
            Text(title, color = Navy, fontWeight = FontWeight.Bold)
            Text(
                if (count == 0) "No rating" else String.format(Locale.getDefault(), "%.1f", rating),
                color = SuccessGreen,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("$count review${if (count == 1) "" else "s"}", color = MutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun LockedReview(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SoftBlue,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Lock, null, tint = DeepBlue)
            Spacer(Modifier.width(12.dp))
            Text(message, color = Navy, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SkillBadgeRow(badges: List<String>) {
    val shown = badges.ifEmpty { listOf("Verified Worker") }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(shown) { badge ->
            StatusPill(
                text = badge + if (badge in badges) " ✓" else "",
                green = badge in badges,
                icon = if (badge == "Verified Worker") Icons.Outlined.Verified else Icons.Outlined.EmojiEvents
            )
        }
    }
}

@Composable
private fun ReviewGroup(title: String, subtitle: String, reviews: List<JobReview>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, color = Navy, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(subtitle, color = MutedText, fontSize = 12.sp)
        if (reviews.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text("No reviews yet.", modifier = Modifier.padding(18.dp), color = MutedText)
            }
        } else {
            reviews.take(4).forEach { review -> ReviewCard(review) }
        }
    }
}

@Composable
private fun ReviewCard(review: JobReview) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.reviewerName, color = Navy, fontWeight = FontWeight.Bold)
                    Text(review.date, color = MutedText, fontSize = 11.sp)
                }
                RatingStars(review.rating.toFloat(), 17.dp)
            }
            Text("“${review.comment}”", color = Navy, fontStyle = FontStyle.Italic, lineHeight = 21.sp)
            if (review.skillBadge.isNotBlank()) {
                Text("+ ${review.skillBadge} badge", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun JobActionBar(
    isEmployer: Boolean,
    application: JobApplication?,
    onApply: () -> Unit
) {
    Surface(color = AppBackground, shadowElevation = 10.dp) {
        val enabled = !isEmployer && application == null
        val label = when {
            isEmployer -> "Employer Account • View Applications"
            application == null -> "Apply Now"
            application.status == "Completed" -> "Job Completed • Reviews Available"
            else -> "Application ${application.status}"
        }
        Button(
            onClick = onApply,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SuccessGreen,
                disabledContainerColor = if (application?.status == "Completed") SuccessGreen else SoftBlue,
                disabledContentColor = if (application?.status == "Completed") Color.White else DeepBlue
            )
        ) {
            Text(label, fontWeight = FontWeight.Bold)
        }
    }
}

/** Opens the student's Employer → Worker review form directly from Boss Panel. */
@Composable
fun EmployerReviewWorkerDialog(
    viewModel: MainViewModel,
    job: Job,
    application: JobApplication,
    onDismiss: () -> Unit
) {
    ReviewFormDialog(
        isEmployer = true,
        job = job,
        application = application,
        onDismiss = onDismiss,
        onSubmit = { rating, comment, badge ->
            viewModel.submitReview(job.id, application.id, rating, comment, badge)
            onDismiss()
        }
    )
}

@Composable
private fun ReviewFormDialog(
    isEmployer: Boolean,
    job: Job,
    application: JobApplication,
    onDismiss: () -> Unit,
    onSubmit: (Int, String, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var badge by remember { mutableStateOf("") }
    val badgeOptions = listOf("Reliable", "Punctual", "Customer Service", "Quick Learner", "Team Player")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(22.dp),
        title = { Text(if (isEmployer) "Review Worker" else "Review Company", color = Navy, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    if (isEmployer) "${application.workerName} • ${job.title}" else "${job.company} • ${job.title}",
                    color = MutedText
                )
                Text("Rating", color = Navy, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    for (star in 1..5) {
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                Icons.Filled.Star,
                                "$star stars",
                                tint = if (star <= rating) SuccessGreen else CardBorder,
                                modifier = Modifier.size(31.dp)
                            )
                        }
                    }
                }
                if (isEmployer) {
                    Text("Award Skill Badge (optional)", color = Navy, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(badgeOptions) { option ->
                            FilterChip(
                                selected = badge == option,
                                onClick = { badge = if (badge == option) "" else option },
                                label = { Text(option) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrightGreen,
                                    selectedLabelColor = SuccessGreen
                                )
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Review comment") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment, badge) },
                enabled = comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Submit Review") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun StatusPill(
    text: String,
    green: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        color = if (green) BrightGreen else SoftBlue,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (green) BrightGreen else Color(0xFFD5E2F7))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(it, null, tint = if (green) SuccessGreen else DeepBlue, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(text, color = if (green) SuccessGreen else DeepBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RatingStars(rating: Float, size: Dp) {
    Row {
        repeat(5) { index ->
            Icon(
                Icons.Filled.Star,
                null,
                tint = if (index + 1 <= rating) SuccessGreen else CardBorder,
                modifier = Modifier.size(size)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Navy, fontSize = 22.sp, fontWeight = FontWeight.Bold)
}
