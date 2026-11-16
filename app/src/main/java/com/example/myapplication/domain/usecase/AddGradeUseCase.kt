package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.GradeRepository

class AddGradeUseCase(private val repository: GradeRepository) {

    suspend operator fun invoke(activityName: String, subject: String, score: Double) {
        if (activityName.isBlank() || subject.isBlank()) {
            throw IllegalArgumentException("La actividad y la asignatura no pueden estar vacías.")
        }
        // La negación de "in" también rechaza NaN, que un par de comparaciones dejaría pasar
        if (score !in MIN_SCORE..MAX_SCORE) {
            throw IllegalArgumentException("La nota debe estar entre $MIN_SCORE y $MAX_SCORE.")
        }
        repository.addGrade(activityName.trim(), subject.trim(), score)
    }

    companion object {
        const val MIN_SCORE = 0.0
        const val MAX_SCORE = 10.0
    }
}
