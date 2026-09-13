package com.yablonskyi.pdf

import com.yablonskyi.model.character.*
import com.yablonskyi.pdf.html.CharacterSheetPdfGenerator
import com.yablonskyi.pdf.html.HtmlTemplateRenderer
import kotlinx.coroutines.Dispatchers
import com.yablonskyi.ui.provider.CharacterImageLoader
import org.junit.Assert.*
import org.junit.Test

class CharacterSheetTemplateDataTest {
    private val generator = CharacterSheetPdfGenerator(object : CharacterImageLoader {
        override fun loadImageBytes(imagePath: String?): ByteArray? = null
    }, HtmlTemplateRenderer { _, _ -> error("Template data test must not start Python") }, Dispatchers.Unconfined)

    @Test
    fun `export preserves expertise and applies jack of all trades only to untrained skills`() {
        val character = Character(hasJackOfAllTrades = true, skillProficiencies = mapOf(
            Skill.STEALTH to ProficiencyLevel.EXPERT,
            Skill.PERCEPTION to ProficiencyLevel.PROFICIENT,
            Skill.ARCANA to ProficiencyLevel.NONE
        ))
        val data = generator.buildTemplateData(CharacterSheet(character, emptyList(), emptyList()))
        val skills = data["skills"] as List<*>
        fun level(name: String) = skills.map { it as Map<*, *> }
            .first { it["id"] == name }["proficiency"]
        assertEquals("EXPERT", level("STEALTH"))
        assertEquals("PROFICIENT", level("PERCEPTION"))
        assertEquals("HALF", level("ARCANA"))
        assertEquals("HALF", level("HISTORY"))
    }

    @Test
    fun `export includes narrative inventory and spells even without a casting ability`() {
        val character = Character(inventory = "Rope", feats = "Alert", notes = "Allies",
            backstory = "Story", proficiencies = "Common", coins = Money(gold = 7))
        val spell = Spell(name = "Innate spell", description = "Description",
            higherLevels = "Scaling", material = "A feather", isRitual = true)
        val data = generator.buildTemplateData(CharacterSheet(character, listOf(spell), emptyList()))
        val exported = data["character"] as Map<*, *>
        assertEquals("Rope", exported["inventory"])
        assertEquals("Alert", exported["feats"])
        assertEquals("Allies", exported["notes"])
        assertEquals(7, (exported["coins"] as Map<*, *>)["gold"])
        val detail = (data["spell_details"] as List<*>).single() as Map<*, *>
        assertEquals("Description", detail["description"])
        assertEquals("Scaling", detail["higherLevels"])
        assertEquals("A feather", detail["material"])
        assertEquals(true, detail["isRitual"])
        assertEquals("", data["spells"])
    }
}
