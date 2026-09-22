package com.yablonskyi.model.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupManifest(
    val format: String,
    val formatVersion: Int,
    val createdAt: String,
    val sourceAppVersion: String,
    val counts: BackupRecordCounts,
    val dataSha256: String,
    val assets: List<BackupAsset>,
)

@Serializable
data class BackupRecordCounts(
    val characters: Int,
    val attacks: Int,
    val diceRolls: Int,
    val spells: Int,
    val characterSpells: Int,
    val races: Int,
    val classes: Int,
)

@Serializable
data class BackupAsset(val id: String, val path: String, val sizeBytes: Long, val sha256: String)
