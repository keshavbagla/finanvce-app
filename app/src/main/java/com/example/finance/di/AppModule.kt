package com.example.finance.di

import android.content.Context
import androidx.room.Room
import com.example.finance.data.dataStore.StreakPreferences
import com.example.finance.data.dataStore.streakDataStore
import com.example.finance.data.local.FinanceDatabase
import com.example.finance.data.local.GoalDao
import com.example.finance.data.local.TransactionDao
import com.example.finance.data.repository.GoalRepositoryImpl
import com.example.finance.data.repository.TransactionRepositoryImpl
import com.example.finance.domain.repository.IGoalRepository
import com.example.finance.domain.repository.ITransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): FinanceDatabase =
        Room.databaseBuilder(ctx, FinanceDatabase::class.java, "finance.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTransactionDao(db: FinanceDatabase): TransactionDao = db.transactionDao()

    @Provides
    @Singleton
    fun provideGoalDao(db: FinanceDatabase): GoalDao = db.goalDao()

    @Provides
    @Singleton
    fun provideStreakPreferences(@ApplicationContext ctx: Context): StreakPreferences =
        StreakPreferences(ctx.streakDataStore)

    @Provides
    @Singleton
    fun provideTransactionRepository(dao: TransactionDao): ITransactionRepository =
        TransactionRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideGoalRepository(dao: GoalDao): IGoalRepository =
        GoalRepositoryImpl(dao)
}
