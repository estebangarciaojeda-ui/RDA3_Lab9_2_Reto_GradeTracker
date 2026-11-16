package com.example.myapplication.ui.presentation.grades

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.AddGradeUseCase
import com.example.myapplication.domain.usecase.GetGradesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "GradeViewModel"

class GradeViewModel(
    private val getGradesUseCase: GetGradesUseCase,
    private val addGradeUseCase: AddGradeUseCase
) : ViewModel() {

    // Estado mutable privado y estado de solo lectura para la vista (flujo unidireccional)
    private val _uiState = MutableStateFlow<GradeUiState>(GradeUiState.Loading)
    val uiState: StateFlow<GradeUiState> = _uiState.asStateFlow()

    private val _currentScreen = MutableStateFlow(GradeScreen.LIST)
    val currentScreen: StateFlow<GradeScreen> = _currentScreen.asStateFlow()

    private val _form = MutableStateFlow(GradeForm())
    val form: StateFlow<GradeForm> = _form.asStateFlow()

    // Última lista recibida, para salir de un estado de error sin perder los datos
    private var lastSuccess: GradeUiState.Success? = null

    init {
        observeGrades()
    }

    private fun observeGrades() {
        viewModelScope.launch {
            try {
                getGradesUseCase().collect { grades ->
                    // Promedio con operaciones funcionales de colección; una lista vacía no tiene promedio
                    val average = if (grades.isEmpty()) 0.0 else grades.map { it.score }.average()
                    val success = GradeUiState.Success(grades, average)
                    lastSuccess = success
                    _uiState.value = success
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar las calificaciones", e)
                _uiState.value = GradeUiState.Error("Error crítico al cargar las calificaciones: ${e.localizedMessage}")
            }
        }
    }

    private fun clearError() {
        val success = lastSuccess ?: return
        if (_uiState.value is GradeUiState.Error) {
            _uiState.value = success
        }
    }

    fun onNavigateToForm() {
        clearError()
        _form.value = GradeForm()
        _currentScreen.value = GradeScreen.FORM
    }

    fun onNavigateToList() {
        clearError()
        _currentScreen.value = GradeScreen.LIST
    }

    fun onActivityNameChange(value: String) {
        clearError()
        _form.value = _form.value.copy(activityName = value)
    }

    fun onSubjectChange(value: String) {
        clearError()
        _form.value = _form.value.copy(subject = value)
    }

    fun onScoreChange(value: String) {
        clearError()
        _form.value = _form.value.copy(score = value)
    }

    fun onSaveGrade() {
        val current = _form.value
        if (!current.isComplete) return

        // Blindaje de tipos: toDoubleOrNull() no lanza excepción y el operador Elvis resuelve el caso nulo
        val score = current.score.trim().replace(',', '.').toDoubleOrNull() ?: run {
            Log.w(TAG, "Nota no numérica rechazada: \"${current.score}\"")
            _uiState.value = GradeUiState.Error("La nota debe ser un número válido, por ejemplo 8.5.")
            return
        }

        viewModelScope.launch {
            try {
                addGradeUseCase(current.activityName, current.subject, score)
                _form.value = GradeForm()
                _currentScreen.value = GradeScreen.LIST
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Regla de dominio rechazada: ${e.message}")
                _uiState.value = GradeUiState.Error(e.message ?: "Dato inválido")
            } catch (e: Exception) {
                Log.e(TAG, "Error inesperado al guardar la calificación", e)
                _uiState.value = GradeUiState.Error("Error al guardar: ${e.localizedMessage}")
            }
        }
    }
}
