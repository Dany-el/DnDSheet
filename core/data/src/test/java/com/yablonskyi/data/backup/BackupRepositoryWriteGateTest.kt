package com.yablonskyi.data.backup

import androidx.room.Room
import com.yablonskyi.data.AppDatabase
import com.yablonskyi.data.repository.character.AttackRepositoryImpl
import com.yablonskyi.data.repository.character.CharacterRepositoryImpl
import com.yablonskyi.data.repository.character.SpellRepositoryImpl
import com.yablonskyi.data.repository.compendium.ClassRepositoryImpl
import com.yablonskyi.data.repository.compendium.RaceRepositoryImpl
import com.yablonskyi.data.repository.dice.DiceRollRepositoryImpl
import com.yablonskyi.data.rulebook.BuiltInRulebookLoader
import com.yablonskyi.domain.backup.BackupAccessGate
import com.yablonskyi.domain.backup.BackupRecoveryRequiredException
import com.yablonskyi.model.character.Attack
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.Spell
import com.yablonskyi.model.dice.DiceGroup
import com.yablonskyi.model.dice.SavedDiceRoll
import com.yablonskyi.model.rulebook.CharacterClass
import com.yablonskyi.model.rulebook.Race
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRepositoryWriteGateTest {
    @Test fun givenRecoveryPending_whenAnyPortableRepositoryWrites_thenEveryWriteIsRejected() = runTest {
        val context = RuntimeEnvironment.getApplication()
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val gate = BackupAccessGate()
            val writes = listOf<suspend () -> Unit>(
                { CharacterRepositoryImpl(database, database.characterDao(), database.attackDao(), database.spellDao(), gate)
                    .insertCharacter(Character(name = "Character")) },
                { AttackRepositoryImpl(database.attackDao(), gate).insertAttack(Attack()) },
                { SpellRepositoryImpl(database.spellDao(), gate).insertSpell(Spell(name = "Spell")) },
                { RaceRepositoryImpl(BuiltInRulebookLoader(context), database.raceDao(), gate).insert(Race(name = "Race")) },
                { ClassRepositoryImpl(BuiltInRulebookLoader(context), database.classDao(), gate)
                    .insert(CharacterClass(id = "class", name = "Class")) },
                { DiceRollRepositoryImpl(database.diceRollDao(), gate).addDiceRoll(
                    SavedDiceRoll(0, 1, "Roll", listOf(1), null, 1, listOf(DiceGroup(20, 1)), 1),
                ).getOrThrow() },
            )

            writes.forEach { write ->
                assertTrue(runCatching { write() }.exceptionOrNull() is BackupRecoveryRequiredException)
            }
            assertTrue(database.backupDao().snapshot().let {
                it.characters.isEmpty() && it.attacks.isEmpty() && it.spells.isEmpty() &&
                    it.races.isEmpty() && it.classes.isEmpty() && it.diceRolls.isEmpty()
            })
        } finally {
            database.close()
        }
    }
}
