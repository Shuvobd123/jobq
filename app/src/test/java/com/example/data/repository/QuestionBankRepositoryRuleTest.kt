package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Exam
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionBankRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveActiveQuestions_authenticatedUser_succeeds() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = QuestionBankRepository(firestore)

        val questionId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createQuestion(
                subject = "সাধারণ জ্ঞান",
                topic = "মুক্তিযুদ্ধ",
                difficulty = "medium",
                questionText = "বাংলাদেশের মুক্তিযুদ্ধে কয়টি সেক্টর ছিল?",
                optionA = "১০টি",
                optionB = "১১টি",
                optionC = "১২টি",
                optionD = "৯টি",
                correctOptionIndex = 1,
                explanation = "মুক্তিযুদ্ধে বাংলাদেশকে ১১টি সেক্টরে ভাগ করা হয়েছিল।",
                marks = 1,
                isActive = true
            ).getOrThrow()
        }
        assertTrue(questionId.isNotEmpty())

        val activeList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeActiveQuestions().first { list -> list.any { it.id == questionId } }
        }
        assertTrue(activeList.any { it.id == questionId && it.authorId == uid })
    }

    @Test
    fun updateQuestion_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceRepo = QuestionBankRepository(firestore)
        val questionId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createQuestion(
                subject = "গণিত",
                topic = "বীজগণিত",
                difficulty = "easy",
                questionText = "x + 5 = 12 হলে x এর মান কত?",
                optionA = "5",
                optionB = "6",
                optionC = "7",
                optionD = "8",
                correctOptionIndex = 2,
                explanation = "12 - 5 = 7",
                marks = 1,
                isActive = true
            ).getOrThrow()
        }

        val fetchedQuestion = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.getQuestionById(questionId).getOrThrow()
        }
        assertNotNull(fetchedQuestion)

        // Sign in as Bob and attempt to update Alice's question
        signInTestUser(BOB_EMAIL)
        val bobRepo = QuestionBankRepository(firestore)
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.updateQuestion(fetchedQuestion!!.copy(questionText = "Unauthorized edit"))
        }
        assertTrue(updateResult.isFailure)
        val firestoreEx = updateResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun submitAndGetAttempt_crossUserIsolation_enforced() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = QuestionBankRepository(firestore)

        val examId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createExam(
                title = "বিসিএস প্রিলিমিনারি মডেল টেস্ট",
                subject = "সব বিষয়",
                description = "পূর্ণাঙ্গ মডেল টেস্ট",
                durationMinutes = 15,
                totalQuestions = 10,
                negativeMarkPerWrong = 25,
                passPercentage = 40,
                isPublished = true,
                allowRetake = true
            ).getOrThrow()
        }

        val sampleExam = Exam(
            id = examId,
            creatorId = aliceUid,
            creatorName = "Alice",
            title = "বিসিএস প্রিলিমিনারি মডেল টেস্ট",
            subject = "সব বিষয়"
        )

        val attemptId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.submitExamAttempt(
                exam = sampleExam,
                totalQuestionsCount = 10,
                answeredCount = 10,
                correctCount = 8,
                wrongCount = 2,
                scoreHundredths = 750,
                maxMarks = 10,
                accuracyPercent = 80,
                passed = true,
                timeTakenSeconds = 180
            ).getOrThrow()
        }
        assertTrue(attemptId.isNotEmpty())

        // Bob cannot read Alice's exam attempt
        signInTestUser(BOB_EMAIL)
        val bobRepo = QuestionBankRepository(firestore)
        val bobReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getAttemptById(attemptId)
        }
        assertTrue(bobReadResult.isFailure)
        val firestoreEx = bobReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeActiveQuestions_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = QuestionBankRepository(firestore)

        var caughtException: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeActiveQuestions().first()
            }
        } catch (e: Throwable) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val firestoreEx = caughtException?.unwrapFirestoreException()
        assertNotNull(firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
