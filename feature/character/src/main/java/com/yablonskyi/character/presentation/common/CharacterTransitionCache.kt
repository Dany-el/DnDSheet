package com.yablonskyi.character.presentation.common

import com.yablonskyi.model.character.Character
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterTransitionCache @Inject constructor() {
    private var character: Character? = null

    @Synchronized
    fun put(character: Character) {
        this.character = character
    }

    @Synchronized
    fun take(id: Long): Character? {
        val cached = character?.takeIf { it.id == id }
        character = null
        return cached
    }
}