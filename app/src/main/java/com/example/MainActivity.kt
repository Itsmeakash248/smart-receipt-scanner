package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.domain.model.Receipt
import com.example.presentation.ViewModelFactory
import com.example.presentation.dashboard.DashboardScreen
import com.example.presentation.dashboard.DashboardViewModel
import com.example.presentation.history.HistoryScreen
import com.example.presentation.history.HistoryViewModel
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.result.ResultScreen
import com.example.presentation.result.ResultViewModel
import com.example.presentation.scan.ScanScreen
import com.example.presentation.scan.ScanViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val app = application as SmartReceiptScannerApp
                val factory = remember { ViewModelFactory(app) }

                val homeViewModel: HomeViewModel = viewModel(factory = factory)
                val scanViewModel: ScanViewModel = viewModel(factory = factory)
                val resultViewModel: ResultViewModel = viewModel(factory = factory)
                val historyViewModel: HistoryViewModel = viewModel(factory = factory)
                val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)

                val navController = rememberNavController()

                // Shared state tracking representing the receipt being reviewed or inspected
                var activeReviewReceipt by remember { mutableStateOf<Receipt?>(null) }

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onNavigateToScan = {
                                scanViewModel.resetState()
                                navController.navigate("scan")
                            },
                            onNavigateToHistory = {
                                navController.navigate("history")
                            },
                            onNavigateToDashboard = {
                                navController.navigate("dashboard")
                            },
                            onViewReceiptDetail = { receipt ->
                                activeReviewReceipt = receipt
                                navController.navigate("result")
                            }
                        )
                    }

                    composable("scan") {
                        ScanScreen(
                            viewModel = scanViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onAnalysisFinished = { receipt ->
                                activeReviewReceipt = receipt
                                navController.navigate("result") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    }

                    composable("result") {
                        val receiptToReview = activeReviewReceipt
                        if (receiptToReview != null) {
                            ResultScreen(
                                viewModel = resultViewModel,
                                receipt = receiptToReview,
                                onNavigateHome = {
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("history") {
                        HistoryScreen(
                            viewModel = historyViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            },
                            onViewReceiptDetail = { receipt ->
                                activeReviewReceipt = receipt
                                navController.navigate("result")
                            }
                        )
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            onNavigateBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
