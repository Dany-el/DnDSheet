package com.yablonskyi.dndsheet.ui.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.IXmlParserFactory
import com.itextpdf.kernel.utils.XmlProcessorCreator
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.yablonskyi.dndsheet.R
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.CharacterSheet
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.SpellRangeType
import com.yablonskyi.dndsheet.ui.attack.AttackCalculator
import org.xml.sax.XMLReader
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory

object PdfExporter {

    fun generateCustomSheet(context: Context, sheet: CharacterSheet): File? {
        clearOldPdfs(context)

        return try {
            XmlProcessorCreator.setXmlParserFactory(object : IXmlParserFactory {
                override fun createDocumentBuilderInstance(
                    namespaceAware: Boolean,
                    ignoringComments: Boolean
                ): DocumentBuilder {
                    val factory = DocumentBuilderFactory.newInstance()
                    factory.isNamespaceAware = namespaceAware
                    factory.isIgnoringComments = ignoringComments
                    return factory.newDocumentBuilder()
                }

                override fun createXMLReaderInstance(
                    namespaceAware: Boolean,
                    validating: Boolean
                ): XMLReader {
                    val factory = SAXParserFactory.newInstance()
                    factory.isNamespaceAware = namespaceAware
                    factory.isValidating = validating
                    return factory.newSAXParser().xmlReader
                }

                override fun createTransformerInstance(): Transformer {
                    val factory = TransformerFactory.newInstance()
                    return factory.newTransformer()
                }
            })

            val char = sheet.character

            val outputDir = File(context.cacheDir, "pdf")
            if (!outputDir.exists()) outputDir.mkdirs()
            val safeName = char.name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            val outputFile = File(outputDir, "${safeName}_Sheet.pdf")

            val writer = PdfWriter(outputFile.absolutePath)
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            val fontBytes = context.assets.open("font/Roboto-Regular.ttf").readBytes()
            val customFont = PdfFontFactory.createFont(
                fontBytes,
                PdfEncodings.IDENTITY_H,
                PdfFontFactory.EmbeddingStrategy.FORCE_EMBEDDED
            )
            document.setFont(customFont)

            // Header
            val headerDiv = Div().setKeepTogether(true)
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f)))
            headerTable.setWidth(UnitValue.createPercentValue(100f))
            headerTable.setMarginBottom(15f)

            val imageCell = Cell()
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.LEFT)

            val portraitImage = loadCharacterImage(context, char.imagePath)
            if (portraitImage != null) {
                imageCell.add(portraitImage)
            } else {
                imageCell.add(Paragraph("[ No Portrait ]").setFontColor(ColorConstants.LIGHT_GRAY))
            }

            headerTable.addCell(imageCell)

            val textCell = Cell().setBorder(Border.NO_BORDER)
            textCell.add(Paragraph(char.name).simulateBold().setFontSize(24f))
            val subtitle =
                "${char.race}\n${char.charClass}, ${char.subClass} | ${context.getString(R.string.spell_level)} ${char.level}"
            textCell.add(Paragraph(subtitle).simulateItalic().setFontSize(14f))

            headerTable.addCell(textCell)
            headerDiv.add(headerTable)
            document.add(headerDiv)

            // Core Stats
            val statsDiv = Div().setKeepTogether(true)
            val statsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f, 1f)))
            statsTable.setWidth(UnitValue.createPercentValue(100f))
            statsTable.setMarginBottom(15f)

            statsTable.addCell(createHeaderCell(context.getString(R.string.health)))
            statsTable.addCell(createHeaderCell(context.getString(R.string.char_ac)))
            statsTable.addCell(createHeaderCell(context.getString(R.string.char_speed)))
            statsTable.addCell(createHeaderCell(context.getString(R.string.proficiency_bonus)))

            statsTable.addCell(createValueCell("${char.currentHp} / ${char.maxHp}"))
            statsTable.addCell(createValueCell(char.getTotalAc().toString()))
            statsTable.addCell(createValueCell("${char.speed} ${context.getString(R.string.feets)}"))
            statsTable.addCell(createValueCell(char.getProfBonus().withSign()))

            statsDiv.add(statsTable)
            document.add(statsDiv)

            // Abilities
            val abilitiesDiv = Div().setKeepTogether(true)
            abilitiesDiv.add(
                Paragraph(context.getString(R.string.tab_abilities)).simulateBold().setFontSize(16f)
            )

            val abilitiesTable =
                Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f, 1f, 1f, 1f)))
            abilitiesTable.setWidth(UnitValue.createPercentValue(100f))
            abilitiesTable.setMarginBottom(15f)

            Ability.entries.filter { it != Ability.NONE }.forEach { ability ->
                val abilityName = context.getString(ability.nameRes)
                abilitiesTable.addCell(createHeaderCell(abilityName))
            }

            Ability.entries.filter { it != Ability.NONE }.forEach { ability ->
                val score = char.abilityBlock.getScore(ability)
                val mod = char.getAbilityMod(ability).withSign()
                abilitiesTable.addCell(createValueCell("$score\n($mod)"))
            }

            abilitiesDiv.add(abilitiesTable)
            document.add(abilitiesDiv)

            // Saving Throws
            val savesDiv = Div().setKeepTogether(true)
            savesDiv.add(
                Paragraph(context.getString(R.string.saving_throw)).simulateBold().setFontSize(16f)
                    .setMarginTop(10f)
            )

            val savesTable =
                Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 1f, 1f, 4f, 1f)))
            savesTable.setWidth(UnitValue.createPercentValue(100f))
            savesTable.setMarginBottom(15f)

            val validAbilities = Ability.entries.filter { it != Ability.NONE }
            val abilityRows = 3

            for (i in 0 until abilityRows) {
                val leftAbility = validAbilities.getOrNull(i)
                if (leftAbility != null) addSaveCellsToTable(leftAbility, char, context, savesTable)

                val rightAbility = validAbilities.getOrNull(i + abilityRows)
                if (rightAbility != null) addSaveCellsToTable(
                    rightAbility,
                    char,
                    context,
                    savesTable
                )
            }

            savesDiv.add(savesTable)
            document.add(savesDiv)

            // Skills
            val skillsDiv = Div().setKeepTogether(true)
            skillsDiv.add(
                Paragraph(context.getString(R.string.skills)).simulateBold().setFontSize(16f)
                    .setMarginTop(10f)
            )

            val skillsTable =
                Table(UnitValue.createPercentArray(floatArrayOf(1f, 4f, 1f, 1f, 4f, 1f)))
            skillsTable.setWidth(UnitValue.createPercentValue(100f))
            skillsTable.setMarginBottom(15f)

            val sortedSkills = Skill.entries.sortedBy { context.getString(it.nameRes) }
            val rows = kotlin.math.ceil(sortedSkills.size / 2.0).toInt()

            for (i in 0 until rows) {
                val leftSkill = sortedSkills.getOrNull(i)
                if (leftSkill != null) addSkillCellsToTable(leftSkill, char, context, skillsTable)

                val rightSkill = sortedSkills.getOrNull(i + rows)
                if (rightSkill != null) {
                    addSkillCellsToTable(rightSkill, char, context, skillsTable)
                } else {
                    skillsTable.addCell(Cell().setBorder(Border.NO_BORDER))
                    skillsTable.addCell(Cell().setBorder(Border.NO_BORDER))
                    skillsTable.addCell(Cell().setBorder(Border.NO_BORDER))
                }
            }

            skillsDiv.add(skillsTable)
            document.add(skillsDiv)

            // Inventory
            val inventoryDiv = Div().setKeepTogether(true)
            inventoryDiv.add(
                Paragraph(context.getString(R.string.inventory)).simulateBold().setFontSize(16f)
                    .setMarginTop(10f)
            )

            val moneyTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
            moneyTable.setWidth(UnitValue.createPercentValue(100f))
            moneyTable.setMarginBottom(10f)

            moneyTable.addCell(createHeaderCell(context.getString(R.string.gold)))
            moneyTable.addCell(createHeaderCell(context.getString(R.string.silver)))
            moneyTable.addCell(createHeaderCell(context.getString(R.string.copper)))

            moneyTable.addCell(createValueCell(char.coins.gold.toString()))
            moneyTable.addCell(createValueCell(char.coins.silver.toString()))
            moneyTable.addCell(createValueCell(char.coins.copper.toString()))

            inventoryDiv.add(moneyTable)

            if (char.inventory.isNotBlank()) {
                val borderedText = Paragraph(char.inventory)
                    .setBorder(SolidBorder(ColorConstants.BLACK, 1f))
                    .setPadding(10f)
                    .setMarginBottom(15f)
                    .setTextAlignment(TextAlignment.JUSTIFIED)

                inventoryDiv.add(borderedText)
            }
            document.add(inventoryDiv)

            // Attacks
            if (sheet.attacks.isNotEmpty()) {
                val attacksDiv = Div().setKeepTogether(true)
                attacksDiv.add(
                    Paragraph(context.getString(R.string.tab_attacks)).simulateBold()
                        .setFontSize(16f)
                )

                val attacksTable = Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 2f, 2f)))
                attacksTable.setWidth(UnitValue.createPercentValue(100f))
                attacksTable.setMarginBottom(15f)

                attacksTable.addCell(createHeaderCell(context.getString(R.string.spell_name)))
                attacksTable.addCell(createHeaderCell(context.getString(R.string.bonus_hit)))
                attacksTable.addCell(createHeaderCell(context.getString(R.string.damage)))
                attacksTable.addCell(createHeaderCell(context.getString(R.string.notes)))

                for (attack in sheet.attacks) {
                    val calculator = AttackCalculator(char, attack)

                    val damageTypeName = context.getString(attack.damageType.resId)

                    attacksTable.addCell(Cell().add(Paragraph(attack.name)))
                    attacksTable.addCell(
                        Cell().add(
                            Paragraph(calculator.getToHitModifier().withSign())
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    )
                    attacksTable.addCell(
                        Cell().add(
                            Paragraph("${attack.damageDice} $damageTypeName").setTextAlignment(
                                TextAlignment.CENTER
                            )
                        )
                    )
                    attacksTable.addCell(Cell().add(Paragraph(attack.notes)))
                }
                attacksDiv.add(attacksTable)
                document.add(attacksDiv)
            }

            // Text Blocks
            fun addTextBlock(title: String, content: String) {
                if (content.isNotBlank()) {
                    val blockDiv = Div().setKeepTogether(true)
                    val borderedText = Paragraph(content)
                        .setBorder(SolidBorder(ColorConstants.BLACK, 1f))
                        .setPadding(10f)
                        .setMarginBottom(15f)
                        .setTextAlignment(TextAlignment.JUSTIFIED)

                    blockDiv.add(Paragraph(title).simulateBold().setFontSize(14f).setMarginTop(10f))
                    blockDiv.add(borderedText)

                    document.add(blockDiv)
                }
            }

            addTextBlock(context.getString(R.string.proficiencies), char.proficiencies)
            addTextBlock(context.getString(R.string.traits), char.traits)
            addTextBlock(context.getString(R.string.feats), char.feats)
            addTextBlock(context.getString(R.string.backstory), char.backstory)

            // Spells
            if (sheet.spells.isNotEmpty()) {
                val spellsHeaderDiv = Div().setKeepTogether(true)
                spellsHeaderDiv.add(
                    Paragraph(context.getString(R.string.tab_spells)).simulateBold()
                        .setFontSize(16f)
                )

                val spellStatsTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f, 1f)))
                spellStatsTable.setWidth(UnitValue.createPercentValue(100f))
                spellStatsTable.setMarginBottom(10f)

                val castingAbilityName = char.spellSettings.spellCastingAbility?.let {
                    if (it != Ability.NONE) context.getString(it.nameRes) else " "
                } ?: " "
                val spellDc = char.getSpellSaveDC().toString()
                val spellAtk = char.getSpellAttackBonus().withSign()

                spellStatsTable.addCell(createHeaderCell(context.getString(R.string.spell_save_stat)))
                spellStatsTable.addCell(createHeaderCell(context.getString(R.string.saving_throw)))
                spellStatsTable.addCell(createHeaderCell(context.getString(R.string.spell_attack_bonus)))

                spellStatsTable.addCell(createValueCell(castingAbilityName))
                spellStatsTable.addCell(createValueCell(spellDc))
                spellStatsTable.addCell(createValueCell(spellAtk))

                spellsHeaderDiv.add(spellStatsTable)
                document.add(spellsHeaderDiv)

                val spellsTable =
                    Table(UnitValue.createPercentArray(floatArrayOf(2f, 1f, 1f, 1f, 3f)))
                spellsTable.setWidth(UnitValue.createPercentValue(100f))

                spellsTable.addCell(createHeaderCell(context.getString(R.string.spell_name)))
                spellsTable.addCell(createHeaderCell(context.getString(R.string.spell_level)))
                spellsTable.addCell(createHeaderCell(context.getString(R.string.msg_casting_time)))
                spellsTable.addCell(createHeaderCell(context.getString(R.string.msg_distance)))
                spellsTable.addCell(createHeaderCell(context.getString(R.string.spell_description)))

                for (spell in sheet.spells) {
                    val levelName = context.getString(spell.level.resId)
                    val castTimeName = context.getString(spell.castTime.resId)
                    val rangeName = context.getString(spell.rangeType.resId)

                    spellsTable.addCell(Cell().add(Paragraph(spell.name)))
                    spellsTable.addCell(
                        Cell().add(Paragraph(levelName)).setTextAlignment(TextAlignment.CENTER)
                    )
                    spellsTable.addCell(
                        Cell().add(Paragraph(castTimeName)).setTextAlignment(TextAlignment.CENTER)
                    )

                    val actualRange =
                        if (spell.rangeType == SpellRangeType.DISTANCE) "${spell.rangeValue} ${
                            context.getString(R.string.feets)
                        }" else rangeName
                    spellsTable.addCell(Cell().add(Paragraph(actualRange)))

                    spellsTable.addCell(Cell().add(Paragraph(spell.description).setFontSize(9f)))
                }
                document.add(spellsTable)
            }

            document.close()
            Log.d("PdfGen", "PDF created at: ${outputFile.absolutePath}")
            outputFile

        } catch (e: Exception) {
            Log.e("PdfGen", "Error generating PDF", e)
            null
        }
    }

    private fun loadCharacterImage(context: Context, imagePath: String?): Image? {
        if (imagePath.isNullOrBlank()) return null

        return try {
            val bytes = if (imagePath.startsWith("content://")) {
                val uri = imagePath.toUri()
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } else {
                val file = File(imagePath)
                if (file.exists()) {
                    file.readBytes()
                } else {
                    Log.e("PdfGen", "File does not exist at path: $imagePath")
                    null
                }
            }

            if (bytes != null) {
                val imageData = ImageDataFactory.create(bytes)
                val pdfImage = Image(imageData)

                pdfImage.scaleToFit(100f, 100f)
                pdfImage
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("PdfGen", "Failed to load character image: $imagePath", e)
            null
        }
    }

    private fun addSaveCellsToTable(
        ability: Ability,
        char: Character,
        context: Context,
        table: Table
    ) {
        val iconFontBytes = context.assets.open("font/MaterialIcons-Regular.ttf").readBytes()
        val iconFont = PdfFontFactory.createFont(iconFontBytes, PdfEncodings.IDENTITY_H)

        val isProficient = char.savingThrowProficiencies.contains(ability)

        val iconString = if (isProficient) "\uE837" else "\uE836"

        val abilityName = context.getString(ability.nameRes)
        val saveMod = char.getSavingThrowMod(ability).withSign()

        val iconCell = Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER)
        iconCell.add(Paragraph(iconString).setFont(iconFont).setFontSize(12f))
        table.addCell(iconCell)
        table.addCell(Cell().add(Paragraph(abilityName)).setBorder(Border.NO_BORDER))
        table.addCell(
            Cell().add(Paragraph(saveMod).setUnderline()).setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
        )
    }

    private fun addSkillCellsToTable(
        skill: Skill,
        char: Character,
        context: Context,
        table: Table
    ) {
        val iconFontBytes = context.assets.open("font/MaterialIcons-Regular.ttf").readBytes()
        val iconFont = PdfFontFactory.createFont(iconFontBytes, PdfEncodings.IDENTITY_H)

        val profLevel = char.skillProficiencies[skill] ?: ProficiencyLevel.NONE

        val iconString = when {
            profLevel == ProficiencyLevel.EXPERT -> "\uE86C"
            profLevel == ProficiencyLevel.PROFICIENT -> "\uE837"
            profLevel == ProficiencyLevel.HALF -> "\ueb37"
            char.hasJackOfAllTrades && profLevel == ProficiencyLevel.NONE -> "\ueb37"
            else -> "\uE836"
        }

        val skillName = context.getString(skill.nameRes)
        val abilityStr = context.getString(skill.defaultAbility.nameRes).take(3).uppercase()
        val displayName = "$skillName ($abilityStr)"

        val mod = char.getSkillMod(skill).withSign()

        val iconCell = Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER)
        iconCell.add(Paragraph(iconString).setFont(iconFont).setFontSize(12f))
        table.addCell(iconCell)

        table.addCell(Cell().add(Paragraph(displayName)).setBorder(Border.NO_BORDER))

        table.addCell(
            Cell().add(Paragraph(mod).setUnderline()).setTextAlignment(TextAlignment.CENTER)
                .setBorder(Border.NO_BORDER)
        )
    }

    private fun createHeaderCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text).simulateBold().setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(ColorConstants.DARK_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(SolidBorder(ColorConstants.BLACK, 1f))
    }

    private fun createValueCell(text: String): Cell {
        return Cell()
            .add(Paragraph(text))
            .setTextAlignment(TextAlignment.CENTER)
            .setBorder(SolidBorder(ColorConstants.GRAY, 1f))
    }

    private fun Int.withSign(): String {
        return if (this >= 0) "+$this" else this.toString()
    }
}

fun sharePdfIntent(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share Character Sheet"))
}

private fun clearOldPdfs(context: Context) {
    val outputDir = File(context.cacheDir, "pdf")
    if (!outputDir.exists()) return

    val files = outputDir.listFiles() ?: return

    val oneDayInMillis = 24 * 60 * 60 * 1000L
    val cutoffTime = System.currentTimeMillis() - oneDayInMillis

    for (file in files) {
        if (file.lastModified() < cutoffTime) {
            file.delete()
        }
    }
}