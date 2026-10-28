package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.Exam
import com.example.data.model.ExamAttempt
import com.example.data.model.Question
import com.example.data.repository.QuestionBankRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.screens.ExamHallTab
import com.example.ui.screens.ExamResultSummaryScreen
import com.example.ui.screens.LiveExamRunnerScreen
import com.example.ui.screens.QuestionBankStudyTab
import com.example.ui.screens.ServerControlScreen
import com.example.ui.screens.StudentResultsHistoryTab
import com.example.ui.theme.QuestionBankTheme
import com.example.ui.viewmodel.QuestionBankViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestionBankTheme {
                AppNavigation()
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        MainAuthenticatedApp(
            currentUserId = user.uid,
            userDisplayName = user.displayName ?: user.email ?: "শিক্ষার্থী"
        )
    }
}

private data class NavDestination(
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private sealed interface ActiveExamSession {
    data object Idle : ActiveExamSession
    data class Running(val exam: Exam, val questions: List<Question>) : ActiveExamSession
    data class Completed(
        val attempt: ExamAttempt,
        val questions: List<Question>,
        val selectedAnswers: Map<String, Int>
    ) : ActiveExamSession
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAuthenticatedApp(
    currentUserId: String,
    userDisplayName: String
) {
    val viewModel: QuestionBankViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                QuestionBankViewModel(QuestionBankRepository(db), currentUserId)
            }
        }
    )

    val activeQuestionsState by viewModel.activeQuestionsState.collectAsStateWithLifecycle()
    val authoredQuestionsState by viewModel.authoredQuestionsState.collectAsStateWithLifecycle()
    val publishedExamsState by viewModel.publishedExamsState.collectAsStateWithLifecycle()
    val createdExamsState by viewModel.createdExamsState.collectAsStateWithLifecycle()
    val myAttemptsState by viewModel.myAttemptsState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var examSession by remember { mutableStateOf<ActiveExamSession>(ActiveExamSession.Idle) }
    var questionToEditFromBank by remember { mutableStateOf<Question?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(statusMessage) {
        val msg = statusMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    val destinations = remember {
        listOf(
            NavDestination("পরীক্ষা কেন্দ্র", Icons.Default.Quiz, "nav_exam_hall"),
            NavDestination("প্রশ্নব্যাংক", Icons.AutoMirrored.Filled.MenuBook, "nav_question_bank"),
            NavDestination("ফলাফল", Icons.Default.EmojiEvents, "nav_results"),
            NavDestination("সার্ভার কন্ট্রোল", Icons.Default.AdminPanelSettings, "nav_server_control")
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (examSession is ActiveExamSession.Idle) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Question Bank & MCQ Hub",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "ইউজার: $userDisplayName",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {},
                                        scope = coroutineScope
                                    )
                                },
                                modifier = Modifier.testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Sign Out"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (!isExpandedScreen && examSession is ActiveExamSession.Idle) {
                    NavigationBar {
                        destinations.forEachIndexed { index, dest ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label, maxLines = 1) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen && examSession is ActiveExamSession.Idle) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        destinations.forEachIndexed { index, dest ->
                            NavigationRailItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (val session = examSession) {
                        is ActiveExamSession.Running -> {
                            LiveExamRunnerScreen(
                                exam = session.exam,
                                questions = session.questions,
                                isSubmitting = isBusy,
                                onCancelExam = { examSession = ActiveExamSession.Idle },
                                onSubmitExam = { answers, spentSeconds ->
                                    viewModel.submitExamAttempt(
                                        exam = session.exam,
                                        questions = session.questions,
                                        selectedAnswers = answers,
                                        timeTakenSeconds = spentSeconds,
                                        onSubmitted = { resultAttempt ->
                                            examSession = ActiveExamSession.Completed(
                                                attempt = resultAttempt,
                                                questions = session.questions,
                                                selectedAnswers = answers
                                            )
                                        }
                                    )
                                }
                            )
                        }
                        is ActiveExamSession.Completed -> {
                            ExamResultSummaryScreen(
                                attempt = session.attempt,
                                questions = session.questions,
                                selectedAnswers = session.selectedAnswers,
                                onBackToExamHall = {
                                    examSession = ActiveExamSession.Idle
                                    selectedTab = 0
                                }
                            )
                        }
                        is ActiveExamSession.Idle -> {
                            when (selectedTab) {
                                0 -> ExamHallTab(
                                    publishedExamsState = publishedExamsState,
                                    activeQuestionsState = activeQuestionsState,
                                    isBusy = isBusy,
                                    onSeedSampleData = { viewModel.seedSampleData() },
                                    onStartExam = { exam, questions ->
                                        examSession = ActiveExamSession.Running(exam, questions)
                                    },
                                    onNavigateToServerControl = { selectedTab = 3 }
                                )
                                1 -> QuestionBankStudyTab(
                                    activeQuestionsState = activeQuestionsState,
                                    isBusy = isBusy,
                                    onSeedSampleData = { viewModel.seedSampleData() },
                                    onEditQuestion = { q ->
                                        questionToEditFromBank = q
                                        selectedTab = 3
                                    },
                                    currentUserId = currentUserId
                                )
                                2 -> StudentResultsHistoryTab(
                                    myAttemptsState = myAttemptsState
                                )
                                3 -> ServerControlScreen(
                                    viewModel = viewModel,
                                    authoredQuestionsState = authoredQuestionsState,
                                    createdExamsState = createdExamsState,
                                    isBusy = isBusy,
                                    initialQuestionToEdit = questionToEditFromBank,
                                    onClearInitialQuestionToEdit = { questionToEditFromBank = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
