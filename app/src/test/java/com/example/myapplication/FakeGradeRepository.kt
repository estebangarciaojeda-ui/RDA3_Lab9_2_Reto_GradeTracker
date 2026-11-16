package com.example.myapplication

import com.example.myapplication.domain.model.AcademicGrade
import com.example.myapplication.domain.repository.GradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Repositorio falso: demuestra que el dominio y el ViewModel se prueban sin Android ni infraestructura real. */
class FakeGradeRepository(initial: List<AcademicGrade> = emptyList()) : GradeRepository {

    val grades = MutableStateFlow(initial)

    override fun getGradesStream(): Flow<List<AcademicGrade>> = grades

    override suspend fun addGrade(activityName: String, subject: String, score: Double) {
        grades.value = grades.value + AcademicGrade("id-${grades.value.size}", activityName, subject, score)
    }
}

fun grade(id: String, score: Double) = AcademicGrade(id, "Actividad $id", "Programación Móvil", score)
