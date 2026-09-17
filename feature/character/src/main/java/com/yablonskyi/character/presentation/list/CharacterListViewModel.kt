package com.yablonskyi.character.presentation.list

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yablonskyi.character.platform.print.CharacterPrintCoordinator
import com.yablonskyi.character.presentation.common.CharacterTransitionCache
import com.yablonskyi.domain.CharacterSheetHtmlRenderer
import com.yablonskyi.domain.repository.CharacterRepository
import com.yablonskyi.domain.repository.CharacterFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val repository: CharacterRepository,
    htmlRenderer: CharacterSheetHtmlRenderer,
    private val files: CharacterFileRepository,
    private val transitionCache: CharacterTransitionCache,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CharacterListState(
        searchQuery = savedStateHandle["query"] ?: "",
    ))
    val state = mutableState.asStateFlow()
    private val effectChannel = Channel<CharacterListEffect>(Channel.BUFFERED, onUndeliveredElement = { effect ->
        if (effect is CharacterListEffect.Print && effect.effect is com.yablonskyi.character.platform.print.CharacterPrintEffect.LaunchPrint) {
            printCoordinator.onPrintLaunchCancelled(effect.effect.requestId)
        }
    })
    val effects = effectChannel.receiveAsFlow()
    private val printCoordinator = CharacterPrintCoordinator(repository, htmlRenderer, viewModelScope)
    private var observation: Job? = null
    private var exportIds = emptyList<Long>()
    private var retryOperation: (() -> Unit)? = null
    private val reorderMutex = Mutex()
    private var orderRevision = 0L
    private var failedOrder: List<Long>? = null

    init {
        observeCharacters()
        viewModelScope.launch { printCoordinator.printState.collect { reduce(CharacterListMutation.Print(it)) } }
        viewModelScope.launch { printCoordinator.printEffects.collect { effectChannel.send(CharacterListEffect.Print(it)) } }
    }

    fun claimPrintRequest(requestId: Long): Boolean = printCoordinator.claimPrintRequest(requestId)

    fun onIntent(intent: CharacterListIntent) {
        when (intent) {
            CharacterListIntent.ReorderFinished -> saveCharacterOrder()
            is CharacterListIntent.MoveCharacter -> moveCharacter(intent.fromId, intent.toId)
            CharacterListIntent.EnterSelectionMode -> reduce(CharacterListMutation.EnterSelectionMode)
            is CharacterListIntent.SearchChanged -> {
                savedStateHandle["query"] = intent.query
                reduce(CharacterListMutation.Search(intent.query))
            }
            is CharacterListIntent.SelectionToggled -> reduce(CharacterListMutation.ToggleSelection(intent.id))
            CharacterListIntent.SelectAllClicked -> reduce(CharacterListMutation.SelectAll)
            CharacterListIntent.ClearSelection -> reduce(CharacterListMutation.ClearSelection)
            is CharacterListIntent.CharacterClicked -> {
                state.value.allCharacters
                    .firstOrNull { it.id == intent.id }
                    ?.let(transitionCache::put)

                emit(CharacterListEffect.OpenCharacter(intent.id))
            }
            CharacterListIntent.CreateClicked -> emit(CharacterListEffect.CreateCharacter)
            CharacterListIntent.ToggleListView -> emit(CharacterListEffect.ToggleListView)
            is CharacterListIntent.DeleteConfirmed -> delete(setOf(intent.id))
            CharacterListIntent.DeleteSelectedConfirmed -> delete(state.value.selectedIds)
            CharacterListIntent.ImportClicked -> if (idle()) {
                reduce(CharacterListMutation.Operation(CharacterListOperation.IMPORT_PICKER))
                emit(CharacterListEffect.LaunchImport)
            }
            is CharacterListIntent.ImportDocumentSelected -> {
                if (state.value.operation != CharacterListOperation.IMPORT_PICKER) return
                if (intent.uri == null) reduce(CharacterListMutation.Operation(null))
                else runOperation(CharacterListOperation.IMPORT, CharacterListError.IMPORT) {
                    val sheets = files.read(intent.uri)
                    if (sheets.isEmpty()) reduce(CharacterListMutation.Failed(CharacterListError.EMPTY_IMPORT))
                    else reduce(CharacterListMutation.ImportRead(sheets))
                }
            }
            CharacterListIntent.ImportConfirmed -> {
                val sheets = state.value.pendingImport ?: return
                if (!idle()) return
                runOperation(CharacterListOperation.IMPORT, CharacterListError.IMPORT) {
                    files.import(sheets)
                    reduce(CharacterListMutation.ImportRead(null))
                    emit(CharacterListEffect.ImportSucceeded)
                }
            }
            CharacterListIntent.ImportDismissed -> if (idle()) reduce(CharacterListMutation.ImportRead(null))
            CharacterListIntent.ExportClicked -> if (idle()) {
                exportIds = state.value.selectedIds.toList()
                if (exportIds.isEmpty()) reduce(CharacterListMutation.Failed(CharacterListError.EMPTY_SELECTION))
                else {
                    reduce(CharacterListMutation.Operation(CharacterListOperation.EXPORT_PICKER))
                    emit(CharacterListEffect.LaunchExport)
                }
            }
            is CharacterListIntent.ExportDocumentSelected -> {
                if (state.value.operation != CharacterListOperation.EXPORT_PICKER) return
                if (intent.uri == null) reduce(CharacterListMutation.Operation(null))
                else {
                    val capturedIds = exportIds
                    runOperation(CharacterListOperation.EXPORT, CharacterListError.EXPORT) {
                        files.export(intent.uri, capturedIds)
                        reduce(CharacterListMutation.ClearSelection)
                        emit(CharacterListEffect.ExportSucceeded)
                    }
                }
            }
            is CharacterListIntent.PrintClicked -> printCoordinator.prepareCharacterSheetPrint(intent.id, intent.language)
            is CharacterListIntent.PrintFinished -> printCoordinator.onPrintLaunchResult(intent.requestId, intent.result)
            is CharacterListIntent.PrintCancelled -> printCoordinator.onPrintLaunchCancelled(intent.requestId)
            CharacterListIntent.DismissError -> reduce(CharacterListMutation.Failed(null))
            CharacterListIntent.Retry -> if (idle()) {
                when (state.value.error) {
                    CharacterListError.LOAD -> observeCharacters()
                    CharacterListError.REORDER -> failedOrder?.let { order ->
                        mutableState.update { it.copy(characterOrder = order, error = null) }
                        orderRevision++
                        saveCharacterOrder()
                    }
                    else -> retryOperation?.invoke()
                }
            }
        }
    }

    fun moveCharacter(fromId: Long, toId: Long) {
        val previousOrder = state.value.characterOrder
        reduce(CharacterListMutation.MoveCharacter(fromId, toId))
        if (previousOrder != state.value.characterOrder) orderRevision++
    }

    fun saveCharacterOrder() {
        val order = state.value.characterOrder.toList()
        if (order.isEmpty()) return
        val revision = orderRevision
        viewModelScope.launch {
            reorderMutex.withLock {
                if (revision != orderRevision) return@withLock
                try {
                    repository.reorderCharacters(order)
                    val persisted = repository.getAllCharacters().first()
                    reduce(CharacterListMutation.Loaded(persisted))
                    if (revision == orderRevision) {
                        failedOrder = null
                        mutableState.update {
                            it.copy(characterOrder = emptyList(), error = it.error.takeUnless { error -> error == CharacterListError.REORDER })
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    if (revision == orderRevision) {
                        failedOrder = order
                        mutableState.update {
                            it.copy(characterOrder = emptyList(), error = CharacterListError.REORDER)
                        }
                    }
                }
            }
        }
    }

    private fun idle() = state.value.operation == null

    private fun observeCharacters() {
        observation?.cancel()
        observation = viewModelScope.launch {
            try {
                repository.getAllCharacters().collect { reduce(CharacterListMutation.Loaded(it)) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { reduce(CharacterListMutation.Failed(CharacterListError.LOAD)) }
        }
    }

    private fun delete(ids: Set<Long>) {
        if (!idle() || ids.isEmpty()) return
        runOperation(CharacterListOperation.DELETE, CharacterListError.DELETE) {
            val characters = state.value.allCharacters.filter { it.id in ids }
            repository.deleteCharacters(characters)
            reduce(CharacterListMutation.ClearSelection)
        }
    }

    private fun runOperation(operation: CharacterListOperation, error: CharacterListError, block: suspend () -> Unit) {
        retryOperation = { runOperation(operation, error, block) }
        reduce(CharacterListMutation.Operation(operation))
        viewModelScope.launch {
            try {
                block()
                if (state.value.operation == operation) reduce(CharacterListMutation.Operation(null))
                retryOperation = null
            } catch (cancelled: CancellationException) {
                reduce(CharacterListMutation.Operation(null))
                throw cancelled
            } catch (_: Exception) { reduce(CharacterListMutation.Failed(error)) }
        }
    }

    private fun emit(effect: CharacterListEffect) { viewModelScope.launch { effectChannel.send(effect) } }
    private fun reduce(mutation: CharacterListMutation) { mutableState.update { reduceCharacterList(it, mutation) } }
}