package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Question(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val subject: String = "",
    val topic: String = "",
    val difficulty: String = "medium",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctOptionIndex: Long = 0L,
    val explanation: String = "",
    val marks: Long = 1L,
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun optionsList(): List<String> = listOf(optionA, optionB, optionC, optionD)
}

@IgnoreExtraProperties
data class Exam(
    @DocumentId val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val durationMinutes: Long = 15L,
    val totalQuestions: Long = 10L,
    val negativeMarkPerWrong: Long = 25L, // 25 = 0.25 mark penalty per wrong answer
    val passPercentage: Long = 40L,
    val isPublished: Boolean = true,
    val allowRetake: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ExamAttempt(
    @DocumentId val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val examId: String = "",
    val examTitle: String = "",
    val subject: String = "",
    val totalQuestions: Long = 0L,
    val answeredCount: Long = 0L,
    val correctCount: Long = 0L,
    val wrongCount: Long = 0L,
    val scoreHundredths: Long = 0L, // e.g., 850 = 8.50 marks
    val maxMarks: Long = 10L,
    val accuracyPercent: Long = 0L,
    val passed: Boolean = false,
    val timeTakenSeconds: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
