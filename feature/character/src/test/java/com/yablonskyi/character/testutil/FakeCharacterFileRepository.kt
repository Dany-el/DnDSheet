package com.yablonskyi.character.testutil

import com.yablonskyi.domain.repository.CharacterFileRepository
import com.yablonskyi.model.character.CharacterSheet
import kotlinx.coroutines.CompletableDeferred

class FakeCharacterFileRepository : CharacterFileRepository {
    var imported = emptyList<CharacterSheet>()
    var exportedIds = emptyList<Long>()
    var input = emptyList<CharacterSheet>()
    var failure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null
    override suspend fun read(uri: String) = input
    override suspend fun import(sheets: List<CharacterSheet>) { gate?.await(); failure?.let { throw it }; imported = sheets }
    override suspend fun export(uri: String, characterIds: List<Long>) { exportedIds = characterIds }
}
