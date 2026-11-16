package com.example.myapplication.domain.usecase

import com.example.myapplication.FakeGradeRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AddGradeUseCaseTest {

    private val repository = FakeGradeRepository()
    private val addGrade = AddGradeUseCase(repository)

    private fun assertRejected(activity: String, subject: String, score: Double) {
        try {
            runBlocking { addGrade(activity, subject, score) }
            fail("Se esperaba IllegalArgumentException para ($activity, $subject, $score)")
        } catch (e: IllegalArgumentException) {
            // comportamiento esperado: la regla del dominio rechaza el dato
        }
        assertTrue("No debe guardarse ninguna calificación", repository.grades.value.isEmpty())
    }

    @Test
    fun `rechaza campos vacios o con solo espacios`() {
        assertRejected("", "Programación Móvil", 8.0)
        assertRejected("Examen", "   ", 8.0)
    }

    @Test
    fun `rechaza notas fuera del rango de 0 a 10`() {
        assertRejected("Examen", "Programación Móvil", -0.1)
        assertRejected("Examen", "Programación Móvil", 10.1)
        assertRejected("Examen", "Programación Móvil", 11.0)
    }

    @Test
    fun `rechaza valores no numericos como NaN o infinito`() {
        assertRejected("Examen", "Programación Móvil", Double.NaN)
        assertRejected("Examen", "Programación Móvil", Double.POSITIVE_INFINITY)
    }

    @Test
    fun `acepta los limites 0 y 10 y guarda los textos sin espacios sobrantes`() {
        runBlocking {
            addGrade("  Examen final  ", " Programación Móvil ", 0.0)
            addGrade("Proyecto", "Programación Móvil", 10.0)
        }
        val saved = runBlocking { GetGradesUseCase(repository)().first() }
        assertEquals(listOf("Examen final", "Proyecto"), saved.map { it.activityName })
        assertEquals(listOf(0.0, 10.0), saved.map { it.score })
        assertEquals("Programación Móvil", saved.first().subject)
    }
}
