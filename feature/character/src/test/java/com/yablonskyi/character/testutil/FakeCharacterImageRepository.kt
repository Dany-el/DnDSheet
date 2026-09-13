package com.yablonskyi.character.testutil

import com.yablonskyi.domain.repository.CharacterImageRepository

class FakeCharacterImageRepository : CharacterImageRepository {
    var failure: Exception? = null
    val replacements = mutableListOf<Pair<Long, String>>()
    override suspend fun replace(characterId: Long, uri: String) { failure?.let { throw it }; replacements += characterId to uri }
}
