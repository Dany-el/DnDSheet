package com.yablonskyi.data.backup

import com.yablonskyi.data.entity.CharacterEntity
import com.yablonskyi.data.entity.AttackEntity
import com.yablonskyi.data.entity.DiceRollEntity
import com.yablonskyi.data.entity.SpellEntity
import com.yablonskyi.data.entity.CharacterSpellCrossRefEntity
import com.yablonskyi.data.entity.RaceEntity
import com.yablonskyi.data.entity.CharacterClassEntity

/** Complete database-only snapshot; images and preferences require external write coordination. */
data class BackupRoomSnapshot(
    val characters: List<CharacterEntity>,
    val attacks: List<AttackEntity>,
    val diceRolls: List<DiceRollEntity>,
    val spells: List<SpellEntity>,
    val characterSpells: List<CharacterSpellCrossRefEntity>,
    val races: List<RaceEntity>,
    val classes: List<CharacterClassEntity>,
)
