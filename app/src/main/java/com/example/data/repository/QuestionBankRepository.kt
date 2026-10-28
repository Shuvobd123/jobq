package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Exam
import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuestionBankRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun currentUserName(): String {
        val display = auth.currentUser?.displayName?.trim()
        if (!display.isNullOrEmpty()) return display.take(80)
        val emailPrefix = auth.currentUser?.email?.substringBefore("@")?.trim()
        if (!emailPrefix.isNullOrEmpty()) return emailPrefix.take(80)
        return "Instructor"
    }

    // --- REALTIME STREAMS (Client Queries Aligned With Security Rules) ---

    /**
     * Observes all active questions for students taking exams or practicing in the Question Bank.
     * Security rule alignment: `existing().isActive == true`
     */
    fun observeActiveQuestions(): Flow<List<Question>> {
        val path = "questions"
        return db.collection(path)
            .whereEqualTo("isActive", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Question::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Observes all questions authored by the given instructor/admin (both active and inactive drafts).
     * Security rule alignment: `existing().authorId == request.auth.uid`
     */
    fun observeAuthoredQuestions(authorId: String): Flow<List<Question>> {
        val path = "questions"
        return db.collection(path)
            .whereEqualTo("authorId", authorId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Question::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Observes published exams available to students.
     * Security rule alignment: `existing().isPublished == true`
     */
    fun observePublishedExams(): Flow<List<Exam>> {
        val path = "exams"
        return db.collection(path)
            .whereEqualTo("isPublished", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Exam::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Observes all exams created by the instructor/admin (both published and draft).
     * Security rule alignment: `existing().creatorId == request.auth.uid`
     */
    fun observeCreatedExams(creatorId: String): Flow<List<Exam>> {
        val path = "exams"
        return db.collection(path)
            .whereEqualTo("creatorId", creatorId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(Exam::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    /**
     * Observes a student's own submitted exam attempts.
     * Security rule alignment: `existing().studentId == request.auth.uid`
     */
    fun observeMyAttempts(studentId: String): Flow<List<ExamAttempt>> {
        val path = "attempts"
        return db.collection(path)
            .whereEqualTo("studentId", studentId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(ExamAttempt::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    // --- CRUD OPERATIONS ---

    suspend fun getQuestionById(questionId: String): Result<Question?> = runCatching {
        val docRef = db.collection("questions").document(questionId)
        try {
            val snapshot = docRef.get().await()
            snapshot.toObject(Question::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    suspend fun createQuestion(
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
        marks: Int = 1,
        isActive: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "q_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("questions").document(docId)
        val payload = mapOf(
            "authorId" to uid,
            "authorName" to currentUserName(),
            "subject" to subject.trim().take(60),
            "topic" to topic.trim().take(100),
            "difficulty" to difficulty.lowercase().let { if (it in listOf("easy", "medium", "hard")) it else "medium" },
            "questionText" to questionText.trim().take(600),
            "optionA" to optionA.trim().take(250),
            "optionB" to optionB.trim().take(250),
            "optionC" to optionC.trim().take(250),
            "optionD" to optionD.trim().take(250),
            "correctOptionIndex" to correctOptionIndex.coerceIn(0, 3),
            "explanation" to explanation.trim().take(600),
            "marks" to marks.coerceIn(1, 10),
            "isActive" to isActive,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateQuestion(question: Question): Result<Unit> = runCatching {
        requireUserId()
        require(question.id.isNotBlank()) { "Question ID must not be blank for update" }
        requireNotNull(question.createdAt) { "Question createdAt must not be null for update" }
        val docRef = db.collection("questions").document(question.id)
        try {
            docRef.set(question.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteQuestion(questionId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("questions").document(questionId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun createExam(
        title: String,
        subject: String,
        description: String,
        durationMinutes: Int,
        totalQuestions: Int,
        negativeMarkPerWrong: Int = 25,
        passPercentage: Int = 40,
        isPublished: Boolean = true,
        allowRetake: Boolean = true
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "ex_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("exams").document(docId)
        val payload = mapOf(
            "creatorId" to uid,
            "creatorName" to currentUserName(),
            "title" to title.trim().take(120),
            "subject" to subject.trim().take(60),
            "description" to description.trim().take(400),
            "durationMinutes" to durationMinutes.coerceIn(1, 180),
            "totalQuestions" to totalQuestions.coerceIn(1, 100),
            "negativeMarkPerWrong" to negativeMarkPerWrong.coerceIn(0, 100),
            "passPercentage" to passPercentage.coerceIn(1, 100),
            "isPublished" to isPublished,
            "allowRetake" to allowRetake,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateExam(exam: Exam): Result<Unit> = runCatching {
        requireUserId()
        require(exam.id.isNotBlank()) { "Exam ID must not be blank for update" }
        requireNotNull(exam.createdAt) { "Exam createdAt must not be null for update" }
        val docRef = db.collection("exams").document(exam.id)
        try {
            docRef.set(exam.copy(updatedAt = null)).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteExam(examId: String): Result<Unit> = runCatching {
        requireUserId()
        val docRef = db.collection("exams").document(examId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun submitExamAttempt(
        exam: Exam,
        totalQuestionsCount: Int,
        answeredCount: Int,
        correctCount: Int,
        wrongCount: Int,
        scoreHundredths: Int,
        maxMarks: Int,
        accuracyPercent: Int,
        passed: Boolean,
        timeTakenSeconds: Int
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val docId = "att_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val docRef = db.collection("attempts").document(docId)
        val safeTotal = totalQuestionsCount.coerceIn(1, 100)
        val safeAnswered = answeredCount.coerceIn(0, safeTotal)
        val safeCorrect = correctCount.coerceIn(0, safeAnswered)
        val safeWrong = wrongCount.coerceIn(0, safeAnswered)
        val payload = mapOf(
            "studentId" to uid,
            "studentName" to currentUserName(),
            "examId" to exam.id.ifBlank { "practice_exam" },
            "examTitle" to exam.title.ifBlank { "মডেল টেস্ট" }.take(120),
            "subject" to exam.subject.ifBlank { "সব বিষয়" }.take(60),
            "totalQuestions" to safeTotal,
            "answeredCount" to safeAnswered,
            "correctCount" to safeCorrect,
            "wrongCount" to safeWrong,
            "scoreHundredths" to scoreHundredths.coerceIn(-10000, 100000),
            "maxMarks" to maxMarks.coerceIn(1, 1000),
            "accuracyPercent" to accuracyPercent.coerceIn(0, 100),
            "passed" to passed,
            "timeTakenSeconds" to timeTakenSeconds.coerceIn(0, 10800),
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun getAttemptById(attemptId: String): Result<ExamAttempt?> = runCatching {
        val docRef = db.collection("attempts").document(attemptId)
        try {
            val snapshot = docRef.get().await()
            snapshot.toObject(ExamAttempt::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }
}
