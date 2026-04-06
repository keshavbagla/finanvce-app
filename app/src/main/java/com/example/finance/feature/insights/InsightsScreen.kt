package com.example.finance.feature.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.finance.ui.components.EmptyState
import com.example.finance.ui.components.FinanceCard
import com.example.finance.ui.components.ShimmerCard
import com.example.finance.ui.theme.AppColors
import com.example.finance.domain.model.*
import com.example.finance.ui.components.StatCard
import com.example.finance.ui.components.toRupees


@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DeepNavy)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──────────────────────────────────────────
        Text(
            "Insights",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            style    = MaterialTheme.typography.headlineMedium,
            color    = AppColors.TextPrimary
        )

        // ── Period Selector ───────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            TimePeriod.entries.forEach { period ->
                val label = period.label
                val selected = state.selectedPeriod == period
                    Surface(
                        onClick  = { viewModel.setPeriod(period) },
                        shape    = RoundedCornerShape(100.dp),
                        color    = if (selected) AppColors.IrisPurple else AppColors.CardMid,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(
                            Modifier.padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color      = if (selected) Color.White else AppColors.TextSecondary
                            )
                        }
                    }
                }
        }

        Spacer(Modifier.height(20.dp))

        // ── Summary Cards ─────────────────────────────────────
        if (state.isLoading) {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ShimmerCard(Modifier.weight(1f).height(80.dp))
                ShimmerCard(Modifier.weight(1f).height(80.dp))
            }
        } else {
            state.monthlyStats?.let { stats ->
                Row(
                    Modifier.padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Income",   stats.totalIncome,  isPositive = true,  modifier = Modifier.weight(1f))
                    StatCard("Expenses", stats.totalExpense, isPositive = false, modifier = Modifier.weight(1f))
                }

                // Net savings highlight
                Spacer(Modifier.height(12.dp))
                FinanceCard(modifier = Modifier.padding(horizontal = 20.dp), color = AppColors.CardMid) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Savings", style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary)
                            Text(
                                stats.net.toRupees(),
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (stats.net >= 0) AppColors.MintGlow else AppColors.CoralRed
                            )
                        }
                        val savingsRate = if (stats.totalIncome > 0)
                            (stats.net / stats.totalIncome * 100).toInt() else 0
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (savingsRate >= 0) AppColors.MintGlow.copy(.15f) else AppColors.CoralRed.copy(.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$savingsRate%",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = if (savingsRate >= 0) AppColors.MintGlow else AppColors.CoralRed
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Spending Donut Chart ──────────────────────────────
        Text(
            "SPENDING BREAKDOWN",
            modifier = Modifier.padding(horizontal = 20.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = AppColors.TextTertiary
        )
        Spacer(Modifier.height(12.dp))

        if (state.isLoading) {
            ShimmerCard(Modifier.padding(horizontal = 20.dp).height(160.dp))
        } else if (state.topCategories.isNotEmpty()) {
            FinanceCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DonutChart(
                        slices   = state.topCategories as List<Pair<Category, Double>>,
                        modifier = Modifier.size(120.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val total = state.topCategories.sumOf { it.second }
                        val sliceColors = listOf(
                            AppColors.MintGlow,
                            AppColors.CoralRed,
                            AppColors.Sunbeam,
                            AppColors.IrisPurple
                        )
                        state.topCategories.forEachIndexed { i, (cat, amt) ->
                            val pct = if (total > 0) (amt / total * 100).toInt() else 0
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(sliceColors.getOrElse(i) { AppColors.TextTertiary })
                                )
                                Column {
                                    Text(cat.label, fontSize = 12.sp, color = AppColors.TextSecondary, fontWeight = FontWeight.Medium)
                                    Text("$pct%", fontSize = 11.sp, color = AppColors.TextTertiary)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            EmptyState(
                emoji    = "📊",
                title    = "No data yet",
                subtitle = "Add some transactions to see your breakdown",
                modifier = Modifier.padding(vertical = 20.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Top Category Bars ─────────────────────────────────
        if (state.topCategories.isNotEmpty()) {
            Text(
                "TOP EXPENSES",
                modifier = Modifier.padding(horizontal = 20.dp),
                style    = MaterialTheme.typography.labelSmall,
                color    = AppColors.TextTertiary
            )
            Spacer(Modifier.height(12.dp))
            FinanceCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                val total = state.topCategories.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
                val barColors = listOf(AppColors.MintGlow, AppColors.CoralRed, AppColors.Sunbeam, AppColors.IrisPurple)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    state.topCategories.forEachIndexed { i, (cat, amt) ->
                        val fraction = (amt / total).toFloat()
                        val animatedFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(600, delayMillis = i * 80, easing = EaseOut),
                            label = "bar_$i"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${cat.emoji} ${cat.label}",
                                    fontSize = 13.sp,
                                    color    = AppColors.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    amt.toRupees(),
                                    fontSize   = 13.sp,
                                    color      = barColors.getOrElse(i) { AppColors.TextSecondary },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(AppColors.CardMid)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(animatedFraction)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(barColors.getOrElse(i) { AppColors.IrisPurple })
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Monthly Trend Bar Chart ───────────────────────────
        Text(
            "6-MONTH SPENDING TREND",
            modifier = Modifier.padding(horizontal = 20.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = AppColors.TextTertiary
        )
        Spacer(Modifier.height(12.dp))
        FinanceCard(modifier = Modifier.padding(horizontal = 20.dp)) {
            // Static sample data — in prod, pass real monthly rollup from VM
            val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
            val values = listOf(18000.0, 24000.0, 16500.0, 31000.0, 22000.0, 19400.0)
            MonthTrendChart(labels = months, values = values)
        }

        Spacer(Modifier.height(96.dp))
    }
}



@Composable
fun DonutChart(
    slices: List<Pair<Category, Double>>,
    modifier: Modifier = Modifier
) {
    val total = slices.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    val sliceColors = listOf(AppColors.MintGlow, AppColors.CoralRed, AppColors.Sunbeam, AppColors.IrisPurple)

    val animatedSweeps = slices.mapIndexed { i, (_, amt) ->
        val target = (amt / total * 360f).toFloat()
        animateFloatAsState(
            targetValue   = target,
            animationSpec = tween(700, delayMillis = i * 60, easing = EaseOut),
            label         = "donut_$i"
        ).value
    }

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.18f
        val radius      = (size.minDimension - strokeWidth) / 2f
        val center      = Offset(size.width / 2f, size.height / 2f)
        var startAngle  = -90f

        animatedSweeps.forEachIndexed { i, sweep ->
            drawArc(
                color      = sliceColors.getOrElse(i) { AppColors.TextTertiary },
                startAngle = startAngle,
                sweepAngle = sweep - 2f,          // 2f gap between slices
                useCenter  = false,
                topLeft    = Offset(center.x - radius, center.y - radius),
                size       = Size(radius * 2, radius * 2),
                style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

// ── Monthly Trend Bar Chart ───────────────────────────────────────
@Composable
fun MonthTrendChart(labels: List<String>, values: List<Double>) {
    val max = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    val animatedHeights = values.mapIndexed { i, v ->
        animateFloatAsState(
            targetValue   = (v / max).toFloat(),
            animationSpec = tween(500, delayMillis = i * 60, easing = EaseOut),
            label         = "bar_height_$i"
        ).value
    }

    Column {
        // Bars
        Row(
            modifier              = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment     = Alignment.Bottom
        ) {
            animatedHeights.forEachIndexed { i, frac ->
                val isLast = i == animatedHeights.lastIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .fillMaxHeight(frac)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (isLast) AppColors.CoralRed.copy(.6f)
                            else AppColors.IrisPurple.copy(.3f)
                        )
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    label,
                    fontSize   = 10.sp,
                    color      = AppColors.TextTertiary,
                    modifier   = Modifier.weight(1f),
                    textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}