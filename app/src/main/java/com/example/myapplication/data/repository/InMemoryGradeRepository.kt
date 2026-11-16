package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.AcademicGrade
import com.example.myapplication.domain.repository.GradeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class InMemoryGradeRepository : GradeRepository {

    // Simulación de una base de datos local: el flujo mutable es privado y solo se expone de lectura
    private val gradesFlow = MutableStateFlow(
        listOf(
            AcademicGrade(UUID.randomUUID().toString(), "Examen práctico RDA-1", "Programación Móvil", 8.5),
            AcademicGrade(UUID.randomUUID().toString(), "Taller de programación funcional", "Programación Móvil", 9.5),
            AcademicGrade(UUID.randomUUID().toString(), "Prueba de Null Safety", "Programación Móvil", 7.0)
        )
    )

    override fun getGradesStream(): Flow<List<AcademicGrade>> = gradesFlow

    override suspend fun addGrade(activityName: String, subject: String, score: Double) {
        val grade = AcademicGrade(UUID.randomUUID().toString(), activityName, subject, score)
        gradesFlow.value = gradesFlow.value + grade
    }
}
