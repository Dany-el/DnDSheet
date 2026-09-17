package com.yablonskyi.compendium.classes.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.yablonskyi.compendium.MainDispatcherRule
import com.yablonskyi.compendium.fake.FakeClassRepository
import com.yablonskyi.model.rulebook.CharacterClass
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ClassFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createRoute() = SavedStateHandle(mapOf("classId" to null as String?))

    private fun updateRoute(classId: String) = SavedStateHandle(mapOf("classId" to classId))

    @Test
    fun `create mode form is invalid until required fields are filled`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = ClassFormViewModel(FakeClassRepository(), createRoute())

            assertFalse(vm.uiState.value.isFormValid)

            vm.onIntent(ClassFormIntent.NameChanged("Wizard"))
            vm.onIntent(ClassFormIntent.DescriptionChanged("desc"))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `invalid skill choice count marks form invalid and back to valid`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val vm = ClassFormViewModel(FakeClassRepository(), createRoute())
            vm.onIntent(ClassFormIntent.NameChanged("Wizard"))
            vm.onIntent(ClassFormIntent.DescriptionChanged("desc"))

            vm.onIntent(ClassFormIntent.SkillChoiceCountChanged(99))
            assertFalse(vm.uiState.value.isFormValid)

            vm.onIntent(ClassFormIntent.SkillChoiceCountChanged(2))
            assertTrue(vm.uiState.value.isFormValid)
        }

    @Test
    fun `update mode loads class and form is valid without edits`() =
        runTest(mainDispatcherRule.testDispatcher.scheduler) {
            val existing = CharacterClass(
                id = "c1",
                name = "Fighter",
                hitDice = "d10",
                description = "A master of martial combat."
            )
            val fake = FakeClassRepository().apply { setClasses(listOf(existing)) }
            val vm = ClassFormViewModel(fake, updateRoute("c1"))
            advanceUntilIdle()

            assertEquals("Fighter", vm.uiState.value.name.text)
            assertEquals("d10", vm.uiState.value.hitDice)
            assertTrue(vm.uiState.value.isFormValid)
        }
}