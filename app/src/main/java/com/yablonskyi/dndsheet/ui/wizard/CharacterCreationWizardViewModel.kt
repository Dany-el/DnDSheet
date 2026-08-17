package com.yablonskyi.dndsheet.ui.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.model.character.Ability
import com.yablonskyi.dndsheet.data.model.character.AbilityBlock
import com.yablonskyi.dndsheet.data.model.character.Character
import com.yablonskyi.dndsheet.data.model.character.ProficiencyLevel
import com.yablonskyi.dndsheet.data.model.character.Skill
import com.yablonskyi.dndsheet.data.model.character.SpellSettings
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.data.model.rulebook.Race
import com.yablonskyi.dndsheet.domain.repository.CharacterRepository
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
import com.yablonskyi.dndsheet.domain.repository.RaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class CharacterCreationWizardViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    raceRepository: RaceRepository,
    classRepository: ClassRepository,
) : ViewModel() {

    // ── Step state ──────────────────────────────────────────────
    private val _step = MutableStateFlow(WizardStep.NAME)
    val step = _step.asStateFlow()

    // Ability method
    private val _abilityMethod = MutableStateFlow(AbilityMethod.STANDARD_ARRAY)
    val abilityMethod = _abilityMethod.asStateFlow()

    private val _standardAssignments = MutableStateFlow<Map<Ability, Int>>(emptyMap())
    val standardAssignments = _standardAssignments.asStateFlow()

    private val _pendingPoolValue = MutableStateFlow<Int?>(null)
    val pendingPoolValue = _pendingPoolValue.asStateFlow()

    companion object {
        val POINT_BUY_COSTS = mapOf(
            8 to 0, 9 to 1, 10 to 2, 11 to 3,
            12 to 4, 13 to 5, 14 to 7, 15 to 9
        )
        const val POINT_BUY_BUDGET = 27
        val CORE_ABILITIES = Ability.entries.filter { it != Ability.NONE }
    }

    private val _pointBuyScores = MutableStateFlow(
        CORE_ABILITIES.associateWith { 8 }
    )
    val pointBuyScores = _pointBuyScores.asStateFlow()

    val pointsSpent: StateFlow<Int> = _pointBuyScores.map { scores ->
        scores.values.sumOf { POINT_BUY_COSTS[it] ?: 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _rolledResults = MutableStateFlow<List<Int>>(emptyList())
    val rolledResults = _rolledResults.asStateFlow()

    private val _pendingRollIndex = MutableStateFlow<Int?>(null)
    val pendingRollIndex = _pendingRollIndex.asStateFlow()

    private val _rollIndexAssignments = MutableStateFlow<Map<Ability, Int>>(emptyMap())
    val rollIndexAssignments = _rollIndexAssignments.asStateFlow()

    val baseAbilityBlock: StateFlow<AbilityBlock> = combine(
        _abilityMethod, _standardAssignments, _pointBuyScores, _rollIndexAssignments, _rolledResults
    ) { method, stdMap, pbMap, rollIndexMap, rolled ->
        val scores: Map<Ability, Int> = when (method) {
            AbilityMethod.STANDARD_ARRAY ->
                CORE_ABILITIES.associateWith { stdMap[it] ?: 8 }

            AbilityMethod.POINT_BUY -> pbMap
            AbilityMethod.ROLL -> {
                CORE_ABILITIES.associateWith { ability ->
                    val index = rollIndexMap[ability]
                    if (index != null) rolled.getOrNull(index) ?: 8 else 8
                }
            }
        }
        scores.entries.fold(AbilityBlock()) { block, (ab, v) -> block.update(ab, v) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AbilityBlock())

    // ── User selections ─────────────────────────────────────────
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _selectedRace = MutableStateFlow<Race?>(null)
    val selectedRace = _selectedRace.asStateFlow()

    private val _selectedClass = MutableStateFlow<CharacterClass?>(null)
    val selectedClass = _selectedClass.asStateFlow()

    private val _selectedSkills = MutableStateFlow<Set<Skill>>(emptySet())
    val selectedSkills = _selectedSkills.asStateFlow()

    // ── Homebrew search queries ───────────────────────────────────────
    private val _raceQuery = MutableStateFlow("")
    val raceQuery = _raceQuery.asStateFlow()

    private val _classQuery = MutableStateFlow("")
    val classQuery = _classQuery.asStateFlow()

    // ── Data from DB — split into original and homebrew ───────────────
    private val _allRaces = raceRepository.getAllRaces()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allClasses = classRepository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val origRaces: StateFlow<List<Race>> = combine(_allRaces, _raceQuery) { races, query ->
        races.filter { !it.isHomebrew }
            .let {
                if (query.isBlank()) it else it.filter { r ->
                    r.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homebrewRaces: StateFlow<List<Race>> = combine(_allRaces, _raceQuery) { races, query ->
        races.filter { it.isHomebrew }
            .let {
                if (query.isBlank()) it else it.filter { r ->
                    r.name.contains(
                        query,
                        ignoreCase = true
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val origClasses: StateFlow<List<CharacterClass>> =
        combine(_allClasses, _classQuery) { classes, query ->
            classes.filter { !it.isHomebrew }
                .let {
                    if (query.isBlank()) it else it.filter { c ->
                        c.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val homebrewClasses: StateFlow<List<CharacterClass>> =
        combine(_allClasses, _classQuery) { classes, query ->
            classes.filter { it.isHomebrew }
                .let {
                    if (query.isBlank()) it else it.filter { c ->
                        c.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRaceQuery(query: String) {
        _raceQuery.value = query
    }

    fun setClassQuery(query: String) {
        _classQuery.value = query
    }

    // ── Derived: available skills for the chosen class ───────────
    val availableSkills: StateFlow<List<Skill>> = _selectedClass.map { cls ->
        cls?.availableSkills?.ifEmpty { Skill.entries.toList() } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maxSkills: StateFlow<Int> = _selectedClass.map { it?.skillChoiceCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ── Ability method switch ─────────────────────────────────────────────────────
    fun setAbilityMethod(method: AbilityMethod) {
        _abilityMethod.value = method
        _standardAssignments.value = emptyMap()
        _pendingPoolValue.value = null
        _pendingRollIndex.value = null
        _rolledResults.value = emptyList()
    }

    // ── Standard Array ────────────────────────────────────────────────────────────

    fun selectPoolValue(value: Int) {
        _pendingPoolValue.value = if (_pendingPoolValue.value == value) null else value
    }

    fun assignValueToAbility(ability: Ability) {
        when (_abilityMethod.value) {
            AbilityMethod.STANDARD_ARRAY -> {
                assignPoolValueToAbility(ability)
            }

            AbilityMethod.POINT_BUY -> {

            }

            AbilityMethod.ROLL -> {
                assignRollToAbility(ability)
            }
        }
    }

    fun unassignAbility(ability: Ability) {
        when (_abilityMethod.value) {
            AbilityMethod.STANDARD_ARRAY -> {
                _standardAssignments.value -= ability
            }

            AbilityMethod.POINT_BUY -> {

            }

            AbilityMethod.ROLL -> {
                _rollIndexAssignments.value -= ability
            }
        }
    }

    private fun assignPoolValueToAbility(ability: Ability) {
        val value = _pendingPoolValue.value ?: return
        val current = _standardAssignments.value.toMutableMap()
        current[ability] = value
        _standardAssignments.value = current
        _pendingPoolValue.value = null
    }

    // ── Point Buy ─────────────────────────────────────────────────────────────────
    fun incrementPointBuy(ability: Ability) {
        val scores = _pointBuyScores.value
        val current = scores[ability] ?: 8
        if (current >= 15) return
        val nextCost = POINT_BUY_COSTS[current + 1] ?: return
        val currentCost = POINT_BUY_COSTS[current] ?: return
        val spent = scores.values.sumOf { POINT_BUY_COSTS[it] ?: 0 }
        if (spent + (nextCost - currentCost) > POINT_BUY_BUDGET) return
        _pointBuyScores.value = scores + (ability to current + 1)
    }

    fun decrementPointBuy(ability: Ability) {
        val scores = _pointBuyScores.value
        val current = scores[ability] ?: 8
        if (current <= 8) return
        _pointBuyScores.value = scores + (ability to current - 1)
    }

    // ── Roll ──────────────────────────────────────────────────────────────────────
    fun rollAllAbilities() {
        _rolledResults.value = List(6) { rollFourDropLowest() }
        _pendingRollIndex.value = null
    }

    fun selectRollIndex(index: Int) {
        _pendingRollIndex.value = if (_pendingRollIndex.value == index) null else index
    }

    private fun assignRollToAbility(ability: Ability) {
        val index = _pendingRollIndex.value ?: return

        _rollIndexAssignments.value += (ability to index)
        _pendingRollIndex.value = null
    }

    private fun rollFourDropLowest(): Int {
        val rolls = List(4) { (1..6).random() }
        return rolls.asSequence().sortedDescending().take(3).sum()
    }

    // ── Level ─────────────────────────────────────────────────────────────────────
    private val _level = MutableStateFlow(1)
    val level = _level.asStateFlow()

    // ── Derived: calculated HP ─────────────────────────────
    val calculatedHp: StateFlow<Int> = combine(
        _selectedClass, baseAbilityBlock, _selectedRace, _level
    ) { cls, base, race, lvl ->
        if (cls == null) return@combine 0
        val sides = cls.hitDice.removePrefix("d").removePrefix("к").toIntOrNull() ?: 8
        val racialCon = race?.abilityBonuses?.get(Ability.CON) ?: 0
        val conMod = floor((base.constitution + racialCon - 10) / 2.0).toInt()
        val level1Hp = sides + conMod
        val avgPerLevel = (sides / 2) + 1 + conMod
        maxOf(lvl, 1).let { level1Hp + (avgPerLevel * (it - 1)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setLevel(value: Int) {
        _level.value = value.coerceIn(1, 20)
    }

    // ── Events ───────────────────────────────────────────────────
    private val _createdId = MutableSharedFlow<Long>()
    val createdId = _createdId.asSharedFlow()

    // ── Validation ───────────────────────────────────────────────
    val canProceed: StateFlow<Boolean> = combine(
        _step, _name, _selectedRace, _selectedClass, _selectedSkills,
        _abilityMethod, _standardAssignments, _rolledResults, _rollIndexAssignments
    ) { args ->
        val step = args[0] as WizardStep
        val name = args[1] as String
        val race = args[2] as Race?
        val cls = args[3] as CharacterClass?
        val skills = args[4] as Set<*>
        val method = args[5] as AbilityMethod
        val stdAssign = args[6] as Map<*, *>
        val rolled = args[7] as List<*>
        val rollAssign = args[8] as Map<*, *>
        when (step) {
            WizardStep.NAME -> name.isNotBlank()
            WizardStep.RACE -> race != null
            WizardStep.ABILITIES -> when (method) {
                AbilityMethod.STANDARD_ARRAY -> stdAssign.size == 6
                AbilityMethod.POINT_BUY -> true
                AbilityMethod.ROLL -> rolled.size == 6 && rollAssign.size == 6
            }

            WizardStep.CLASS -> cls != null
            WizardStep.SKILLS -> skills.size == (cls?.skillChoiceCount ?: 0)
            WizardStep.LEVEL -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ── Actions ──────────────────────────────────────────────────

    fun setName(value: String) {
        _name.value = value
    }

    fun selectRace(race: Race) {
        _selectedRace.value = race
    }

    fun selectClass(cls: CharacterClass) {
        _selectedClass.value = cls
        _selectedSkills.value = emptySet()
    }

    fun toggleSkill(skill: Skill) {
        val current = _selectedSkills.value
        val max = _selectedClass.value?.skillChoiceCount ?: 0
        _selectedSkills.value = when {
            current.contains(skill) -> current - skill
            current.size < max -> current + skill
            else -> current
        }
    }

    fun goNext() {
        _step.value = when (_step.value) {
            WizardStep.NAME -> WizardStep.RACE
            WizardStep.RACE -> WizardStep.ABILITIES
            WizardStep.ABILITIES -> WizardStep.CLASS
            WizardStep.CLASS -> WizardStep.SKILLS
            WizardStep.SKILLS -> WizardStep.LEVEL
            WizardStep.LEVEL -> return
        }
    }

    fun goBack(): Boolean {
        _step.value = when (_step.value) {
            WizardStep.NAME -> return false
            WizardStep.RACE -> WizardStep.NAME
            WizardStep.ABILITIES -> WizardStep.RACE
            WizardStep.CLASS -> WizardStep.ABILITIES
            WizardStep.SKILLS -> WizardStep.CLASS
            WizardStep.LEVEL -> WizardStep.SKILLS
        }
        return true
    }

    fun finish() {
        val race = _selectedRace.value ?: return
        val cls = _selectedClass.value ?: return
        val base = baseAbilityBlock.value
        val lvl = _level.value

        val finalAbilities = race.abilityBonuses.entries
            .fold(base) { block, (ability, bonus) ->
                block.update(ability, block.getScore(ability) + bonus)
            }

        val skillProfMap: Map<Skill, ProficiencyLevel> = buildMap {
            race.grantedSkills.forEach { put(it, ProficiencyLevel.PROFICIENT) }
            _selectedSkills.value.forEach { put(it, ProficiencyLevel.PROFICIENT) }
        }

        // HP: level 1 = max hit die + CON mod; each additional level = avg + CON mod
        val sides = cls.hitDice.drop(1).toIntOrNull() ?: 8
        val conMod = finalAbilities.getModifier(Ability.CON)
        val hp = (sides + conMod) + (((sides / 2) + 1 + conMod) * (lvl - 1))

        val character = Character(
            name = _name.value.trim(),
            race = race.name,
            charClass = cls.name,
            level = lvl,
            speed = race.speed,
            hitDice = cls.hitDice,
            maxHp = maxOf(1, hp),
            currentHp = maxOf(1, hp),
            abilityBlock = finalAbilities,
            skillProficiencies = skillProfMap,
            savingThrowProficiencies = cls.savingThrows,
            spellSettings = if (cls.spellcastingAbility != null)
                SpellSettings(spellCastingAbility = cls.spellcastingAbility)
            else SpellSettings(),
        )

        viewModelScope.launch {
            val id = characterRepository.insertCharacter(character)
            _createdId.emit(id)
        }
    }
}

enum class WizardStep { NAME, RACE, ABILITIES, CLASS, SKILLS, LEVEL }

enum class AbilityMethod { STANDARD_ARRAY, POINT_BUY, ROLL }