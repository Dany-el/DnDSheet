package com.yablonskyi.pdf.html

import android.util.Base64
import com.yablonskyi.pdf.module.PdfIoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.CharacterSheet
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.ui.provider.CharacterImageLoader
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CharacterSheetPdfGenerator @Inject constructor(
    private val imageLoader: CharacterImageLoader,
    private val templateRenderer: HtmlTemplateRenderer,
    @param:PdfIoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    fun buildTemplateData(sheet: CharacterSheet): Map<String, Any> {
        val character = sheet.character
        val modFormat = { mod: Int -> if (mod >= 0) "+$mod" else "$mod" }

        return mapOf(
            "character" to mapOf(
                "name"        to character.name,
                "race"        to character.race,
                "charClass"   to character.charClass,
                "level"       to character.level,
                "hitDice"     to character.hitDice,
                "currentHp"   to character.currentHp,
                "maxHp"       to character.maxHp,
                "tempHp"      to character.tempHp,
                "totalAc"     to character.armorClass,
                "initiative"  to modFormat(character.initiativeBonus),
                "speed"       to character.speed,
                "profBonus"   to character.getProfBonus(),
                "traits"      to character.traits,
                "backstory"   to character.backstory,
                "subClass"    to character.subClass,
                "feats"       to character.feats,
                "inventory"   to character.inventory,
                "notes"       to character.notes.joinToString("\n\n") { note ->
                    "${note.topic}\n${note.text.plainText}"
                },
                "proficiencies" to character.proficiencies,
                "coins"       to mapOf(
                    "gold" to character.coins.gold,
                    "silver" to character.coins.silver,
                    "copper" to character.coins.copper
                ),
            ),
            "abilities" to Ability.playableAbilities.map { ability ->
                val score = character.abilityBlock.getScore(ability)
                val mod   = character.abilityBlock.getModifier(ability)
                mapOf(
                    "id"       to ability.name,
                    "score"    to score,
                    "modifier" to modFormat(mod)
                )
            },
            "saving_throws" to Ability.playableAbilities.map { ability ->
                mapOf(
                    "id"         to ability.name,
                    "modifier"   to modFormat(character.getSavingThrowMod(ability)),
                    "proficient" to (ability in character.savingThrowProficiencies)
                )
            },
            "skills" to Skill.entries.map { skill ->
                mapOf(
                    "id"         to skill.name,
                    "ability"    to skill.defaultAbility.name,
                    "modifier"   to modFormat(character.getSkillMod(skill)),
                    "proficiency" to (character.skillProficiencies[skill]
                        ?.takeUnless { it == ProficiencyLevel.NONE }
                        ?: if (character.hasJackOfAllTrades) ProficiencyLevel.HALF
                        else ProficiencyLevel.NONE).name,
                    "proficient" to (character.skillProficiencies[skill]
                        ?.let { it != ProficiencyLevel.NONE } ?: false)
                )
            },
            "attacks" to sheet.attacks.map { attack ->
                mapOf(
                    "name"       to attack.name,
                    "bonus"      to modFormat(attack.bonusToHit),
                    "damage"     to com.yablonskyi.ui.utils.AttackCalculator(character, attack).getDamageString(),
                    "damageType" to attack.damageType.name
                )
            },
            "spell_details" to sheet.spells.map { spell ->
                mapOf(
                    "name" to spell.name,
                    "level" to spell.level.value,
                    "school" to spell.school.name,
                    "castTime" to spell.castTime.name,
                    "rangeType" to spell.rangeType.name,
                    "rangeValue" to (spell.rangeValue?.toString() ?: ""),
                    "duration" to spell.duration.name,
                    "components" to spell.components.map { it.name },
                    "material" to (spell.material ?: ""),
                    "isRitual" to spell.isRitual,
                    "isConcentration" to spell.isConcentration,
                    "attackType" to spell.attackType.name,
                    "saveStat" to (spell.saveStat?.takeUnless { it == Ability.NONE }?.name ?: ""),
                    "damageDice" to (spell.damageDice ?: ""),
                    "damageType" to (spell.damageType?.name ?: ""),
                    "description" to spell.description,
                    "higherLevels" to (spell.higherLevels ?: "")
                )
            },
            "spells" to if (character.spellSettings.spellCastingAbility != null &&
                character.spellSettings.spellCastingAbility != Ability.NONE) mapOf(
                "ability"      to character.spellSettings.spellCastingAbility!!.name,
                "saveDc"       to character.getSpellSaveDC(),
                "attackBonus"  to modFormat(character.getSpellAttackBonus()),
                "slots"        to character.spellSettings.spellSlots
                    .filter { (_, slot) -> slot.max > 0 }
                    .map { (level, slot) ->
                        mapOf("level" to level.value, "max" to slot.max)
                    }
            ) else ""
        )
    }

    suspend fun generateHtml(sheet: CharacterSheet, languageCode: String = "en"): String =
        withContext(ioDispatcher) {
            val data = buildTemplateData(sheet).toMutableMap()
            imageLoader.loadImageBytes(sheet.character.imagePath)?.let { bytes ->
                val mime = java.net.URLConnection.guessContentTypeFromStream(bytes.inputStream())
                if (mime?.startsWith("image/") == true) {
                    data["portrait_data_uri"] = "data:$mime;base64," +
                        Base64.encodeToString(bytes, Base64.NO_WRAP)
                }
            }
            templateRenderer.render(data, languageCode)
        }
}
