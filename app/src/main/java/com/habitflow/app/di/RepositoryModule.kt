package com.habitflow.app.di

import com.habitflow.app.data.repository.AuthRepositoryImpl
import com.habitflow.app.data.repository.HabitRepositoryImpl
import com.habitflow.app.domain.repository.AuthRepository
import com.habitflow.app.domain.repository.HabitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
