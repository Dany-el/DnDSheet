package com.yablonskyi.wizard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.domain.repository.ClassRepository
import com.yablonskyi.domain.repository.RaceRepository
import com.yablonskyi.model.character.Ability
import com.yablonskyi.model.character.Character
import com.yablonskyi.model.character.ProficiencyLevel
import com.yablonskyi.model.character.Skill
import com.yablonskyi.model.character.SpellSettings
import com.yablonskyi.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterCreationWizardViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    raceRepository: RaceRepository,
    classRepository: ClassRepository
) : ViewModel() {
    private val internalState = MutableStateFlow(WizardFormState())
    private val effects = Channel<WizardEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    private val raceContent = combine(
        raceRepository.getAllRaces(),
        internalState.map { it.raceQuery }.distinctUntilChanged()
    ) { races, query ->
        races.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .partition { it.isHomebrew }
    }
    private val classContent = combine(
        classRepository.getAllClasses(),
        internalState.map { it.classQuery }.distinctUntilChanged()
    ) { classes, query ->
        classes.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .partition { it.isHomebrew }
    }
    val uiState: StateFlow<WizardUiState> = combine(
        internalState,
        raceContent,
        classContent
    ) { form, races, classes ->
        WizardUiState(
            form,
            races.second,
            races.first,
            classes.second,
            classes.first,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WizardUiState())

    fun onIntent(intent: WizardIntent) {
        if (internalState.value.isSubmitting) return
        when (intent) {
            WizardIntent.Finish -> finish()
            WizardIntent.Close -> sendEffect(WizardEffect.NavigateBack)
            WizardIntent.Back -> {
                val step = internalState.value.step
                if (step == WizardStep.NAME) sendEffect(WizardEffect.NavigateBack)
                else internalState.update { it.copy(step = WizardStep.entries[step.ordinal - 1]) }
            }
            else -> internalState.update { state -> reduce(state, intent) }
        }
    }

    private fun reduce(s: WizardFormState, intent: WizardIntent): WizardFormState = when (intent) {
        is WizardIntent.NameChanged -> s.copy(name = intent.value)
        is WizardIntent.RaceQueryChanged -> s.copy(raceQuery = intent.value)
        is WizardIntent.ClassQueryChanged -> s.copy(classQuery = intent.value)
        is WizardIntent.RaceSelected -> s.copy(selectedRace = intent.race)
        is WizardIntent.ClassSelected -> s.copy(selectedClass = intent.characterClass, selectedSkills = emptySet())
        is WizardIntent.SkillToggled -> s.copy(selectedSkills = when {
            intent.skill in s.selectedSkills -> s.selectedSkills - intent.skill
            intent.skill in s.availableSkills && s.selectedSkills.size < s.maxSkills -> s.selectedSkills + intent.skill
            else -> s.selectedSkills
        })
        is WizardIntent.AbilityMethodChanged -> if (s.abilityMethod == intent.method) s else s.copy(
            abilityMethod = intent.method,
            pendingPoolValue = null,
            pendingRollIndex = null,
        )
        is WizardIntent.PoolValueSelected -> s.copy(pendingPoolValue = if (s.pendingPoolValue == intent.value) null else intent.value)
        is WizardIntent.AbilityAssigned -> when (s.abilityMethod) {
            AbilityMethod.STANDARD_ARRAY -> s.pendingPoolValue?.let { s.copy(standardAssignments = s.standardAssignments + (intent.ability to it), pendingPoolValue = null) } ?: s
            AbilityMethod.ROLL -> s.pendingRollIndex?.let { s.copy(rollIndexAssignments = s.rollIndexAssignments + (intent.ability to it), pendingRollIndex = null) } ?: s
            AbilityMethod.POINT_BUY -> s
        }
        is WizardIntent.AbilityUnassigned -> when (s.abilityMethod) {
            AbilityMethod.STANDARD_ARRAY -> s.copy(standardAssignments = s.standardAssignments - intent.ability)
            AbilityMethod.ROLL -> s.copy(rollIndexAssignments = s.rollIndexAssignments - intent.ability)
            AbilityMethod.POINT_BUY -> s
        }
        is WizardIntent.PointBuyIncremented -> changePointBuy(s, intent.ability, 1)
        is WizardIntent.PointBuyDecremented -> changePointBuy(s, intent.ability, -1)
        WizardIntent.RollAll -> s.copy(rolledResults = List(6) { List(4) { (1..6).random() }.sortedDescending().take(3).sum() }, pendingRollIndex = null)
        is WizardIntent.RollIndexSelected -> s.copy(pendingRollIndex = if (s.pendingRollIndex == intent.index) null else intent.index)
        is WizardIntent.LevelChanged -> s.copy(level = intent.value.coerceIn(1, 20))
        WizardIntent.Next -> if (s.canProceed && s.step != WizardStep.LEVEL) s.copy(step = WizardStep.entries[s.step.ordinal + 1]) else s
        WizardIntent.Back, WizardIntent.Close, WizardIntent.Finish -> s
    }

    private fun changePointBuy(s: WizardFormState, ability: Ability, delta: Int): WizardFormState {
        val current = s.pointBuyScores[ability] ?: return s
        val next = current + delta
        val cost = WizardAbilityRules.POINT_BUY_COSTS[next] ?: return s
        if (s.pointsSpent - WizardAbilityRules.POINT_BUY_COSTS.getValue(current) + cost > WizardAbilityRules.POINT_BUY_BUDGET) return s
        return s.copy(pointBuyScores = s.pointBuyScores + (ability to next))
    }

    private fun sendEffect(effect: WizardEffect) { viewModelScope.launch { effects.send(effect) } }

    private fun finish() {
        val snapshot = internalState.value
        if (snapshot.step != WizardStep.LEVEL || !WizardStep.entries.all { snapshot.isValid(it) }) return
        val race = snapshot.selectedRace ?: return
        val cls = snapshot.selectedClass ?: return
        val base = snapshot.baseAbilityBlock
        val lvl = snapshot.level
        val finalAbilities = race.abilityBonuses.entries
            .fold(base) { block, (ability, bonus) ->
                block.update(ability, block.getScore(ability) + bonus)
            }

        val skillProfMap: Map<Skill, ProficiencyLevel> = buildMap {
            race.grantedSkills.forEach { put(it, ProficiencyLevel.PROFICIENT) }
            snapshot.selectedSkills.forEach { put(it, ProficiencyLevel.PROFICIENT) }
        }

        // HP: level 1 = max hit die + CON mod; each additional level = avg + CON mod
        val sides = cls.hitDice.drop(1).toIntOrNull() ?: 8
        val conMod = finalAbilities.getModifier(Ability.CON)
        val hp = (sides + conMod) + (((sides / 2) + 1 + conMod) * (lvl - 1))

        val character = Character(
            name = snapshot.name.trim(),
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

        internalState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                val id = characterRepository.insertCharacter(character)
                effects.send(WizardEffect.CharacterCreated(id))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                internalState.update { it.copy(isSubmitting = false) }
                effects.send(WizardEffect.ShowSnackbar(R.string.wizard_creation_failed))
            }
        }
    }
}