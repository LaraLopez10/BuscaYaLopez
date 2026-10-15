package com.example.a2buscaya.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a2buscaya.data.model.FarmaciaTurnoModel
import com.example.a2buscaya.repository.EmptyFarmaciasException
import com.example.a2buscaya.repository.FarmaciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FarmaciaUiState {
    data object Loading : FarmaciaUiState
    data class Success(val farmacias: List<FarmaciaTurnoModel>) : FarmaciaUiState
    data object Empty : FarmaciaUiState
    data class Error(val message: String) : FarmaciaUiState
}

class FarmaciaViewModel(
    private val repository: FarmaciaRepository = FarmaciaRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<FarmaciaUiState>(FarmaciaUiState.Loading)
    val uiState: StateFlow<FarmaciaUiState> = _uiState.asStateFlow()

    init {
        loadFarmacias()
    }

    fun loadFarmacias() {
        viewModelScope.launch {
            _uiState.value = FarmaciaUiState.Loading
            repository.getFarmaciasTurno()
                .onSuccess { farmacias ->
                    _uiState.value = FarmaciaUiState.Success(farmacias)
                }
                .onFailure { error ->
                    _uiState.value = when (error) {
                        is EmptyFarmaciasException -> FarmaciaUiState.Empty
                        else -> FarmaciaUiState.Error(
                            error.localizedMessage ?: "Error de conexion. Verifica tu internet.",
                        )
                    }
                }
        }
    }
}
