package com.example.finance.data.local

import com.example.finance.domain.model.Category
import com.example.finance.domain.model.Goal
import com.example.finance.domain.model.Transaction
import com.example.finance.domain.model.TransactionType

fun TransactionEntity.toDomain() = Transaction(
    id        = id,
    title     = title,
    amount    = amount,
    type      = TransactionType.valueOf(type),
    category  = Category.valueOf(category),
    note      = note,
    timestamp = timestamp
)

fun Transaction.toEntity() = TransactionEntity(
    id        = id,
    title     = title,
    amount    = amount,
    type      = type.name,
    category  = category.name,
    note      = note,
    timestamp = timestamp
)

fun GoalEntity.toDomain() = Goal(
    id           = id,
    title        = title,
    emoji        = emoji,
    targetAmount = targetAmount,
    savedAmount  = savedAmount,
    createdAt    = createdAt
)

fun Goal.toEntity() = GoalEntity(
    id           = id,
    title        = title,
    emoji        = emoji,
    targetAmount = targetAmount,
    savedAmount  = savedAmount,
    createdAt    = createdAt
)