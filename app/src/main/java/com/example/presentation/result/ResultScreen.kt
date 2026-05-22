package com.example.presentation.result

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Receipt
import com.example.domain.model.ReceiptItem
import com.example.presentation.home.getCategoryColor
import com.example.presentation.home.getCategoryIcon
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    receipt: Receipt,
    onNavigateHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Groceries", "Travel", "Shopping", "Entertainment", "Utilities", "Other")

    // Bind original receipt values toViewModel state fields
    LaunchedEffect(receipt) {
        viewModel.initialize(receipt)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Scan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateHome) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveReceipt(imageUri = null, onNavigateHome) },
                        modifier = Modifier.testTag("save_receipt_button")
                    ) {
                        Text("Save", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState is ResultUiState.Saving) {
            Box(
                modifier = Modifier.fillMaxSize().clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Saving scan to local DB...", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Alert
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Gemini AI completed extraction. Please verify details before saving.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Core editable fields
                item {
                    Text(
                        text = "General Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    OutlinedTextField(
                        value = viewModel.merchant,
                        onValueChange = { viewModel.updateReceiptField(merchantName = it) },
                        label = { Text("Merchant Name") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.date,
                            onValueChange = { viewModel.updateReceiptField(dateStr = it) },
                            label = { Text("Date (YYYY-MM-DD)") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = viewModel.currency,
                            onValueChange = { viewModel.updateReceiptField(currencySymbol = it) },
                            label = { Text("Currency") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Category badges picker block (Horizontal flowing Chips)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Expense Category",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            categories.forEach { cat ->
                                val isSelected = viewModel.category.equals(cat, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateReceiptField(categoryName = cat) },
                                    label = { Text(cat) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getCategoryIcon(cat),
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = getCategoryColor(cat).copy(alpha = 0.2f),
                                        selectedLabelColor = getCategoryColor(cat),
                                        selectedLeadingIconColor = getCategoryColor(cat)
                                    )
                                )
                            }
                        }
                    }
                }

                // Amounts cards block
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = if (viewModel.total == 0.0) "" else viewModel.total.toString(),
                            onValueChange = {
                                val amount = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateReceiptField(totalAmount = amount)
                            },
                            label = { Text("Total Paid") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = if (viewModel.tax == 0.0) "" else viewModel.tax.toString(),
                            onValueChange = {
                                val amount = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateReceiptField(taxAmount = amount)
                            },
                            label = { Text("Sales Tax") },
                            leadingIcon = { Icon(Icons.Default.RequestQuote, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Line items list header
                item {
                    Text(
                        text = "Itemized breakdown table",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Inner list representing item components
                if (viewModel.items.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Zero individual line items extracted.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(viewModel.items) { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Qty: ${item.quantity} × ${viewModel.currency}${String.format(Locale.getDefault(), "%.2f", item.price / item.quantity.coerceAtLeast(1))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = String.format(Locale.getDefault(), "%s%.2f", viewModel.currency, item.price),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Frame spacing at bottom
                item {
                    Button(
                        onClick = { viewModel.saveReceipt(imageUri = null, onNavigateHome) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .testTag("confirm_and_save_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm and Save Scan")
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}
