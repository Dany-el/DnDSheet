package com.yablonskyi.dndsheet.data.di

import android.content.Context
import androidx.room.Room
import com.yablonskyi.dndsheet.data.AppDatabase
import com.yablonskyi.dndsheet.data.dao.AttackDao
import com.yablonskyi.dndsheet.data.dao.CharacterDao
import com.yablonskyi.dndsheet.data.dao.SpellDao
import com.yablonskyi.dndsheet.data.repository.AttackRepositoryImpl
import com.yablonskyi.dndsheet.data.repository.CharacterRepositoryImpl
import com.yablonskyi.dndsheet.data.repository.SpellRepositoryImpl
import com.yablonskyi.dndsheet.domain.repository.AttackRepository
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.domain.repository.SpellRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dnd_sheet_db"
        ).build()
    }

    // DAO

    @Provides
    fun provideCharacterDao(db: AppDatabase) = db.characterDao()

    @Provides
    fun provideSpellDao(db: AppDatabase) = db.spellDao()

    @Provides
    fun provideAttackDao(db: AppDatabase) = db.attackDao()

    // REPOSITORY

    @Provides
    @Singleton
    fun provideCharacterRepository(
        characterDao: CharacterDao
    ) : CharacterRepository {
        return CharacterRepositoryImpl(characterDao)
    }

    @Provides
    @Singleton
    fun provideSpellRepository(
        spellDao: SpellDao
    ) : SpellRepository {
        return SpellRepositoryImpl(spellDao)
    }

    @Provides
    @Singleton
    fun provideAttackRepository(
        attackDao: AttackDao
    ) : AttackRepository {
        return AttackRepositoryImpl(attackDao)
    }
}