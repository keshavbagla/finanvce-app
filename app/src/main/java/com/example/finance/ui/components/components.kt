package com.example.finance.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.finance.domain.model.*
import com.example.finance.ui.theme.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.StrokeCap
import kotlin.properties.ReadOnlyProperty

// ─── Rupee formatter ──────────────────────────────────────────────
val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
fun Double.toRupees(): String = inrFormat.format(this)

// ─── FinanceCard ──────────────────────────────────────────────────
@Composable
fun FinanceCard(
    modifier: Modifier = Modifier,
    color: Color = AppColors.CardDark,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

// ─── TransactionRow ───────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRow(
    txn: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onEdit, onLongClick = onDelete)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (txn.type == TransactionType.INCOME) AppColors.MintGlow.copy(.15f)
                    else AppColors.CoralRed.copy(.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(txn.category.emoji, fontSize = 18.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(
                txn.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = AppColors.TextPrimary,
                maxLines   = 1
            )
            Text(
                "${txn.category.label} · ${txn.formattedDate()}",
                style  = MaterialTheme.typography.labelSmall,
                color  = AppColors.TextTertiary
            )
        }
        Text(
            text  = if (txn.type == TransactionType.INCOME) "+${txn.amount.toRupees()}"
            else "−${txn.amount.toRupees()}",
            color = if (txn.type == TransactionType.INCOME) AppColors.MintGlow else AppColors.CoralRed,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun Transaction.formattedDate(): String {
    val diff = (System.currentTimeMillis() - timestamp) / 86_400_000L
    return when {
        diff == 0L -> "Today"
        diff == 1L -> "Yesterday"
        diff < 7L  -> "$diff days ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

// ─── GoalCard ─────────────────────────────────────────────────────
@Composable
fun GoalCard(goal: Goal, onClick: () -> Unit) {
    FinanceCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(goal.emoji, fontSize = 20.sp)
                Text(goal.title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)
            }
            Text(
                "${goal.progressPercent}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.MintGlow
            )
        }
        Spacer(Modifier.height(10.dp))
        val targetValue = null
        val animProg by animateFloatAsState(
            targetValue   ==goal.progress.toFloat(),
            animationSpec = tween(600, easing = EaseInOut),
            label         = "goal_prog"
        )
        LinearProgressIndicator(
            progress     = { animProg },
            modifier     = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color        = AppColors.IrisPurple,
            trackColor   = AppColors.CardMid
        )
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(goal.savedAmount.toRupees(), style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
            Text("Goal: ${goal.targetAmount.toRupees()}", style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary)
        }
    }
}

private fun ColumnScope.animateFloatAsState(
    targetValue: Boolean,
    animationSpec: TweenSpec<Float>,
    label: String
): ReadOnlyProperty<Any?, Float> {
    TODO("Not yet implemented")
}


@Composable
fun StatCard(
    label: String,
    amount: Double,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    FinanceCard(modifier = modifier, color = AppColors.CardMid) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                if (isPositive) "↑" else "↓",
                color    = if (isPositive) AppColors.MintGlow else AppColors.CoralRed,
                fontSize = 12.sp
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = AppColors.TextTertiary)
        }
        Spacer(Modifier.height(4.dp))
        val animAmt by animateFloatAsState(
            targetValue   = amount.toFloat(),
            animationSpec = tween(800, easing = EaseOut),
            label         = "stat_amount"
        )
        Text(
            animAmt.toDouble().toRupees(),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color      = if (isPositive) AppColors.MintGlow else AppColors.CoralRed
        )
    }
}

// ─── EmptyState ───────────────────────────────────────────────────
@Composable
fun EmptyState(
    emoji: String    = "💸",
    title: String    = "No transactions yet",
    subtitle: String = "Tap + to add your first transaction",
    modifier: Modifier = Modifier
) {
    Column(
        modifier                  = modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment       = Alignment.CenterHorizontally,
        verticalArrangement       = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppColors.TextTertiary, textAlign = TextAlign.Center)
    }
}

// ─── ShimmerCard ─────────────────────────────────────────────────
@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "shimmer")
    val alpha by inf.animateFloat(
        initialValue  = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label         = "shimmer_alpha"
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.CardMid.copy(alpha = alpha))
    )
}

