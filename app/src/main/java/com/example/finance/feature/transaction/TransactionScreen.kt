package com.example.finance.feature.transaction

import com.example.finance.navigation.AddTransactionSheet
import com.example.finance.ui.components.EmptyState
import com.example.finance.ui.components.ShimmerCard
import com.example.finance.ui.theme.AppColors
import com.example.finance.feature.transaction.TransactionEvent
import com.example.finance.feature.transaction.TransactionViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.finance.ui.components.TransactionRow
import com.example.finance.ui.components.toRupees
import com.example.finance.feature.transaction.TransactionFilter
import com.example.finance.domain.model.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Transaction?>(null) }

    // Group transactions by month
    val grouped: Map<String, List<Transaction>> = remember(state.transactions) {
        state.transactions.groupBy { txn ->
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(txn.timestamp))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DeepNavy)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = AppColors.TextPrimary
                )
                // Summary badge
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = AppColors.IrisPurpleAlpha
                ) {
                    Text(
                        "${state.transactions.size} entries",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = AppColors.IrisPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Search Bar ───────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(TransactionEvent.SetSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search transactions…", color = AppColors.TextTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextTertiary, modifier = Modifier.size(18.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AppColors.IrisPurple,
                    unfocusedBorderColor = AppColors.Divider,
                    focusedTextColor     = AppColors.TextPrimary,
                    unfocusedTextColor   = AppColors.TextPrimary,
                    cursorColor          = AppColors.IrisPurple,
                    focusedContainerColor   = AppColors.CardMid,
                    unfocusedContainerColor = AppColors.CardMid
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Filter Chips ─────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                val filters = listOf("All", "Income", "Expense") +
                        Category.entries.map { "${it.emoji} ${it.label}" }
                val filterEnums = listOf(
                    TransactionFilter.ALL,
                    TransactionFilter.INCOME,
                    TransactionFilter.EXPENSE
                )

                filters.forEachIndexed { index, label ->
                    val isSelected = when {
                        index == 0 -> state.selectedFilter == TransactionFilter.ALL
                        index == 1 -> state.selectedFilter == TransactionFilter.INCOME
                        index == 2 -> state.selectedFilter == TransactionFilter.EXPENSE
                        else       -> false
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            if (index < filterEnums.size) {
                                viewModel.onEvent(TransactionEvent.SetFilter(filterEnums[index]))
                            }
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.IrisPurple,
                            selectedLabelColor     = Color.White,
                            containerColor         = AppColors.CardMid,
                            labelColor             = AppColors.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = isSelected,
                            borderColor         = AppColors.Divider,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { ShimmerCard() }
                    }
                }

                state.transactions.isEmpty() -> {
                    EmptyState(
                        emoji    = if (state.searchQuery.isNotBlank()) "🔍" else "💸",
                        title    = if (state.searchQuery.isNotBlank()) "No results found" else "No transactions yet",
                        subtitle = if (state.searchQuery.isNotBlank()) "Try a different search term"
                        else "Tap + to log your first transaction",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        grouped.forEach { (monthLabel, txns) ->

                            // Month header with summary
                            val monthIncome  = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                            val monthExpense = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                            stickyHeader(key = "header_$monthLabel") {
                                MonthStickyHeader(
                                    label   = monthLabel,
                                    income  = monthIncome,
                                    expense = monthExpense
                                )
                            }

                            itemsIndexed(
                                items = txns,
                                key   = { _, txn -> txn.id }
                            ) { index, txn ->
                                AnimatedVisibility(
                                    visible      = true,
                                    enter        = slideInVertically(
                                        initialOffsetY = { it / 3 },
                                        animationSpec  = tween(280, easing = EaseOut)
                                    ) + fadeIn(tween(280, index * 30))
                                ) {
                                    TransactionRow(
                                        txn      = txn,
                                        onEdit   = { viewModel.onEvent(TransactionEvent.StartEdit(txn)) },
                                        onDelete = { showDeleteDialog = txn }
                                    )
                                }
                                if (index < txns.lastIndex) {
                                    HorizontalDivider(
                                        modifier  = Modifier.padding(horizontal = 16.dp),
                                        color     = AppColors.Divider,
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showAddSheet) {
            AddTransactionSheet(
                existingTxn = state.editingTransaction,
                onDismiss   = { viewModel.onEvent(TransactionEvent.ShowAddSheet(false)) },
                onSave      = { txn ->
                    if (state.editingTransaction != null)
                        viewModel.onEvent(TransactionEvent.Update(txn))
                    else
                        viewModel.onEvent(TransactionEvent.Add(txn))
                }
            )
        }
    }

    showDeleteDialog?.let { txnToDelete ->
        AlertDialog(
            onDismissRequest  = { showDeleteDialog = null },
            containerColor    = AppColors.CardDark,
            title = {
                Text("Delete Transaction?", color = AppColors.TextPrimary, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(
                    "\"${txnToDelete.title}\" will be permanently removed.",
                    color = AppColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(TransactionEvent.Delete(txnToDelete))
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = AppColors.CoralRed, fontWeight = FontWeight.Medium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            }
        )
    }
}


@Composable
fun MonthStickyHeader(label: String, income: Double, expense: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.DeepNavy)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style      = MaterialTheme.typography.labelSmall,
            color      = AppColors.TextTertiary,
            fontWeight = FontWeight.Medium
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("↑${income.toRupees()}",  fontSize = 11.sp, color = AppColors.MintGlow,  fontWeight = FontWeight.Medium)
            Text("↓${expense.toRupees()}", fontSize = 11.sp, color = AppColors.CoralRed, fontWeight = FontWeight.Medium)
        }
    }
}