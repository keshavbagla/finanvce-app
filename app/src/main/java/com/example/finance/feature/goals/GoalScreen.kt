package com.example.finance.feature.goals

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finance.ui.components.EmptyState
import com.example.finance.ui.components.FinanceCard
import com.example.finance.ui.components.GoalCard
import com.example.finance.ui.components.ShimmerCard
import com.example.finance.ui.components.toRupees
import com.example.finance.ui.theme.AppColors
import com.example.finance.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddGoalSheet by remember { mutableStateOf(false) }
    var addSavingsTarget by remember { mutableStateOf<Goal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DeepNavy)
            .verticalScroll(rememberScrollState())
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Goals & Streaks",
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.TextPrimary
            )
            // Add Goal button
            Surface(
                onClick = { showAddGoalSheet = true },
                shape   = RoundedCornerShape(100.dp),
                color   = AppColors.IrisPurple
            ) {
                Text(
                    "+ Goal",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        StreakCard(state.streak)

        Spacer(Modifier.height(20.dp))

        state.weeklyChallenge?.let { challenge ->
            WeeklyChallengeCard(challenge)
            Spacer(Modifier.height(20.dp))
        }


        Text(
            "ACTIVE GOALS",
            modifier = Modifier.padding(horizontal = 20.dp),
            style    = MaterialTheme.typography.labelSmall,
            color    = AppColors.TextTertiary
        )
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Column(
                    Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    repeat(3) { ShimmerCard(Modifier.height(90.dp)) }
                }
            }

            state.goals.isEmpty() -> {
                EmptyState(
                    emoji    = "🎯",
                    title    = "No goals yet",
                    subtitle = "Tap + Goal to start saving for something",
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }

            else -> {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.goals.forEach { goal ->
                        GoalCard(
                            goal    = goal,
                            onClick = { addSavingsTarget = goal }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(96.dp))
    }

    // ── Add Goal Bottom Sheet ─────────────────────────────────
    if (showAddGoalSheet) {
        AddGoalSheet(
            onDismiss = { showAddGoalSheet = false },
            onSave    = { goal ->
                viewModel.addGoal(goal)
                showAddGoalSheet = false
            }
        )
    }

    addSavingsTarget?.let { goal ->
        AddSavingsSheet(
            goal      = goal,
            onDismiss = { addSavingsTarget = null },
            onAdd     = { amount ->
                viewModel.addSavings(goal.id, amount)
                addSavingsTarget = null
            }
        )
    }
}

@Composable
fun StreakCard(streak: StreakData?) {
    val flameScale by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue   = 1f,
        targetValue    = 1.08f,
        animationSpec  = infiniteRepeatable(tween(800, easing = EaseInOut), RepeatMode.Reverse),
        label          = "flame_scale"
    )

    FinanceCard(
        modifier = Modifier.padding(horizontal = 20.dp),
        color    = Color(0xFF1A1424)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Animated flame + count
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "🔥",
                    fontSize = 32.sp,
                    modifier = Modifier.scale(if ((streak?.currentStreak ?: 0) > 0) flameScale else 1f)
                )
                Text(
                    "${streak?.currentStreak ?: 0}",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = AppColors.Sunbeam,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text("day streak", fontSize = 10.sp, color = AppColors.TextTertiary)
            }

            // Week dots
            Column(Modifier.weight(1f)) {
                Text(
                    "This week",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextTertiary
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
                    val history   = streak?.weekHistory ?: List(7) { false }
                    dayLabels.forEachIndexed { i, day ->
                        val done    = history.getOrElse(i) { false }
                        val isToday = i == 6
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isToday && done -> AppColors.Sunbeam.copy(.4f)
                                            done            -> AppColors.Sunbeam.copy(.15f)
                                            else            -> AppColors.CardMid
                                        }
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.5.dp, AppColors.Sunbeam, RoundedCornerShape(8.dp)
                                        ) else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (done) "✓" else "·",
                                    fontSize = 12.sp,
                                    color    = if (done) AppColors.Sunbeam else AppColors.TextTertiary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(day, fontSize = 9.sp, color = AppColors.TextTertiary)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if ((streak?.currentStreak ?: 0) > 0)
                        "🎉 Keep it up! Log today to extend your streak."
                    else
                        "Log a transaction today to start your streak!",
                    fontSize = 11.sp,
                    color    = AppColors.TextSecondary
                )
            }
        }
    }
}


