package com.yablonskyi.dndsheet.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6
            )
            .build()
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
        db: AppDatabase,
        spellDao: SpellDao,
        attackDao: AttackDao,
        characterDao: CharacterDao,
    ): CharacterRepository {
        return CharacterRepositoryImpl(db, characterDao, attackDao, spellDao)
    }

    @Provides
    @Singleton
    fun provideSpellRepository(
        spellDao: SpellDao
    ): SpellRepository {
        return SpellRepositoryImpl(spellDao)
    }

    @Provides
    @Singleton
    fun provideAttackRepository(
        attackDao: AttackDao
    ): AttackRepository {
        return AttackRepositoryImpl(attackDao)
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `character_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `level` INTEGER NOT NULL,
                `current_hp` INTEGER NOT NULL,
                `max_hp` INTEGER NOT NULL,
                `temp_hp` INTEGER NOT NULL,
                `hit_dice` TEXT NOT NULL,
                
                `class` TEXT NOT NULL,
                `subclass` TEXT NOT NULL,
                `race` TEXT NOT NULL,
                
                `speed` INTEGER NOT NULL,
                `armor_class` INTEGER NOT NULL,
                `shield` INTEGER NOT NULL,
                `initiative_bonus` INTEGER NOT NULL,
                `passivePerceptionBonus` INTEGER NOT NULL,
                `hasJackOfAllTrades` INTEGER NOT NULL,
                
                `money_gold` INTEGER NOT NULL DEFAULT 0,
                `money_silver` INTEGER NOT NULL DEFAULT 0,
                `money_copper` INTEGER NOT NULL DEFAULT 0,

                `proficiencies` TEXT NOT NULL,
                `traits` TEXT NOT NULL,
                `feats` TEXT NOT NULL,
                `inventory` TEXT NOT NULL,
                `backstory` TEXT NOT NULL,
                `skill_proficiencies` TEXT NOT NULL, -- Assuming TypeConverter -> String
                `savingThrowProficiencies` TEXT NOT NULL, -- Assuming TypeConverter -> String

                -- EMBEDDED: AbilityBlock (Prefix: 'abilities_' + ColumnInfo name)
                `abilities_strength` INTEGER NOT NULL,
                `abilities_dexterity` INTEGER NOT NULL,
                `abilities_constitution` INTEGER NOT NULL,
                `abilities_intelligence` INTEGER NOT NULL,
                `abilities_wisdom` INTEGER NOT NULL,
                `abilities_charisma` INTEGER NOT NULL,

                -- EMBEDDED: SpellSettings (Prefix: 'spell_settings_' + ColumnInfo name)
                `spell_settings_spell_ability` TEXT, -- Nullable Enum
                `spell_settings_spell_dc_misc_bonus` INTEGER NOT NULL,
                `spell_settings_spell_attack_misc_bonus` INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            INSERT INTO character_new (
                id, name, level, current_hp, max_hp, temp_hp, hit_dice, 
                class, subclass, race, speed, armor_class, shield,
                initiative_bonus, passivePerceptionBonus, hasJackOfAllTrades,
                
                money_gold, money_silver, money_copper,
                
                proficiencies, traits, feats, inventory, backstory,
                skill_proficiencies, savingThrowProficiencies,
                
                abilities_strength, abilities_dexterity, abilities_constitution,
                abilities_intelligence, abilities_wisdom, abilities_charisma,
                
                spell_settings_spell_ability, 
                spell_settings_spell_dc_misc_bonus, 
                spell_settings_spell_attack_misc_bonus
            )
            SELECT 
                id, name, level, current_hp, max_hp, temp_hp, hit_dice, 
                class, subclass, race, speed, armor_class, shield,
                initiative_bonus, passivePerceptionBonus, hasJackOfAllTrades,
                
                CAST(coins AS INTEGER),              -- Gold
                CAST((coins * 10) % 10 AS INTEGER),  -- Silver
                CAST((coins * 100) % 10 AS INTEGER), -- Copper
                
                proficiencies, traits, feats, inventory, backstory,
                skill_proficiencies, savingThrowProficiencies,
                
                abilities_strength, abilities_dexterity, abilities_constitution,
                abilities_intelligence, abilities_wisdom, abilities_charisma,
                
                spell_settings_spell_ability, 
                spell_settings_spell_dc_misc_bonus, 
                spell_settings_spell_attack_misc_bonus
            FROM character
            """
        )

        db.execSQL("DROP TABLE character")

        db.execSQL("ALTER TABLE character_new RENAME TO character")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `character` ADD COLUMN `spell_settings_spell_slots` TEXT NOT NULL DEFAULT '{}'"
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `spells_new` (
                `spellId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `school` TEXT NOT NULL,
                `level` TEXT NOT NULL,
                `castTime` TEXT NOT NULL,
                `rangeType` TEXT NOT NULL,
                `rangeValue` INTEGER,
                `components` TEXT NOT NULL,
                `material` TEXT,
                `isRitual` INTEGER NOT NULL,
                `duration` TEXT NOT NULL,
                `isConcentration` INTEGER NOT NULL,
                `attackType` TEXT NOT NULL,
                `saveStat` TEXT,
                `damageType` TEXT,
                `damageDice` TEXT,
                `description` TEXT NOT NULL,
                `higherLevels` TEXT
            )
        """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO `spells_new` (
                `spellId`, `name`, `school`, `level`, `castTime`, 
                `rangeType`, `rangeValue`, `components`, `material`, 
                `isRitual`, `duration`, `isConcentration`, `attackType`, 
                `saveStat`, `damageType`, `damageDice`, `description`, `higherLevels`
            )
            SELECT 
                `spellId`, `name`, `school`, `level`, `castTime`, 
                'SELF', NULL, `components`, `material`, 
                `isRitual`, `duration`, `isConcentration`, `attackType`, 
                `saveStat`, `damageType`, `damageDice`, `description`, `higherLevels`
            FROM `spells`
        """.trimIndent()
        )

        db.execSQL("DROP TABLE `spells`")

        db.execSQL("ALTER TABLE `spells_new` RENAME TO `spells`")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            DELETE FROM spells 
            WHERE spellId NOT IN (
                SELECT MIN(spellId) 
                FROM spells 
                GROUP BY name
            )
        """.trimIndent()
        )

        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_spells_name` ON `spells` (`name`)"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE character ADD COLUMN imagePath TEXT
        """.trimIndent()
        )
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE character ADD COLUMN notes TEXT NOT NULL DEFAULT ''
        """.trimIndent()
        )
    }
}