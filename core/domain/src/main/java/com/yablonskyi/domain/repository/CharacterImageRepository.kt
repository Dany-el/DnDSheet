package com.yablonskyi.domain.repository

interface CharacterImageRepository {
    /** Writes the replacement, commits its path, then removes the previous owned image. */
    suspend fun replace(characterId: Long, uri: String)
}
