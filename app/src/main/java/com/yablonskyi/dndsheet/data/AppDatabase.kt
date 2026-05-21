package com.yablonskyi.dndsheet.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yablonskyi.dndsheet.data.converters.Converters
import com.yablonskyi.dndsheet.data.dao.AttackDao
import com.yablonskyi.dndsheet.data.dao.CharacterDao
import com.yablonskyi.dndsheet.data.dao.ClassDao
import com.yablonskyi.dndsheet.data.dao.RaceDao
import com.yablonskyi.dndsheet.data.dao.SpellDao
import com.yablonskyi.dndsheet.data.model.character.Attack
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSpellCrossRef
import com.yablonskyi.dndsheet.data.model.character.Spell
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.data.model.rulebook.Race

@Database(
    entities = [Character::class, Spell::class, Attack::class,
        CharacterSpellCrossRef::class,
        Race::class, CharacterClass::class],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun spellDao(): SpellDao
    abstract fun attackDao(): AttackDao
    abstract fun raceDao(): RaceDao
    abstract fun classDao(): ClassDao
}