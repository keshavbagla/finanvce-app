package com.example.finance.feature.transaction


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finance.data.dataStore.StreakPreferences
import com.example.finance.domain.model.*
import com.example.finance.domain.repository.ITransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val selectedFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val showAddSheet: Boolean = false,
    val editingTransaction: Transaction? = null
)

enum class TransactionFilter { ALL, INCOME, EXPENSE }

sealed class TransactionEvent {
    data class Add(val txn: Transaction) : TransactionEvent()
    data class Update(val txn: Transaction) : TransactionEvent()
    data class Delete(val txn: Transaction) : TransactionEvent()
    data class SetFilter(val filter: TransactionFilter) : TransactionEvent()
    data class SetSearch(val query: String) : TransactionEvent()
    data class ShowAddSheet(val show: Boolean) : TransactionEvent()
    data class StartEdit(val txn: Transaction) : TransactionEvent()
}

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val txnRepo: ITransactionRepository,
    private val streak: StreakPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionUiState())
    val state: StateFlow<TransactionUiState> = _state.asStateFlow()

    private val allTransactions = txnRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            combine(
                allTransactions,
                _state.map { it.selectedFilter to it.searchQuery }) { txns, (filter, query) ->
                val filtered = txns
                    .filter { txn ->
                        when (filter) {
                            TransactionFilter.ALL -> true
                            TransactionFilter.INCOME -> false
                            TransactionFilter.EXPENSE -> false
                        }
                    }
                    .filter { txn ->
                        query.isBlank() ||
                                txn.title.contains(query, ignoreCase = true) ||
                                txn.category.label.contains(query, ignoreCase = true)
                    }
                filtered
            }.collect { filtered ->
                _state.update { it.copy(transactions = filtered, isLoading = false) }
            }
        }
    }

    fun onEvent(event: TransactionEvent) {
        when (event) {
            is TransactionEvent.Add -> viewModelScope.launch {
                txnRepo.upsert(event.txn)
                streak.recordActivity()   // Log = streak progress
                _state.update { it.copy(showAddSheet = false) }
            }

            is TransactionEvent.Update -> viewModelScope.launch {
                txnRepo.upsert(event.txn)
                _state.update { it.copy(editingTransaction = null, showAddSheet = false) }
            }

            is TransactionEvent.Delete -> viewModelScope.launch {
                txnRepo.delete(event.txn)
            }

            is TransactionEvent.SetFilter -> _state.update { it.copy(selectedFilter = event.filter) }
            is TransactionEvent.SetSearch -> _state.update { it.copy(searchQuery = event.query) }
            is TransactionEvent.ShowAddSheet -> _state.update {
                it.copy(
                    showAddSheet = event.show,
                    editingTransaction = null
                )
            }

            is TransactionEvent.StartEdit -> _state.update {
                it.copy(
                    editingTransaction = event.txn,
                    showAddSheet = true
                )
            }
        }
    }

}