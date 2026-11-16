package com.example.myapplication.ui.presentation.grades

import com.example.myapplication.domain.model.AcademicGrade

sealed class GradeUiState {
    object Loading : GradeUiState()
    data class Success(val grades: List<AcademicGrade>, val average: Double) : GradeUiState()
    data class Error(val message: String) : GradeUiState()
}

/** Enrutamiento interno entre las dos pantallas locales. */
enum class GradeScreen {
    LIST, FORM
}

/** Campos del formulario. La nota se mantiene como texto hasta que se valida. */
data class GradeForm(
    val activityName: String = "",
    val subject: String = "",
    val score: String = ""
) {
    val isComplete: Boolean
        get() = activityName.isNotBlank() && subject.isNotBlank() && score.isNotBlank()
}
