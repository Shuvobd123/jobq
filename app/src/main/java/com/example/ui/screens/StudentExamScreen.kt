package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.Exam
import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.example.data.model.UiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExamHallTab(
    publishedExamsState: UiState<List<Exam>>,
    activeQuestionsState: UiState<List<Question>>,
    isBusy: Boolean,
    onSeedSampleData: () -> Unit,
    onStartExam: (Exam, List<Question>) -> Unit,
    onNavigateToServerControl: () -> Unit
) {
    val allQuestions = (activeQuestionsState as? UiState.Success)?.data ?: emptyList()
    var selectedSubject by rememberSaveable { mutableStateOf("সব বিষয়") }

    val subjects = remember(allQuestions) {
        listOf("সব বিষয়") + allQuestions.map { it.subject }.filter { it.isNotBlank() }.distinct()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("exam_hall_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_exam_hero),
                        contentDescription = "Exam Hall Hero",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE60F172A), Color(0x660F172A))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Surface(
                                color = Color(0xFF10B981),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "LIVE CLOUD EXAM PORTAL",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "অনলাইন এমসিকিউ পরীক্ষা কেন্দ্র",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "সার্ভারের সক্রিয় প্রশ্নভান্ডার: ${allQuestions.size} টি এমসিকিউ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD1FAE5)
                            )
                        }
                    }
                }
            }
        }

        // Quick Instant Practice Exam Card if questions exist
        if (allQuestions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "তাৎক্ষণিক র‍্যান্ডম মক টেস্ট",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "সার্ভারের প্রশ্নব্যাংক থেকে যেকোনো প্রশ্নে দ্রুত পরীক্ষা দিন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val quickExam = Exam(
                                    id = "quick_mock_test",
                                    title = "তাৎক্ষণিক মক টেস্ট ($selectedSubject)",
                                    subject = selectedSubject,
                                    description = "সার্ভারের সক্রিয় প্রশ্ন থেকে বাছাইকৃত তাৎক্ষণিক এমসিকিউ পরীক্ষা",
                                    durationMinutes = 10L,
                                    totalQuestions = allQuestions.size.coerceAtMost(15).toLong(),
                                    negativeMarkPerWrong = 25L,
                                    passPercentage = 40L
                                )
                                val pool = if (selectedSubject == "সব বিষয়") {
                                    allQuestions
                                } else {
                                    allQuestions.filter { it.subject == selectedSubject }.ifEmpty { allQuestions }
                                }
                                onStartExam(quickExam, pool.shuffled().take(15))
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("start_quick_mock_exam_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("শুরু করুন")
                        }
                    }
                }
            }
        }

        // Subject Filter Chips
        if (subjects.size > 1) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(subjects) { subject ->
                        FilterChip(
                            selected = selectedSubject == subject,
                            onClick = { selectedSubject = subject },
                            label = { Text(subject) }
                        )
                    }
                }
            }
        }

        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সার্ভার নিয়ন্ত্রিত লাইভ পরীক্ষাসমূহ",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToServerControl) {
                    Text("+ নতুন পরীক্ষা / প্রশ্ন")
                }
            }
        }

        when (publishedExamsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = publishedExamsState.message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            is UiState.Success -> {
                val filteredExams = publishedExamsState.data.filter { exam ->
                    selectedSubject == "সব বিষয়" || exam.subject == "সব বিষয়" || exam.subject == selectedSubject
                }

                if (filteredExams.isEmpty()) {
                    item {
                        EmptyServerBankCard(
                            isBusy = isBusy,
                            onSeedSampleData = onSeedSampleData,
                            onNavigateToServerControl = onNavigateToServerControl
                        )
                    }
                } else {
                    items(filteredExams, key = { it.id }) { exam ->
                        val matchingQuestions = remember(exam, allQuestions) {
                            val pool = if (exam.subject == "সব বিষয়" || exam.subject.isBlank()) {
                                allQuestions
                            } else {
                                val exact = allQuestions.filter { it.subject.equals(exam.subject, ignoreCase = true) }
                                exact.ifEmpty { allQuestions }
                            }
                            pool.take(exam.totalQuestions.toInt().coerceAtLeast(1))
                        }
                        ExamCardItem(
                            exam = exam,
                            availableQuestionCount = matchingQuestions.size,
                            onStartClick = {
                                if (matchingQuestions.isNotEmpty()) {
                                    onStartExam(exam, matchingQuestions)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyServerBankCard(
    isBusy: Boolean,
    onSeedSampleData: () -> Unit,
    onNavigateToServerControl: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "সার্ভারে এখনো কোনো লাইভ পরীক্ষা যুক্ত করা হয়নি",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "এক ক্লিকে ৮টি নমুনা এমসিকিউ প্রশ্ন ও ২টি মডেল টেস্ট সার্ভারে লোড করুন অথবা সার্ভার কন্ট্রোল প্যানেল থেকে নিজের প্রশ্ন যোগ করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSeedSampleData,
                    enabled = !isBusy,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.testTag("seed_sample_bank_button")
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("ডেমো প্রশ্নব্যাংক লোড করুন")
                }
                OutlinedButton(
                    onClick = onNavigateToServerControl,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("সার্ভার কন্ট্রোল")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExamCardItem(
    exam: Exam,
    availableQuestionCount: Int,
    onStartClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = exam.subject.ifBlank { "সব বিষয়" },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${exam.durationMinutes} মিনিট",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (exam.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exam.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val negMarkFormatted = String.format(Locale.US, "%.2f", exam.negativeMarkPerWrong / 100.0)
                Text(
                    text = "• প্রশ্ন সংখ্যা: $availableQuestionCount টি",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• নেগেটিভ মার্ক: $negMarkFormatted",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• পাস মার্ক: ${exam.passPercentage}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartClick,
                enabled = availableQuestionCount > 0,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_exam_btn_${exam.id}")
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (availableQuestionCount > 0) "এমসিকিউ পরীক্ষা দিন ($availableQuestionCount টি প্রশ্ন)"
                    else "সার্ভারে প্রশ্ন নেই — প্রথমে প্রশ্ন যোগ করুন",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun QuestionBankStudyTab(
    activeQuestionsState: UiState<List<Question>>,
    isBusy: Boolean,
    onSeedSampleData: () -> Unit,
    onEditQuestion: (Question) -> Unit,
    currentUserId: String
) {
    var selectedSubject by rememberSaveable { mutableStateOf("সব বিষয়") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val questions = (activeQuestionsState as? UiState.Success)?.data ?: emptyList()
    val subjects = remember(questions) {
        listOf("সব বিষয়") + questions.map { it.subject }.filter { it.isNotBlank() }.distinct()
    }

    val filteredQuestions = remember(questions, selectedSubject, searchQuery) {
        questions.filter { q ->
            val matchesSubject = selectedSubject == "সব বিষয়" || q.subject == selectedSubject
            val matchesSearch = searchQuery.isBlank() ||
                q.questionText.contains(searchQuery, ignoreCase = true) ||
                q.topic.contains(searchQuery, ignoreCase = true) ||
                q.subject.contains(searchQuery, ignoreCase = true)
            matchesSubject && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("question_bank_study_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "ক্লাউড প্রশ্নব্যাংক ও অধ্যায়ভিত্তিক অনুশীলন",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "প্রতিটি প্রশ্নের সঠিক উত্তর ও ব্যাখ্যা দেখুন অথবা নিজে উত্তর মিলিয়ে নিন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (subjects.size > 1) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjects) { subject ->
                        FilterChip(
                            selected = selectedSubject == subject,
                            onClick = { selectedSubject = subject },
                            label = { Text(subject) }
                        )
                    }
                }
            }
        }

        if (filteredQuestions.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "কোনো প্রশ্ন পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onSeedSampleData,
                            enabled = !isBusy
                        ) {
                            Text("ডেমো প্রশ্নব্যাংক সার্ভারে লোড করুন")
                        }
                    }
                }
            }
        } else {
            itemsIndexed(filteredQuestions, key = { _, q -> q.id }) { index, question ->
                StudyQuestionCard(
                    index = index + 1,
                    question = question,
                    canEdit = question.authorId == currentUserId,
                    onEditClick = { onEditQuestion(question) }
                )
            }
        }
    }
}

@Composable
private fun StudyQuestionCard(
    index: Int,
    question: Question,
    canEdit: Boolean,
    onEditClick: () -> Unit
) {
    var selectedOption by rememberSaveable(question.id) { mutableIntStateOf(-1) }
    var showAnswer by rememberSaveable(question.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = question.subject,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "• ${question.topic}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canEdit) {
                    TextButton(onClick = onEditClick) {
                        Text("আপডেট")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "প্রশ্ন $index: ${question.questionText}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            val labels = listOf("ক", "খ", "গ", "ঘ")
            question.optionsList().forEachIndexed { optIndex, optText ->
                val isCorrect = optIndex == question.correctOptionIndex.toInt()
                val isSelected = selectedOption == optIndex
                val reveal = showAnswer || selectedOption != -1

                val containerColor = when {
                    reveal && isCorrect -> Color(0xFFD1FAE5)
                    reveal && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                    isSelected -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                val textColor = when {
                    reveal && isCorrect -> Color(0xFF065F46)
                    reveal && isSelected && !isCorrect -> Color(0xFF991B1B)
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedOption = optIndex }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = labels.getOrElse(optIndex) { "${optIndex + 1}" },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = textColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = optText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                        if (reveal && isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct",
                                tint = Color(0xFF059669)
                            )
                        } else if (reveal && isSelected && !isCorrect) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Wrong",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showAnswer = !showAnswer }) {
                    Icon(
                        imageVector = if (showAnswer) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showAnswer) "উত্তর লুকান" else "সঠিক উত্তর ও ব্যাখ্যা দেখুন")
                }
                Text(
                    text = "মান: ${question.marks}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(visible = showAnswer || selectedOption != -1) {
                if (question.explanation.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ব্যাখ্যা: ${question.explanation}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveExamRunnerScreen(
    exam: Exam,
    questions: List<Question>,
    isSubmitting: Boolean,
    onCancelExam: () -> Unit,
    onSubmitExam: (Map<String, Int>, Int) -> Unit
) {
    var currentQuestionIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<String, Int>() }
    val totalSeconds = remember(exam) { (exam.durationMinutes.coerceAtLeast(1L) * 60L).toInt() }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(totalSeconds) }
    var showConfirmExitDialog by remember { mutableStateOf(false) }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showConfirmExitDialog = true
    }

    LaunchedEffect(exam.id) {
        while (remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds--
        }
        if (remainingSeconds <= 0 && !isSubmitting) {
            val spent = (totalSeconds - remainingSeconds).coerceAtLeast(1)
            onSubmitExam(selectedAnswers.toMap(), spent)
        }
    }

    if (showConfirmExitDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmExitDialog = false },
            title = { Text("পরীক্ষা ত্যাগ করবেন?") },
            text = { Text("এখন বের হয়ে গেলে আপনার উত্তরপত্র জমা হবে না। আপনি কি নিশ্চিত?") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmExitDialog = false
                    onCancelExam()
                }) {
                    Text("হ্যাঁ, বের হবো")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirmExitDialog = false }) {
                    Text("পরীক্ষা চালিয়ে যান")
                }
            }
        )
    }

    if (showConfirmSubmitDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSubmitDialog = false },
            title = { Text("উত্তরপত্র জমা দেবেন?") },
            text = {
                Text("আপনি ${questions.size} টি প্রশ্নের মধ্যে ${selectedAnswers.size} টির উত্তর দিয়েছেন। সার্ভারে চূড়ান্তভাবে জমা দিতে চান?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmSubmitDialog = false
                        val spent = (totalSeconds - remainingSeconds).coerceAtLeast(1)
                        onSubmitExam(selectedAnswers.toMap(), spent)
                    },
                    modifier = Modifier.testTag("confirm_submit_exam_button")
                ) {
                    Text("হ্যাঁ, জমা দিন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmSubmitDialog = false }) {
                    Text("রিভিউ করুন")
                }
            }
        )
    }

    val currentQuestion = questions.getOrNull(currentQuestionIndex)
    val minutesLeft = remainingSeconds / 60
    val secondsLeft = remainingSeconds % 60
    val timerFormatted = String.format(Locale.US, "%02d:%02d", minutesLeft, secondsLeft)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("live_exam_runner_screen")
    ) {
        // Exam Header with Timer
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = { showConfirmExitDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit Exam"
                            )
                        }
                        Column {
                            Text(
                                text = exam.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "উত্তর দেওয়া হয়েছে: ${selectedAnswers.size} / ${questions.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Surface(
                        color = if (remainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Remaining Time",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timerFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size.coerceAtLeast(1).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Question Number Pills Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(questions) { idx, q ->
                val isCurrent = idx == currentQuestionIndex
                val isAnswered = selectedAnswers.containsKey(q.id)
                Surface(
                    color = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isAnswered -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { currentQuestionIndex = idx }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${idx + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = when {
                                isCurrent -> MaterialTheme.colorScheme.onPrimary
                                isAnswered -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Question Card
        if (currentQuestion != null) {
            Card(
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "প্রশ্ন ${currentQuestionIndex + 1} / ${questions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${currentQuestion.subject} • মান: ${currentQuestion.marks}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentQuestion.questionText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val optionLabels = listOf("ক", "খ", "গ", "ঘ")
                    itemsIndexed(currentQuestion.optionsList()) { optIndex, optText ->
                        val isSelected = selectedAnswers[currentQuestion.id] == optIndex
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedAnswers[currentQuestion.id] = optIndex
                                }
                                .testTag("exam_option_$optIndex")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.size(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = optionLabels.getOrElse(optIndex) { "${optIndex + 1}" },
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = optText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Bottom Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (currentQuestionIndex > 0) currentQuestionIndex--
                },
                enabled = currentQuestionIndex > 0,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("পূর্ববর্তী")
            }

            if (currentQuestionIndex < questions.size - 1) {
                Button(
                    onClick = { currentQuestionIndex++ },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("next_question_button")
                ) {
                    Text("পরবর্তী")
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = { showConfirmSubmitDialog = true },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("submit_exam_button")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("সাবমিট করুন")
                }
            }
        }
    }
}

@Composable
fun ExamResultSummaryScreen(
    attempt: ExamAttempt,
    questions: List<Question>,
    selectedAnswers: Map<String, Int>,
    onBackToExamHall: () -> Unit
) {
    BackHandler { onBackToExamHall() }

    val formattedScore = String.format(Locale.US, "%.2f", attempt.scoreHundredths / 100.0)
    val labels = listOf("ক", "খ", "গ", "ঘ")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("exam_result_summary_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (attempt.passed) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = if (attempt.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (attempt.passed) "অভিনন্দন! আপনি উত্তীর্ণ হয়েছেন" else "আরও অনুশীলন প্রয়োজন",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = attempt.examTitle,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBadge("প্রাপ্ত নম্বর", "$formattedScore / ${attempt.maxMarks}")
                        StatBadge("সঠিক উত্তর", "${attempt.correctCount} টি")
                        StatBadge("ভুল উত্তর", "${attempt.wrongCount} টি")
                        StatBadge("অ্যাকুরেসি", "${attempt.accuracyPercent}%")
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = onBackToExamHall,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("পরীক্ষা কেন্দ্রে ফিরে যান")
                    }
                }
            }
        }

        item {
            Text(
                text = "বিস্তারিত উত্তরপত্র ও সমাধান",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(questions) { idx, q ->
            val userChoice = selectedAnswers[q.id]
            val correctChoice = q.correctOptionIndex.toInt()
            val isCorrect = userChoice == correctChoice

            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "প্রশ্ন ${idx + 1}: ${q.questionText}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val userText = if (userChoice != null) {
                        "${labels.getOrElse(userChoice) { "" }}) ${q.optionsList().getOrElse(userChoice) { "" }}"
                    } else {
                        "উত্তর দেওয়া হয়নি"
                    }
                    val correctText = "${labels.getOrElse(correctChoice) { "" }}) ${q.optionsList().getOrElse(correctChoice) { "" }}"

                    Text(
                        text = "আপনার উত্তর: $userText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCorrect) Color(0xFF059669) else Color(0xFFDC2626),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "সঠিক উত্তর: $correctText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Bold
                    )
                    if (q.explanation.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ব্যাখ্যা: ${q.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun StudentResultsHistoryTab(
    myAttemptsState: UiState<List<ExamAttempt>>
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("student_results_history_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "আমার পরীক্ষার ফলাফল ও মেধাক্রম",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "সার্ভারে সংরক্ষিত আপনার সকল এমসিকিউ পরীক্ষার রেকর্ড",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (myAttemptsState) {
            is UiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(
                        text = myAttemptsState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is UiState.Success -> {
                val attempts = myAttemptsState.data
                if (attempts.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "আপনি এখনো কোনো পরীক্ষায় অংশ নেননি",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "‘পরীক্ষা কেন্দ্র’ ট্যাব থেকে যেকোনো মডেল টেস্টে অংশ নিন।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(attempts, key = { it.id }) { att ->
                        val scoreFormatted = String.format(Locale.US, "%.2f", att.scoreHundredths / 100.0)
                        val dateText = att.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "সদ্য সমাপ্ত"
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = att.examTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = if (att.passed) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (att.passed) "উত্তীর্ণ (${att.accuracyPercent}%)" else "অনুত্তীর্ণ (${att.accuracyPercent}%)",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (att.passed) Color(0xFF065F46) else Color(0xFF991B1B),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "বিষয়: ${att.subject} • সময়: $dateText",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("স্কোর: $scoreFormatted / ${att.maxMarks}", fontWeight = FontWeight.SemiBold)
                                    Text("সঠিক: ${att.correctCount}/${att.totalQuestions}")
                                    Text("ভুল: ${att.wrongCount}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
