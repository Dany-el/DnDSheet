package com.yablonskyi.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.yablonskyi.data.backup.BackupRoomSnapshot
import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.data.entity.SpellEntity
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.data.entity.RaceEntity
import com.yablonskyi.data.entity.CharacterClassEntity
import com.yablonskyi.data.entity.RestoreCommitEntity

/** Internal database primitive. Production restore must use the recovery coordinator. */
@Dao
abstract class BackupDao {
    @Query("SELECT operationId FROM restore_commit WHERE id = 1")
    abstract suspend fun committedRestore(): String?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertCommitMarker(marker: RestoreCommitEntity)

    @Query("DELETE FROM restore_commit WHERE id = 1 AND operationId = :operationId")
    abstract suspend fun clearCommitMarker(operationId: String)

    @Transaction
    open suspend fun commitRestore(snapshot: BackupRoomSnapshot, operationId: String) {
        replace(snapshot)
        insertCommitMarker(RestoreCommitEntity(operationId = operationId))
    }

    @Query("SELECT * FROM character ORDER BY id")
    abstract suspend fun characters(): List<CharacterEntity>

    @Insert
    abstract suspend fun insertCharacterEntity(rows: List<CharacterEntity>)

    @Query("DELETE FROM character")
    abstract suspend fun clearCharacterEntity()

    @Query("SELECT * FROM attacks ORDER BY attackId")
    abstract suspend fun attacks(): List<AttackEntity>

    @Insert
    abstract suspend fun insertAttackEntity(rows: List<AttackEntity>)

    @Query("DELETE FROM attacks")
    abstract suspend fun clearAttackEntity()

    @Query("SELECT * FROM dice_rolls ORDER BY id")
    abstract suspend fun diceRolls(): List<DiceRollEntity>

    @Insert
    abstract suspend fun insertDiceRollEntity(rows: List<DiceRollEntity>)

    @Query("DELETE FROM dice_rolls")
    abstract suspend fun clearDiceRollEntity()

    @Query("SELECT * FROM spells ORDER BY spellId")
    abstract suspend fun spells(): List<SpellEntity>

    @Insert
    abstract suspend fun insertSpellEntity(rows: List<SpellEntity>)

    @Query("DELETE FROM spells")
    abstract suspend fun clearSpellEntity()

    @Query("SELECT * FROM character_spell_cross_ref ORDER BY characterId, spellId")
    abstract suspend fun characterSpells(): List<CharacterSpellCrossRefEntity>

    @Insert
    abstract suspend fun insertCharacterSpellCrossRefEntity(rows: List<CharacterSpellCrossRefEntity>)

    @Query("DELETE FROM character_spell_cross_ref")
    abstract suspend fun clearCharacterSpellCrossRefEntity()

    @Query("SELECT * FROM races ORDER BY id")
    abstract suspend fun races(): List<RaceEntity>

    @Insert
    abstract suspend fun insertRaceEntity(rows: List<RaceEntity>)

    @Query("DELETE FROM races")
    abstract suspend fun clearRaceEntity()

    @Query("SELECT * FROM classes ORDER BY id")
    abstract suspend fun classes(): List<CharacterClassEntity>

    @Insert
    abstract suspend fun insertCharacterClassEntity(rows: List<CharacterClassEntity>)

    @Query("DELETE FROM classes")
    abstract suspend fun clearCharacterClassEntity()

    @Transaction
    open suspend fun snapshot(): BackupRoomSnapshot = BackupRoomSnapshot(
        characters = characters(),
        attacks = attacks(),
        diceRolls = diceRolls(),
        spells = spells(),
        characterSpells = characterSpells(),
        races = races(),
        classes = classes(),
    )

    /** ABORT on any conflict; never regenerate IDs or trim history. */
    @Transaction
    open suspend fun replace(snapshot: BackupRoomSnapshot) {
        clearCharacterSpellCrossRefEntity()
        clearAttackEntity()
        clearDiceRollEntity()
        clearCharacterEntity()
        clearSpellEntity()
        clearRaceEntity()
        clearCharacterClassEntity()
        insertCharacterEntity(snapshot.characters)
        insertSpellEntity(snapshot.spells)
        insertRaceEntity(snapshot.races)
        insertCharacterClassEntity(snapshot.classes)
        insertAttackEntity(snapshot.attacks)
        insertDiceRollEntity(snapshot.diceRolls)
        insertCharacterSpellCrossRefEntity(snapshot.characterSpells)
    }
}
