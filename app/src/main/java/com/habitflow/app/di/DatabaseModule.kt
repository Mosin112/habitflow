package com.habitflow.app.di

import android.content.Context
import androidx.room.Room
import com.habitflow.app.data.local.dao.HabitDao
import com.habitflow.app.data.local.dao.HabitEntryDao
import com.habitflow.app.data.local.database.HabitDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): HabitDatabase =
        Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            HabitDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideHabitDao(db: HabitDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideHabitEntryDao(db: HabitDatabase): HabitEntryDao = db.habitEntryDao()
}
