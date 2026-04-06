package com.example.finance.navigation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import com.example.finance.domain.model.Category
import com.example.finance.domain.model.Transaction
import com.example.finance.domain.model.TransactionType
import com.example.finance.ui.theme.AppColors
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    existingTxn: Transaction? = null,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit
) {
    var title   by remember { mutableStateOf(existingTxn?.title ?: "") }
    var amount  by remember { mutableStateOf(existingTxn?.amount?.toString() ?: "") }
    var typeIdx by remember { mutableIntStateOf(if (existingTxn?.type == TransactionType.INCOME) 1 else 0) }
    var catIdx  by remember { mutableIntStateOf(Category.entries.indexOf(existingTxn?.category ?: Category.FOOD).coerceAtLeast(0)) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = AppColors.CardDark,
        scrimColor       = AppColors.DeepNavy.copy(alpha = 0.8f),
        dragHandle       = { BottomSheetDefaults.DragHandle(color = AppColors.TextTertiary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text  = if (existingTxn == null) "Add Transaction" else "Edit Transaction",
                style = MaterialTheme.typography.titleMedium,
                color = AppColors.TextPrimary
            )


            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Expense", "Income").forEachIndexed { i, label ->
                    val selected = typeIdx == i
                    FilterChip(
                        selected = selected,
                        onClick  = { typeIdx = i },   // FIX 2: was hardcoded to 0
                        label    = { Text(label, fontSize = 13.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.IrisPurple,
                            selectedLabelColor     = Color.White,
                            containerColor         = AppColors.CardMid,
                            labelColor             = AppColors.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = selected,
                            borderColor         = AppColors.Divider,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            OutlinedTextField(
                value           = amount,
                onValueChange   = { amount = it },
                label           = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier        = Modifier.fillMaxWidth(),
                colors          = sheetTextFieldColors()
            )


            OutlinedTextField(
                value         = title,
                onValueChange = { title = it },
                label         = { Text("Title") },
                modifier      = Modifier.fillMaxWidth(),
                colors        = sheetTextFieldColors()
            )


            Text(
                "Category",
                style = MaterialTheme.typography.labelSmall,
                color = AppColors.TextSecondary
            )
            Row(
                modifier              = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Category.entries.forEachIndexed { i, cat ->
                    val selected = catIdx == i
                    FilterChip(
                        selected = selected,
                        onClick  = { catIdx = i },    // FIX 2 again: was hardcoded to 0
                        label    = { Text("${cat.emoji} ${cat.label}", fontSize = 12.sp) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.IrisPurple,
                            selectedLabelColor     = Color.White,
                            containerColor         = AppColors.CardMid,
                            labelColor             = AppColors.TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = selected,
                            borderColor         = AppColors.Divider,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }


            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    if (title.isBlank()) return@Button
                    onSave(
                        Transaction(
                            id       = existingTxn?.id ?: UUID.randomUUID().toString(),
                            title    = title.trim(),
                            amount   = amt,
                            type     = if (typeIdx == 1) TransactionType.INCOME else TransactionType.EXPENSE,
                            category = Category.entries[catIdx]
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = AppColors.IrisPurple)
            ) {
                Text("Save Transaction", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun sheetTextFieldColors() = OutlinedTextFieldDefaults.colors(
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