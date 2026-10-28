package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Exam
import com.example.data.model.Question
import com.example.data.model.UiState
import com.example.ui.viewmodel.QuestionBankViewModel

@Composable
fun ServerControlScreen(
    viewModel: QuestionBankViewModel,
    authoredQuestionsState: UiState<List<Question>>,
    createdExamsState: UiState<List<Exam>>,
    isBusy: Boolean,
    initialQuestionToEdit: Question? = null,
    onClearInitialQuestionToEdit: () -> Unit = {}
) {
    var selectedSubTab by rememberSaveable { mutableIntStateOf(0) }
    var showQuestionDialog by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<Question?>(null) }

    var showExamDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }

    // Open edit dialog if navigated from study tab
    if (initialQuestionToEdit != null && !showQuestionDialog) {
        editingQuestion = initialQuestionToEdit
        showQuestionDialog = true
        onClearInitialQuestionToEdit()
    }

    val myQuestions = (authoredQuestionsState as? UiState.Success)?.data ?: emptyList()
    val myExams = (createdExamsState as? UiState.Success)?.data ?: emptyList()

    if (showQuestionDialog) {
        QuestionEditorDialog(
            existingQuestion = editingQuestion,
            onDismiss = {
                showQuestionDialog = false
                editingQuestion = null
            },
            onSave = { subject, topic, difficulty, qText, optA, optB, optC, optD, correctIdx, explanation, marks, isActive ->
                val currentEditing = editingQuestion
                if (currentEditing == null) {
                    viewModel.addQuestion(
                        subject = subject,
                        topic = topic,
                        difficulty = difficulty,
                        questionText = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctOptionIndex = correctIdx,
                        explanation = explanation,
                        marks = marks,
                        isActive = isActive,
                        onComplete = {
                            showQuestionDialog = false
                            editingQuestion = null
                        }
                    )
                } else {
                    viewModel.updateQuestion(
                        question = currentEditing.copy(
                            subject = subject,
                            topic = topic,
                            difficulty = difficulty,
                            questionText = qText,
                            optionA = optA,
                            optionB = optB,
                            optionC = optC,
                            optionD = optD,
                            correctOptionIndex = correctIdx.toLong(),
                            explanation = explanation,
                            marks = marks.toLong(),
                            isActive = isActive
                        ),
                        onComplete = {
                            showQuestionDialog = false
                            editingQuestion = null
                        }
                    )
                }
            }
        )
    }

    if (showExamDialog) {
        ExamEditorDialog(
            existingExam = editingExam,
            onDismiss = {
                showExamDialog = false
                editingExam = null
            },
            onSave = { title, subject, desc, duration, totalQ, negMark, passPct, isPublished, allowRetake ->
                val currentEditing = editingExam
                if (currentEditing == null) {
                    viewModel.createExam(
                        title = title,
                        subject = subject,
                        description = desc,
                        durationMinutes = duration,
                        totalQuestions = totalQ,
                        negativeMarkPerWrong = negMark,
                        passPercentage = passPct,
                        isPublished = isPublished,
                        allowRetake = allowRetake,
                        onComplete = {
                            showExamDialog = false
                            editingExam = null
                        }
                    )
                } else {
                    viewModel.updateExam(
                        exam = currentEditing.copy(
                            title = title,
                            subject = subject,
                            description = desc,
                            durationMinutes = duration.toLong(),
                            totalQuestions = totalQ.toLong(),
                            negativeMarkPerWrong = negMark.toLong(),
                            passPercentage = passPct.toLong(),
                            isPublished = isPublished,
                            allowRetake = allowRetake
                        ),
                        onComplete = {
                            showExamDialog = false
                            editingExam = null
                        }
                    )
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("server_control_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Control Dashboard Header
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "সার্ভার ও অ্যাডমিন কন্ট্রোল প্যানেল",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "প্রশ্ন অ্যাড, আপডেট, হাইড/আনহাইড এবং লাইভ পরীক্ষা নিয়ন্ত্রণ করুন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                editingQuestion = null
                                showQuestionDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_add_question_dialog_button")
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("প্রশ্ন অ্যাড করুন")
                        }

                        Button(
                            onClick = {
                                editingExam = null
                                showExamDialog = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_create_exam_dialog_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("পরীক্ষা তৈরি করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.seedSampleData() },
                        enabled = !isBusy,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সার্ভারে ৮টি নমুনা প্রশ্ন ও ২টি মডেল টেস্ট অটো-লোড করুন")
                    }
                }
            }
        }

        // Sub-Tabs: Question Management vs Exam Control
        item {
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("প্রশ্ন নিয়ন্ত্রণ (${myQuestions.size})") }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("পরীক্ষা কন্ট্রোল (${myExams.size})") }
                )
            }
        }

        if (selectedSubTab == 0) {
            if (myQuestions.isEmpty()) {
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
                                text = "আপনি এখনো কোনো প্রশ্ন যুক্ত করেননি",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উপরে ‘প্রশ্ন অ্যাড করুন’ বাটনে ট্যাপ করে নতুন এমসিকিউ প্রশ্ন সার্ভারে যোগ করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(myQuestions, key = { it.id }) { question ->
                    ServerQuestionManageCard(
                        question = question,
                        onEdit = {
                            editingQuestion = question
                            showQuestionDialog = true
                        },
                        onToggleActive = { viewModel.toggleQuestionActive(question) },
                        onDelete = { viewModel.deleteQuestion(question.id) }
                    )
                }
            }
        } else {
            if (myExams.isEmpty()) {
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
                                text = "আপনি এখনো কোনো এমসিকিউ পরীক্ষা তৈরি করেননি",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উপরে ‘পরীক্ষা তৈরি করুন’ বাটনে ট্যাপ করে সময়সীমা ও নেগেটিভ মার্কিংসহ নতুন পরীক্ষা চালু করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(myExams, key = { it.id }) { exam ->
                    ServerExamManageCard(
                        exam = exam,
                        onEdit = {
                            editingExam = exam
                            showExamDialog = true
                        },
                        onTogglePublished = { viewModel.toggleExamPublished(exam) },
                        onDelete = { viewModel.deleteExam(exam.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerQuestionManageCard(
    question: Question,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val optionLabels = listOf("ক", "খ", "গ", "ঘ")
    val correctLabel = optionLabels.getOrElse(question.correctOptionIndex.toInt()) { "ক" }
    val correctText = question.optionsList().getOrElse(question.correctOptionIndex.toInt()) { "" }

    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_question_card_${question.id}")
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
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = question.subject,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = if (question.isActive) Color(0xFFD1FAE5) else Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (question.isActive) "সার্ভারে লাইভ" else "ড্রাফট (লুকানো)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (question.isActive) Color(0xFF065F46) else Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Switch(
                    checked = question.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "সঠিক উত্তর ($correctLabel): $correctText",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "টপিক: ${question.topic} • মান: ${question.marks}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    TextButton(
                        onClick = onEdit,
                        modifier = Modifier.testTag("edit_question_btn_${question.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Question", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("আপডেট")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Question",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerExamManageCard(
    exam: Exam,
    onEdit: () -> Unit,
    onTogglePublished: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("server_exam_card_${exam.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (exam.isPublished) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (exam.isPublished) "লাইভ পরীক্ষা (Published)" else "লক করা (Unpublished)",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (exam.isPublished) Color(0xFF065F46) else Color(0xFF991B1B),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Switch(
                    checked = exam.isPublished,
                    onCheckedChange = { onTogglePublished() }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = exam.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "বিষয়: ${exam.subject} • সময়: ${exam.durationMinutes} মিনিট • প্রশ্ন: ${exam.totalQuestions} টি",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag("edit_exam_btn_${exam.id}")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Exam", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("কন্ট্রোল / আপডেট")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Exam",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionEditorDialog(
    existingQuestion: Question?,
    onDismiss: () -> Unit,
    onSave: (
        subject: String,
        topic: String,
        difficulty: String,
        questionText: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctOptionIndex: Int,
        explanation: String,
        marks: Int,
        isActive: Boolean
    ) -> Unit
) {
    var subject by rememberSaveable { mutableStateOf(existingQuestion?.subject ?: "সাধারণ জ্ঞান") }
    var topic by rememberSaveable { mutableStateOf(existingQuestion?.topic ?: "বাংলাদেশ বিষয়াবলি") }
    var difficulty by rememberSaveable { mutableStateOf(existingQuestion?.difficulty ?: "medium") }
    var questionText by rememberSaveable { mutableStateOf(existingQuestion?.questionText ?: "") }
    var optionA by rememberSaveable { mutableStateOf(existingQuestion?.optionA ?: "") }
    var optionB by rememberSaveable { mutableStateOf(existingQuestion?.optionB ?: "") }
    var optionC by rememberSaveable { mutableStateOf(existingQuestion?.optionC ?: "") }
    var optionD by rememberSaveable { mutableStateOf(existingQuestion?.optionD ?: "") }
    var correctIndex by rememberSaveable { mutableIntStateOf(existingQuestion?.correctOptionIndex?.toInt() ?: 0) }
    var explanation by rememberSaveable { mutableStateOf(existingQuestion?.explanation ?: "") }
    var marksText by rememberSaveable { mutableStateOf((existingQuestion?.marks ?: 1L).toString()) }
    var isActive by rememberSaveable { mutableStateOf(existingQuestion?.isActive ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val presetSubjects = listOf("সাধারণ জ্ঞান", "বাংলা সাহিত্য", "ইংরেজি", "গণিত", "বিজ্ঞান ও প্রযুক্তি")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingQuestion == null) "নতুন এমসিকিউ প্রশ্ন অ্যাড করুন"
                else "এমসিকিউ প্রশ্ন আপডেট করুন",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "বিষয় নির্বাচন বা টাইপ করুন:",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetSubjects.forEach { preset ->
                        FilterChip(
                            selected = subject == preset,
                            onClick = { subject = preset },
                            label = { Text(preset) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("বিষয় (Subject)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("অধ্যায় / টপিক (Topic)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("প্রশ্ন (Question Text)") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("question_text_input")
                )

                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("অপশন ক (Option A)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("option_a_input")
                )
                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("অপশন খ (Option B)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("option_b_input")
                )
                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("অপশন গ (Option C)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("option_c_input")
                )
                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("অপশন ঘ (Option D)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("option_d_input")
                )

                Text(
                    text = "সঠিক উত্তর কোনটি?",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("ক" to 0, "খ" to 1, "গ" to 2, "ঘ" to 3).forEach { (label, idx) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = correctIndex == idx,
                                onClick = { correctIndex = idx }
                            )
                            Text(label)
                        }
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("ব্যাখ্যা / সমাধান নোট (ঐচ্ছিক)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = marksText,
                        onValueChange = { marksText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("নম্বর (Marks)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("লাইভ?")
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isBlank() || topic.isBlank() || questionText.isBlank() ||
                        optionA.isBlank() || optionB.isBlank() || optionC.isBlank() || optionD.isBlank()
                    ) {
                        validationError = "অনুগ্রহ করে প্রশ্ন এবং চারটি অপশনই পূরণ করুন।"
                        return@Button
                    }
                    val marks = marksText.toIntOrNull()?.coerceIn(1, 10) ?: 1
                    onSave(
                        subject,
                        topic,
                        difficulty,
                        questionText,
                        optionA,
                        optionB,
                        optionC,
                        optionD,
                        correctIndex,
                        explanation,
                        marks,
                        isActive
                    )
                },
                modifier = Modifier.testTag("save_question_button")
            ) {
                Text(if (existingQuestion == null) "সার্ভারে অ্যাড করুন" else "আপডেট সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
fun ExamEditorDialog(
    existingExam: Exam?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        subject: String,
        description: String,
        durationMinutes: Int,
        totalQuestions: Int,
        negativeMarkPerWrong: Int,
        passPercentage: Int,
        isPublished: Boolean,
        allowRetake: Boolean
    ) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(existingExam?.title ?: "") }
    var subject by rememberSaveable { mutableStateOf(existingExam?.subject ?: "সব বিষয়") }
    var description by rememberSaveable { mutableStateOf(existingExam?.description ?: "") }
    var durationText by rememberSaveable { mutableStateOf((existingExam?.durationMinutes ?: 15L).toString()) }
    var totalQuestionsText by rememberSaveable { mutableStateOf((existingExam?.totalQuestions ?: 10L).toString()) }
    var negativeMarkText by rememberSaveable { mutableStateOf((existingExam?.negativeMarkPerWrong ?: 25L).toString()) }
    var passPercentText by rememberSaveable { mutableStateOf((existingExam?.passPercentage ?: 40L).toString()) }
    var isPublished by rememberSaveable { mutableStateOf(existingExam?.isPublished ?: true) }
    var allowRetake by rememberSaveable { mutableStateOf(existingExam?.allowRetake ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingExam == null) "সার্ভারে নতুন এমসিকিউ পরীক্ষা তৈরি করুন"
                else "পরীক্ষার সেটিংস আপডেট করুন",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("পরীক্ষার নাম (Exam Title)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_title_input")
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("বিষয় (সব বিষয় অথবা নির্দিষ্ট বিষয়)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("নির্দেশনা / সিলেবাস বিবরণ") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("সময় (মিনিট)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = totalQuestionsText,
                        onValueChange = { totalQuestionsText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("মোট প্রশ্ন") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = negativeMarkText,
                        onValueChange = { negativeMarkText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("নেগেটিভ % (25 = 0.25)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = passPercentText,
                        onValueChange = { passPercentText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("পাস মার্ক (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("স্টুডেন্টদের জন্য লাইভ (Published)")
                    Switch(checked = isPublished, onCheckedChange = { isPublished = it })
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || subject.isBlank()) {
                        validationError = "পরীক্ষার নাম এবং বিষয় লিখুন।"
                        return@Button
                    }
                    val duration = durationText.toIntOrNull()?.coerceIn(1, 180) ?: 15
                    val totalQ = totalQuestionsText.toIntOrNull()?.coerceIn(1, 100) ?: 10
                    val negMark = negativeMarkText.toIntOrNull()?.coerceIn(0, 100) ?: 25
                    val passPct = passPercentText.toIntOrNull()?.coerceIn(1, 100) ?: 40
                    onSave(title, subject, description, duration, totalQ, negMark, passPct, isPublished, allowRetake)
                },
                modifier = Modifier.testTag("save_exam_button")
            ) {
                Text(if (existingExam == null) "পরীক্ষা চালু করুন" else "আপডেট সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
