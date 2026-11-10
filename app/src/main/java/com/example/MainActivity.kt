package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.DocumentType
import com.example.data.model.OfficeDocument
import com.example.ui.components.NaraChatBottomSheet
import com.example.ui.components.NovaBottomNav
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.files.NovaFilesScreen
import com.example.ui.nara.NaraHubScreen
import com.example.ui.onboarding.OnboardingDialog
import com.example.ui.pdf.NovaPdfScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.sheets.NovaSheetsScreen
import com.example.ui.slides.NovaSlidesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NovaBluePrimary
import com.example.ui.theme.NovaNavyDark
import com.example.ui.theme.NovaWriterBlue
import com.example.ui.tools.NovaToolsScreen
import com.example.ui.viewmodel.NovaOfficeViewModel
import com.example.ui.writer.NovaWriterScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: NovaOfficeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.settingsRepository.theme.collectAsState()
            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                NovaOfficeApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaOfficeApp(viewModel: NovaOfficeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isNaraSheetOpen by viewModel.isNaraSheetOpen.collectAsState()
    val feedbackMsg by viewModel.userFeedbackMessage.collectAsState()
    val onboardingCompleted by viewModel.settingsRepository.onboardingCompleted.collectAsState()

    var showOnboarding by remember { mutableStateOf(!onboardingCompleted) }
    var isSplashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(900) // Brief executive splash screen
        isSplashVisible = false
    }

    LaunchedEffect(feedbackMsg) {
        if (feedbackMsg != null) {
            delay(3000)
            viewModel.clearFeedback()
        }
    }

    if (isSplashVisible) {
        NovaSplashScreen()
    } else {
        val showBottomNav = currentRoute in listOf(
            com.example.ui.navigation.Screen.Dashboard.route,
            com.example.ui.navigation.Screen.Files.route,
            com.example.ui.navigation.Screen.Tools.route,
            com.example.ui.navigation.Screen.NaraHub.route,
            com.example.ui.navigation.Screen.Settings.route
        )

        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    NovaBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(com.example.ui.navigation.Screen.Dashboard.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = com.example.ui.navigation.Screen.Dashboard.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Dashboard Home
                    composable(com.example.ui.navigation.Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToDocument = { doc ->
                                navigateToDocumentEditor(navController, doc)
                            },
                            onNavigateToRoute = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    // Files Manager
                    composable(com.example.ui.navigation.Screen.Files.route) {
                        NovaFilesScreen(
                            viewModel = viewModel,
                            onNavigateToDocument = { doc ->
                                navigateToDocumentEditor(navController, doc)
                            }
                        )
                    }

                    // Productivity Tools
                    composable(com.example.ui.navigation.Screen.Tools.route) {
                        NovaToolsScreen(
                            viewModel = viewModel,
                            onNavigateToRoute = { route ->
                                navController.navigate(route)
                            }
                        )
                    }

                    // NARA AI Novel Studio
                    composable(com.example.ui.navigation.Screen.NaraHub.route) {
                        NaraHubScreen(viewModel = viewModel)
                    }

                    // Settings & API Configuration
                    composable(com.example.ui.navigation.Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }

                    // Nova Writer Editor
                    composable(
                        route = com.example.ui.navigation.Screen.Writer.ROUTE,
                        arguments = listOf(navArgument("docId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: ""
                        NovaWriterScreen(
                            docId = docId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Nova Sheets Editor
                    composable(
                        route = com.example.ui.navigation.Screen.Sheets.ROUTE,
                        arguments = listOf(navArgument("docId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: ""
                        NovaSheetsScreen(
                            docId = docId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Nova Slides Editor
                    composable(
                        route = com.example.ui.navigation.Screen.Slides.ROUTE,
                        arguments = listOf(navArgument("docId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: ""
                        NovaSlidesScreen(
                            docId = docId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Nova PDF Viewer & Annotator
                    composable(
                        route = com.example.ui.navigation.Screen.Pdf.ROUTE,
                        arguments = listOf(navArgument("docId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val docId = backStackEntry.arguments?.getString("docId") ?: ""
                        NovaPdfScreen(
                            docId = docId,
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                // Global Top User Feedback Banner
                AnimatedVisibility(
                    visible = feedbackMsg != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = NovaNavyDark),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF00D2FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = feedbackMsg ?: "",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.clearFeedback() },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Tutup",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global Floating NARA AI Bottom Sheet
        if (isNaraSheetOpen) {
            NaraChatBottomSheet(
                viewModel = viewModel,
                onDismiss = { viewModel.setNaraSheetOpen(false) }
            )
        }

        // First-launch Onboarding Dialog
        if (showOnboarding) {
            OnboardingDialog(
                onDismiss = {
                    showOnboarding = false
                    viewModel.settingsRepository.setOnboardingCompleted(true)
                }
            )
        }
    }
}

private fun navigateToDocumentEditor(navController: NavHostController, doc: OfficeDocument) {
    val route = when (doc.type) {
        DocumentType.WRITER, DocumentType.TXT -> com.example.ui.navigation.Screen.Writer.createRoute(doc.id)
        DocumentType.SHEETS -> com.example.ui.navigation.Screen.Sheets.createRoute(doc.id)
        DocumentType.SLIDES -> com.example.ui.navigation.Screen.Slides.createRoute(doc.id)
        DocumentType.PDF -> com.example.ui.navigation.Screen.Pdf.createRoute(doc.id)
    }
    navController.navigate(route)
}

@Composable
fun NovaSplashScreen() {
    Surface(
        color = NovaNavyDark,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF1E3E62), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "N",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF00D2FF)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "NOVA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OFFICE",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        color = Color(0xFF00D2FF),
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Complete Office & Creative Workspace",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(30.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = Color(0xFF00D2FF)
                )
            }
        }
    }
}
