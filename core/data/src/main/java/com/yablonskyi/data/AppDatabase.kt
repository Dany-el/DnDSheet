package com.yablonskyi.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yablonskyi.data.converters.Converters
import com.yablonskyi.data.converters.DiceRollConverters
import com.yablonskyi.data.dao.AttackDao
import com.yablonskyi.data.dao.BackupDao
import com.yablonskyi.data.dao.CharacterDao
import com.yablonskyi.data.dao.ClassDao
import com.yablonskyi.data.dao.RaceDao
import com.yablonskyi.data.dao.SpellDao
import com.yablonskyi.data.dao.DiceRollDao
import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.data.entity.RaceEntity
import com.yablonskyi.data.entity.SpellEntity
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.data.entity.RestoreCommitEntity

@Database(
    entities = [CharacterEntity::class, SpellEntity::class, AttackEntity::class,
        CharacterSpellCrossRefEntity::class,
        RaceEntity::class, CharacterClassEntity::class, DiceRollEntity::class, RestoreCommitEntity::class],
    version = 10,
    exportSchema = true,
)
@TypeConverters(Converters::class, DiceRollConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backupDao(): BackupDao
    abstract fun characterDao(): CharacterDao
    abstract fun spellDao(): SpellDao
    abstract fun attackDao(): AttackDao
    abstract fun raceDao(): RaceDao
    abstract fun classDao(): ClassDao
    abstract fun diceRollDao(): DiceRollDao
}
