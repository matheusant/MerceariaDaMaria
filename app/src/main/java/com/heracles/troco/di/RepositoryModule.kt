package com.heracles.troco.di

import com.heracles.troco.data.repository.FirebaseAuthRepository
import com.heracles.troco.data.repository.SignupRepositoryImpl
import com.heracles.troco.domain.repository.AuthRepository
import com.heracles.troco.domain.repository.SignupRepository
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
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSignupRepository(impl: SignupRepositoryImpl): SignupRepository
}