@Composable
fun WeeklyChallengeCard(challenge: WeeklyChallenge) {
    val progress       = (challenge.currentSpend / challenge.spendLimit).toFloat().coerceIn(0f, 1f)
    val animatedProg   by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(600, easing = EaseOut),
        label         = "challenge_progress"
    )
    val isOk = challenge.currentSpend <= challenge.spendLimit

    FinanceCard(
        modifier = Modifier.padding(horizontal = 20.dp),
        color    = if (isOk) Color(0xFF142018) else Color(0xFF201414)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "WEEKLY CHALLENGE",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = AppColors.Sunbeam
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    challenge.description,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color      = AppColors.TextPrimary
                )
            }
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = AppColors.Sunbeam.copy(.15f)
            ) {
                Text(
                    "${challenge.daysRemaining}d left",
                    modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    fontSize   = 10.sp,
                    color      = AppColors.Sunbeam,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Spend progress bar
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(AppColors.CardMid)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(animatedProg)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isOk) AppColors.MintGlow else AppColors.CoralRed)
            )
        }

        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${challenge.currentSpend.toRupees()} spent",
                fontSize = 11.sp,
                color    = if (isOk) AppColors.MintGlow else AppColors.CoralRed,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Limit: ${challenge.spendLimit.toRupees()}",
                fontSize = 11.sp,
                color    = AppColors.TextTertiary
            )
        }
    }
}

// ── Add Goal Bottom Sheet ─────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalSheet(onDismiss: () -> Unit, onSave: (Goal) -> Unit) {
    var title  by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var emoji  by remember { mutableStateOf("🎯") }

    val emojiOptions = listOf("🎯","🏖","💻","🚗","🏠","✈️","💍","📱","🎓","💊")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = AppColors.CardDark,
        scrimColor       = AppColors.DeepNavy.copy(alpha = 0.8f),
        dragHandle       = { BottomSheetDefaults.DragHandle(color = AppColors.TextTertiary) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("New Goal", style = MaterialTheme.typography.titleMedium, color = AppColors.TextPrimary)

            // Emoji picker
            Text("Pick an icon", style = MaterialTheme.typography.labelSmall, color = AppColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojiOptions.forEach { e ->
                    Surface(
                        onClick  = { emoji = e },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (emoji == e) AppColors.IrisPurpleAlpha else AppColors.CardMid,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(e, fontSize = 20.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange  = { title = it },
                label    = { Text("Goal title (e.g. Goa Trip)") },
                modifier = Modifier.fillMaxWidth(),
                colors   = outlinedColors()
            )

            OutlinedTextField(
                value = amount,
                onValueChange  = { amount = it },
                label    = { Text("Target amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors   = outlinedColors()
            )

            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    if (title.isBlank()) return@Button
                    onSave(Goal(title = title.trim(), emoji = emoji, targetAmount = amt, savedAmount = 0.0))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AppColors.IrisPurple)
            ) {
                Text("Create Goal", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Add Savings Sheet ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsSheet(goal: Goal, onDismiss: () -> Unit, onAdd: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = AppColors.CardDark,
        scrimColor       = AppColors.DeepNavy.copy(alpha = 0.8f),
        dragHandle       = { BottomSheetDefaults.DragHandle(color = AppColors.TextTertiary) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "${goal.emoji} Add to ${goal.title}",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary
            )
            Text(
                "Saved so far: ${goal.savedAmount.toRupees()} / ${goal.targetAmount.toRupees()} (${goal.progressPercent}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextSecondary
            )
            OutlinedTextField(
                value = amount,
                onValueChange  = { amount = it },
                label    = { Text("Amount to add (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors   = outlinedColors()
            )
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onAdd(amt)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AppColors.MintGlow)
            ) {
                Text("Add Savings", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            }
        }
    }
}

// Shared outlined colors helper
@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
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
