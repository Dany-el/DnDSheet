package com.yablonskyi.dndsheet.ui.compendium.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.dndsheet.data.model.rulebook.CharacterClass
import com.yablonskyi.dndsheet.domain.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassCreateViewModel @Inject constructor(
    private val repo: ClassRepository
) : ViewModel() {

    fun createClass(cls: CharacterClass) {
        viewModelScope.launch {
            repo.insert(cls.copy(isHomebrew = true))
        }
    }
}