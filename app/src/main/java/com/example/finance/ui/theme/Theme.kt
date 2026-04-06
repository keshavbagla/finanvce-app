package com.example.finance.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.finance.ui.theme.AppColors
import com.example.finance.ui.theme.AppTypography


private val DarkColorScheme = darkColorScheme(
    primary           = AppColors.IrisPurple,
    onPrimary         = Color.White,
    primaryContainer  = AppColors.CardMid,
    secondary         = AppColors.MintGlow,
    tertiary          = AppColors.Sunbeam,
    background        = AppColors.DeepNavy,
    surface           = AppColors.CardDark,
    surfaceVariant    = AppColors.CardMid,
    onBackground      = AppColors.TextPrimary,
    onSurface         = AppColors.TextPrimary,
    error             = AppColors.CoralRed
)



@Composable
fun FinanceAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = AppTypography,
        content     = content
    )
}