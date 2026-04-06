package com.example.finance.feature.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finance.navigation.AddTransactionSheet
import com.example.finance.navigation.Screen
import com.example.finance.ui.components.BarChart
import com.example.finance.ui.components.EmptyState
import com.example.finance.ui.components.ShimmerCard
import com.example.finance.ui.components.StatCard
import com.example.finance.ui.components.TransactionRow
import com.example.finance.ui.components.toRupees
import com.example.finance.ui.theme.AppColors

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // ✅ Controls whether the Add Transaction bottom sheet is visible
    var showAddSheet by remember { mutableStateOf(false) }

    // ✅ Show bottom sheet when user taps FAB
    if (showAddSheet) {
        AddTransactionSheet(
            onDismiss = { showAddSheet = false },
            onSave = { txn ->
                viewModel.addTransaction(txn)
                showAddSheet = false
            }
        )
    }

    Scaffold(
        containerColor = AppColors.DeepNavy,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showAddSheet = true },
                containerColor = AppColors.IrisPurple,
                contentColor   = Color.White
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Add Transaction"
                )
            }
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DeepNavy)
                .padding(innerPadding),          // ✅ respect Scaffold padding so FAB doesn't overlap
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {

            // ────── Header card: balance + income/expense ──────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            AppColors.CardDark,
                            RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Good morning ☀",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AppColors.TextTertiary
                                )
                                Text(
                                    "Arjun Sharma",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.TextPrimary
                                )
                            }
                            Box(
                                Modifier
                                    .size(36.dp)
                                    .background(
                                        AppColors.IrisPurple.copy(.3f),
                                        RoundedCornerShape(50)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "AS",
                                    fontSize   = 13.sp,
                                    color      = AppColors.IrisPurple,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            "TOTAL BALANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AppColors.TextTertiary
                        )
                        AnimatedContent(targetState = state.balance, label = "balance") { bal ->
                            Text(
                                bal.toRupees(),
                                fontSize   = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = AppColors.TextPrimary
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                "Income",
                                state.monthlyIncome,
                                isPositive = true,
                                modifier   = Modifier.weight(1f)
                            )
                            StatCard(
                                "Expenses",
                                state.monthlyExpense,
                                isPositive = false,
                                modifier   = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ────── Weekly bar chart ──────
            item {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(
                        "SPENDING THIS WEEK",
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.TextTertiary
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.weeklySpending.isNotEmpty()) {
                        BarChart(
                            data     = state.weeklySpending,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )
                    } else {
                        ShimmerCard()
                    }
                }
            }

            // ────── Recent header ──────
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                    TextButton(onClick = { navController.navigate(Screen.Transactions.route) }) {
                        Text("See all →", color = AppColors.IrisPurple, fontSize = 12.sp)
                    }
                }
            }

            if (state.isLoading) {
                items(3) {
                    ShimmerCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                }
            } else if (state.recentTransactions.isEmpty()) {
                item { EmptyState() }
            } else {
                itemsIndexed(state.recentTransactions) { index, txn ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = slideInVertically(initialOffsetY = { it / 2 }) +
                                fadeIn(tween(300, index * 40)),
                    ) {
                        TransactionRow(
                            txn      = txn,
                            onEdit   = { /* open edit sheet */ },
                            onDelete = { /* show confirm dialog */ }
                        )
                    }
                    if (index < state.recentTransactions.lastIndex) {
                        HorizontalDivider(
                            Modifier.padding(horizontal = 16.dp),
                            color = AppColors.Divider
                        )
                    }
                }
            }
        }
    }
}