// ─── BarChart (Canvas, no library) ───────────────────────────────
@Composable
fun BarChart(data: List<Double>, modifier: Modifier = Modifier) {
    val max = data.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val barWidth = size.width / (data.size * 1.5f)
        val gap      = barWidth * 0.5f
        data.forEachIndexed { i, value ->
            val barHeight = ((value / max) * size.height).toFloat()
            val x         = i * (barWidth + gap)
            val color     = if (i == data.lastIndex - 1) AppColors.CoralRed else AppColors.IrisPurple
            drawRoundRect(
                color        = color.copy(alpha = if (i == data.lastIndex) 0.4f else 0.3f),
                topLeft      = Offset(x, size.height - barHeight),
                size         = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f)
            )
        }
    }
}

// ─── DonutChart (Canvas, no library) ─────────────────────────────
@Composable
fun DonutChart(slices: List<Pair<Category, Double>>, modifier: Modifier = Modifier) {
    val total       = slices.sumOf { it.second }.takeIf { it > 0 } ?: 1.0
    val sliceColors = listOf(AppColors.MintGlow, AppColors.CoralRed, AppColors.Sunbeam, AppColors.IrisPurple)
    val animSweeps  = slices.mapIndexed { i, (_, amt) ->
        animateFloatAsState(
            targetValue   = (amt / total * 360f).toFloat(),
            animationSpec = tween(700, i * 60, EaseOut),
            label         = "donut_$i"
        ).value
    }
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val stroke  = size.minDimension * 0.18f
        val radius  = (size.minDimension - stroke) / 2f
        val center  = Offset(size.width / 2f, size.height / 2f)
        var startA  = -90f
        animSweeps.forEachIndexed { i, sweep ->
            drawArc(
                color      = sliceColors.getOrElse(i) { AppColors.TextTertiary },
                startAngle = startA,
                sweepAngle = (sweep - 2f).coerceAtLeast(0f),
                useCenter  = false,
                topLeft    = Offset(center.x - radius, center.y - radius),
                size       = Size(radius * 2, radius * 2),
                style      = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            startA += sweep
        }
    }
}

// ─── OutlinedTextField colors helper ─────────────────────────────
@Composable
fun financeTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = AppColors.IrisPurple,
    unfocusedBorderColor    = AppColors.Divider,
    focusedLabelColor       = AppColors.IrisPurple,
    unfocusedLabelColor     = AppColors.TextTertiary,
    cursorColor             = AppColors.IrisPurple,
    focusedTextColor        = AppColors.TextPrimary,
    unfocusedTextColor      = AppColors.TextPrimary,
    focusedContainerColor   = AppColors.CardMid,
    unfocusedContainerColor = AppColors.CardMid
)

// ─── BottomNavBar (shared scaffold component) ─────────────────────
@Composable
fun FinanceBottomNav(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(
        containerColor = AppColors.CardDark,
        tonalElevation = 0.dp,
        modifier       = Modifier.height(64.dp)
    ) {
        data class NavItem(val icon: String, val label: String, val route: String)
        val items = listOf(
            NavItem("⊞", "Home",     "dashboard"),
            NavItem("↕", "Txns",    "transactions"),
            NavItem("◉", "Insights", "insights"),
            NavItem("🏆", "Goals",   "goals")
        )
        items.take(2).forEach { item ->
            NavigationBarItem(
                selected  = currentRoute == item.route,
                onClick   = { onNavigate(item.route) },
                icon      = { Text(item.icon, fontSize = 20.sp) },
                label     = { Text(item.label, fontSize = 10.sp) },
                colors    = navItemColors(currentRoute == item.route)
            )
        }
        // Center FAB placeholder space
        Box(Modifier.weight(1f))
        items.takeLast(2).forEach { item ->
            NavigationBarItem(
                selected  = currentRoute == item.route,
                onClick   = { onNavigate(item.route) },
                icon      = { Text(item.icon, fontSize = 20.sp) },
                label     = { Text(item.label, fontSize = 10.sp) },
                colors    = navItemColors(currentRoute == item.route, isGoals = item.route == "goals")
            )
        }
    }
}

@Composable
private fun navItemColors(selected: Boolean, isGoals: Boolean = false) =
    NavigationBarItemDefaults.colors(
        selectedIconColor   = if (isGoals) AppColors.Sunbeam else AppColors.IrisPurple,
        selectedTextColor   = if (isGoals) AppColors.Sunbeam else AppColors.IrisPurple,
        unselectedIconColor = AppColors.TextTertiary,
        unselectedTextColor = AppColors.TextTertiary,
        indicatorColor      = Color.Transparent
    )
