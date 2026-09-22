package com.yablonskyi.model.backup

import java.time.Instant
import java.time.format.DateTimeParseException

enum class BackupValidationFailure { UNSUPPORTED_VERSION, INVALID_DATA, SIZE_LIMIT_EXCEEDED }

class BackupValidationException(val failure: BackupValidationFailure, message: String) :
    IllegalArgumentException(message)

data class BackupValidationLimits(
    val characters: Int = 10_000,
    val attacks: Int = 100_000,
    val diceRolls: Int = 1_000_000,
    val spells: Int = 100_000,
    val characterSpells: Int = 1_000_000,
    val races: Int = 100_000,
    val classes: Int = 100_000,
    val assets: Int = 10_000,
    val totalRecords: Long = 2_000_000,
) {
    init {
        require(
            listOf(characters, attacks, diceRolls, spells, characterSpells, races, classes, assets)
                .all { it > 0 } && totalRecords > 0,
        )
    }
}

/** Structural validation only: the archive reader must also enforce byte limits and verify hashes. */
object BackupValidator {
    const val FORMAT = "dnd-sheet-backup"
    const val VERSION = 1
    private val sha256 = Regex("[0-9a-f]{64}")
    private val assetId = Regex("[A-Za-z0-9_-]{1,100}")
    private val assetPath = Regex("images/[A-Za-z0-9_-]{1,100}\\.[A-Za-z0-9]{1,10}")

    /** Call before decoding version-specific data. No older formats are supported yet. */
    fun validateManifest(
        manifest: BackupManifest,
        limits: BackupValidationLimits = BackupValidationLimits(),
    ) {
        check(manifest.format == FORMAT, "Unknown backup format")
        if (manifest.formatVersion != VERSION) {
            throw BackupValidationException(BackupValidationFailure.UNSUPPORTED_VERSION, "Unsupported backup version")
        }
        check(manifest.createdAt.endsWith("Z"), "Creation time must be UTC")
        try {
            Instant.parse(manifest.createdAt)
        } catch (_: DateTimeParseException) {
            invalid("Invalid creation time")
        }
        check(manifest.sourceAppVersion.isNotBlank(), "Missing app version")
        check(sha256.matches(manifest.dataSha256), "Invalid data checksum")
        with(manifest.counts) {
            check(listOf(characters, attacks, diceRolls, spells, characterSpells, races, classes).all { it >= 0 },
                "Negative record count")
            limit(characters, limits.characters, "characters")
            limit(attacks, limits.attacks, "attacks")
            limit(diceRolls, limits.diceRolls, "dice rolls")
            limit(spells, limits.spells, "spells")
            limit(characterSpells, limits.characterSpells, "character spell associations")
            limit(races, limits.races, "races")
            limit(classes, limits.classes, "classes")
            val total = listOf(characters, attacks, diceRolls, spells, characterSpells, races, classes)
                .sumOf(Int::toLong)
            limit(total, limits.totalRecords, "total records")
        }
        limit(manifest.assets.size, limits.assets, "assets")
        unique(manifest.assets.map { it.id }, "asset IDs")
        unique(manifest.assets.map { it.path }, "asset paths")
        manifest.assets.forEach {
            check(assetId.matches(it.id), "Invalid asset ID")
            check(assetPath.matches(it.path), "Unsafe asset path")
            check(it.sizeBytes > 0, "Empty asset")
            check(sha256.matches(it.sha256), "Invalid asset checksum")
        }
    }

    fun validate(
        manifest: BackupManifest,
        data: BackupDataV1,
        limits: BackupValidationLimits = BackupValidationLimits(),
    ) {
        validateManifest(manifest, limits)
        check(manifest.counts == BackupRecordCounts(
            data.characters.size, data.attacks.size, data.diceRolls.size, data.spells.size,
            data.characterSpells.size, data.races.size, data.classes.size,
        ), "Record counts do not match")
        val characters = numericIds(data.characters.map { it.id }, "characters")
        val spells = numericIds(data.spells.map { it.spellId }, "spells")
        numericIds(data.attacks.map { it.attackId }, "attacks")
        numericIds(data.diceRolls.map { it.id }, "dice rolls")
        unique(data.spells.map { it.name }, "spell names") // Room has a unique name index.
        unique(data.races.map { it.id }, "race IDs")
        unique(data.classes.map { it.id }, "class IDs")
        check(data.races.all { it.id.isNotBlank() } && data.classes.all { it.id.isNotBlank() }, "Empty library ID")
        unique(data.characterSpells.map { it.characterId to it.spellId }, "character spell associations")
        check(data.attacks.all { it.characterId in characters }, "Attack references missing character")
        check(data.diceRolls.all { it.characterId in characters }, "Roll references missing character")
        check(data.characterSpells.all { it.characterId in characters && it.spellId in spells },
            "Association references missing record")
        val assets = manifest.assets.map { it.id }.toSet()
        val referencedAssets = data.characters.mapNotNull { it.imageAssetId }.toSet()
        check(assets == referencedAssets, "Missing or unreferenced image assets")
    }

    private fun numericIds(ids: List<Long>, label: String): Set<Long> {
        check(ids.all { it > 0 && it < Long.MAX_VALUE }, "Invalid $label ID")
        return unique(ids, label)
    }

    private fun <T> unique(values: List<T>, label: String): Set<T> = values.toSet().also {
        check(it.size == values.size, "Duplicate $label")
    }

    private fun check(condition: Boolean, message: String) {
        if (!condition) invalid(message)
    }

    private fun limit(value: Int, maximum: Int, label: String) = limit(value.toLong(), maximum.toLong(), label)

    private fun limit(value: Long, maximum: Long, label: String) {
        if (value > maximum) {
            throw BackupValidationException(
                BackupValidationFailure.SIZE_LIMIT_EXCEEDED,
                "Too many $label",
            )
        }
    }

    private fun invalid(message: String): Nothing =
        throw BackupValidationException(BackupValidationFailure.INVALID_DATA, message)
}
