package com.example.presentation.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Receipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToScan: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onViewReceiptDetail: (Receipt) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Smart Scanner",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Text(
                            text = "Simplify your expense tracking",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onNavigateToDashboard) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Analysis Dashboard",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "All History"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("scan_floating_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Scan Receipt"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Scan", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Panel Row
            item {
                StatsPanel(
                    totalSpend = uiState.totalMonthlySpend,
                    totalScans = uiState.totalScansCount,
                    currency = uiState.currency,
                    onViewDashboard = onNavigateToDashboard
                )
            }

            // Recent Scans Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (uiState.recentScans.isNotEmpty()) {
                        TextButton(onClick = onNavigateToHistory) {
                            Text("See All")
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Recent Scans List or Empty State
            if (uiState.recentScans.isEmpty()) {
                item {
                    HomeEmptyState(onNavigateToScan)
                }
            } else {
                items(uiState.recentScans, key = { it.timestamp }) { receipt ->
                    ReceiptRowItem(
                        receipt = receipt,
                        onItemClick = { onViewReceiptDetail(receipt) },
                        onDeleteClick = { viewModel.deleteReceipt(receipt) }
                    )
                }
            }

            // Bottom Spacing for navigation overlays
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

data class BarData(val label: String, val heightPercentage: Float, val isActive: Boolean)

@Composable
fun StatsPanel(
    totalSpend: Double,
    totalScans: Int,
    currency: String,
    onViewDashboard: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDashboard)
            .testTag("summary_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Spending",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%s%.2f", currency, totalSpend),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Green badge "+12% vs last month"
                Box(
                    modifier = Modifier
                        .background(
                            color = com.example.ui.theme.Green100,
                            shape = RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "+12% vs last month",
                        style = MaterialTheme.typography.labelSmall,
                        color = com.example.ui.theme.Green700,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Mini Week Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val days = listOf(
                    BarData("Mon", 0.35f, false),
                    BarData("Tue", 0.75f, true), // Active Tuesday (highlighted like template)
                    BarData("Wed", 0.50f, false),
                    BarData("Thu", 0.85f, false),
                    BarData("Fri", 0.60f, false),
                    BarData("Sat", 0.20f, false),
                    BarData("Sun", 0.40f, false)
                )

                days.forEach { day ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(day.heightPercentage)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        if (day.isActive) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = day.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (day.isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (day.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReceiptRowItem(
    receipt: Receipt,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("receipt_item_${receipt.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = getCategoryColor(receipt.category).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(receipt.category),
                    contentDescription = receipt.category,
                    tint = getCategoryColor(receipt.category)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Merchant details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = receipt.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = receipt.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Mini Category chip
                    Box(
                        modifier = Modifier
                            .background(
                                color = getCategoryColor(receipt.category).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = receipt.category,
                            fontSize = 10.sp,
                            color = getCategoryColor(receipt.category),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Total price
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = String.format(Locale.getDefault(), "%s%.2f", receipt.currency, receipt.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                IconButton(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Scan",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Delete Receipt?") },
            text = { Text("Are you sure you want to delete this scan for ${receipt.merchant}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showConfirmDelete = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HomeEmptyState(
    onScanClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Receipts Scanned",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Scan your first receipt to extract total, item matrix, merchant, tax, and categories instantly.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onScanClick,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Scan Receipt Now")
        }
    }
}

// Global category icons dictionary
fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase(Locale.getDefault()).trim()) {
        "food" -> Icons.Default.Restaurant
        "groceries" -> Icons.Default.LocalGroceryStore
        "travel", "transportation" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.LocalMall
        "entertainment" -> Icons.Default.ConfirmationNumber
        "utilities" -> Icons.Default.ElectricBolt
        "other", "miscellaneous" -> Icons.Default.Category
        else -> Icons.Default.Category
    }
}

// Global category theme colored indicator
fun getCategoryColor(category: String): Color {
    return when (category.lowercase(Locale.getDefault()).trim()) {
        "food" -> Color(0xFFFF5722) // Coral orange
        "groceries" -> Color(0xFF4CAF50) // Emerald green
        "travel", "transportation" -> Color(0xFF2196F3) // Sky blue
        "shopping" -> Color(0xFF9C27B0) // Purple
        "entertainment" -> Color(0xFFFF9800) // Vibrant orange
        "utilities" -> Color(0xFF00BCD4) // Teal
        else -> Color(0xFF757575) // Cool gray
    }
}
