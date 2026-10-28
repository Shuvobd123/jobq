package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Exam
import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.example.data.model.UiState
import com.example.data.repository.QuestionBankRepository
import com.example.data.repository.SampleDataSeeder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuestionBankViewModel(
    private val repository: QuestionBankRepository,
    val currentUserId: String
) : ViewModel() {

    val activeQuestionsState: StateFlow<UiState<List<Question>>> = repository.observeActiveQuestions()
        .map<List<Question>, UiState<List<Question>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing active questions", error)
            emit(UiState.Error(error.localizedMessage ?: "প্রশ্নব্যাংক লোড করা যায়নি"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val authoredQuestionsState: StateFlow<UiState<List<Question>>> = repository.observeAuthoredQuestions(currentUserId)
        .map<List<Question>, UiState<List<Question>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing authored questions", error)
            emit(UiState.Error(error.localizedMessage ?: "আপনার প্রশ্ন তালিকা লোড করা যায়নি"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val publishedExamsState: StateFlow<UiState<List<Exam>>> = repository.observePublishedExams()
        .map<List<Exam>, UiState<List<Exam>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing published exams", error)
            emit(UiState.Error(error.localizedMessage ?: "পরীক্ষার তালিকা লোড করা যায়নি"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val createdExamsState: StateFlow<UiState<List<Exam>>> = repository.observeCreatedExams(currentUserId)
        .map<List<Exam>, UiState<List<Exam>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing created exams", error)
            emit(UiState.Error(error.localizedMessage ?: "সার্ভার পরীক্ষার তালিকা লোড করা যায়নি"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val myAttemptsState: StateFlow<UiState<List<ExamAttempt>>> = repository.observeMyAttempts(currentUserId)
        .map<List<ExamAttempt>, UiState<List<ExamAttempt>>> { UiState.Success(it) }
        .catch { error ->
            Log.w(TAG, "Error observing student attempts", error)
            emit(UiState.Error(error.localizedMessage ?: "ফলাফল ইতিহাস লোড করা যায়নি"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun seedSampleData() {
        if (_isBusy.value) return
        _isBusy.value = true
        viewModelScope.launch {
            val result = SampleDataSeeder.seedServerBankIfNeeded(repository)
            _isBusy.value = false
            result.fold(
                onSuccess = { count ->
                    _statusMessage.value = "সার্ভারে $count টি নমুনা এমসিকিউ প্রশ্ন ও ২টি মডেল টেস্ট সফলভাবে যুক্ত হয়েছে!"
                },
                onFailure = { err ->
                    _statusMessage.value = "নমুনা প্রশ্ন লোড করতে সমস্যা হয়েছে: ${err.localizedMessage}"
                }
            )
        }
    }

    fun addQuestion(
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
        isActive: Boolean,
        onComplete: () -> Unit = {}
    ) {
        _isBusy.value = true
        viewModelScope.launch {
            val result = repository.createQuestion(
                subject = subject,
                topic = topic,
                difficulty = difficulty,
                questionText = questionText,
                optionA = optionA,
                optionB = optionB,
                optionC = optionC,
                optionD = optionD,
                correctOptionIndex = correctOptionIndex,
                explanation = explanation,
                marks = marks,
                isActive = isActive
            )
            _isBusy.value = false
            result.fold(
                onSuccess = {
                    _statusMessage.value = "নতুন প্রশ্ন সার্ভারে সফলভাবে সংরক্ষিত হয়েছে!"
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "প্রশ্ন সেভ করা যায়নি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateQuestion(question: Question, onComplete: () -> Unit = {}) {
        _isBusy.value = true
        viewModelScope.launch {
            val result = repository.updateQuestion(question)
            _isBusy.value = false
            result.fold(
                onSuccess = {
                    _statusMessage.value = "প্রশ্নটি সার্ভারে আপডেট করা হয়েছে!"
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "প্রশ্ন আপডেট ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun toggleQuestionActive(question: Question) {
        viewModelScope.launch {
            val updated = question.copy(isActive = !question.isActive)
            repository.updateQuestion(updated).fold(
                onSuccess = {
                    val stateLabel = if (updated.isActive) "অ্যাক্টিভ (লাইভ)" else "ড্রাফট (লুকানো)"
                    _statusMessage.value = "প্রশ্ন স্ট্যাটাস: $stateLabel"
                },
                onFailure = { err ->
                    _statusMessage.value = "স্ট্যাটাস পরিবর্তন ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun deleteQuestion(questionId: String) {
        viewModelScope.launch {
            repository.deleteQuestion(questionId).fold(
                onSuccess = {
                    _statusMessage.value = "প্রশ্নটি সার্ভার থেকে মুছে ফেলা হয়েছে।"
                },
                onFailure = { err ->
                    _statusMessage.value = "প্রশ্ন মুছতে সমস্যা হয়েছে: ${err.localizedMessage}"
                }
            )
        }
    }

    fun createExam(
        title: String,
        subject: String,
        description: String,
        durationMinutes: Int,
        totalQuestions: Int,
        negativeMarkPerWrong: Int,
        passPercentage: Int,
        isPublished: Boolean,
        allowRetake: Boolean,
        onComplete: () -> Unit = {}
    ) {
        _isBusy.value = true
        viewModelScope.launch {
            val result = repository.createExam(
                title = title,
                subject = subject,
                description = description,
                durationMinutes = durationMinutes,
                totalQuestions = totalQuestions,
                negativeMarkPerWrong = negativeMarkPerWrong,
                passPercentage = passPercentage,
                isPublished = isPublished,
                allowRetake = allowRetake
            )
            _isBusy.value = false
            result.fold(
                onSuccess = {
                    _statusMessage.value = "নতুন এমসিকিউ পরীক্ষা সার্ভারে তৈরি হয়েছে!"
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "পরীক্ষা তৈরি করা যায়নি: ${err.localizedMessage}"
                }
            )
        }
    }

    fun updateExam(exam: Exam, onComplete: () -> Unit = {}) {
        _isBusy.value = true
        viewModelScope.launch {
            val result = repository.updateExam(exam)
            _isBusy.value = false
            result.fold(
                onSuccess = {
                    _statusMessage.value = "পরীক্ষার সেটিংস সার্ভারে আপডেট হয়েছে!"
                    onComplete()
                },
                onFailure = { err ->
                    _statusMessage.value = "পরীক্ষা আপডেট ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun toggleExamPublished(exam: Exam) {
        viewModelScope.launch {
            val updated = exam.copy(isPublished = !exam.isPublished)
            repository.updateExam(updated).fold(
                onSuccess = {
                    val label = if (updated.isPublished) "লাইভ (স্টুডেন্টদের জন্য উন্মুক্ত)" else "স্থগিত (লক করা)"
                    _statusMessage.value = "পরীক্ষা স্ট্যাটাস: $label"
                },
                onFailure = { err ->
                    _statusMessage.value = "পরীক্ষা স্ট্যাটাস পরিবর্তন ব্যর্থ: ${err.localizedMessage}"
                }
            )
        }
    }

    fun deleteExam(examId: String) {
        viewModelScope.launch {
            repository.deleteExam(examId).fold(
                onSuccess = {
                    _statusMessage.value = "পরীক্ষাটি সার্ভার থেকে মুছে ফেলা হয়েছে।"
                },
                onFailure = { err ->
                    _statusMessage.value = "পরীক্ষা মুছতে সমস্যা হয়েছে: ${err.localizedMessage}"
                }
            )
        }
    }

    fun submitExamAttempt(
        exam: Exam,
        questions: List<Question>,
        selectedAnswers: Map<String, Int>,
        timeTakenSeconds: Int,
        onSubmitted: (ExamAttempt) -> Unit
    ) {
        _isBusy.value = true
        viewModelScope.launch {
            val totalQ = questions.size.coerceAtLeast(1)
            var answered = 0
            var correct = 0
            var wrong = 0
            var rawMarksHundredths = 0
            var maxMarks = 0

            for (q in questions) {
                val qMarks = q.marks.toInt().coerceAtLeast(1)
                maxMarks += qMarks
                val chosen = selectedAnswers[q.id]
                if (chosen != null) {
                    answered++
                    if (chosen == q.correctOptionIndex.toInt()) {
                        correct++
                        rawMarksHundredths += qMarks * 100
                    } else {
                        wrong++
                        rawMarksHundredths -= exam.negativeMarkPerWrong.toInt().coerceIn(0, 100)
                    }
                }
            }

            val safeMaxMarks = maxMarks.coerceAtLeast(1)
            val accuracy = ((rawMarksHundredths.coerceAtLeast(0).toDouble() / (safeMaxMarks * 100.0)) * 100.0)
                .toInt()
                .coerceIn(0, 100)
            val passed = accuracy >= exam.passPercentage.toInt()

            val result = repository.submitExamAttempt(
                exam = exam,
                totalQuestionsCount = totalQ,
                answeredCount = answered,
                correctCount = correct,
                wrongCount = wrong,
                scoreHundredths = rawMarksHundredths,
                maxMarks = safeMaxMarks,
                accuracyPercent = accuracy,
                passed = passed,
                timeTakenSeconds = timeTakenSeconds
            )
            _isBusy.value = false
            result.fold(
                onSuccess = { attemptId ->
                    val summaryAttempt = ExamAttempt(
                        id = attemptId,
                        studentId = currentUserId,
                        examId = exam.id,
                        examTitle = exam.title,
                        subject = exam.subject,
                        totalQuestions = totalQ.toLong(),
                        answeredCount = answered.toLong(),
                        correctCount = correct.toLong(),
                        wrongCount = wrong.toLong(),
                        scoreHundredths = rawMarksHundredths.toLong(),
                        maxMarks = safeMaxMarks.toLong(),
                        accuracyPercent = accuracy.toLong(),
                        passed = passed,
                        timeTakenSeconds = timeTakenSeconds.toLong()
                    )
                    _statusMessage.value = "পরীক্ষার উত্তরপত্র সার্ভারে জমা হয়েছে!"
                    onSubmitted(summaryAttempt)
                },
                onFailure = { err ->
                    _statusMessage.value = "ফলাফল জমা দিতে সমস্যা হয়েছে: ${err.localizedMessage}"
                }
            )
        }
    }

    private companion object {
        const val TAG = "QuestionBankVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
