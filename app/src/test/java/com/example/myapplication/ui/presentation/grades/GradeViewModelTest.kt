package com.example.myapplication.ui.presentation.grades

import com.example.myapplication.FakeGradeRepository
import com.example.myapplication.domain.usecase.AddGradeUseCase
import com.example.myapplication.domain.usecase.GetGradesUseCase
import com.example.myapplication.grade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GradeViewModelTest {

    private val repository = FakeGradeRepository(listOf(grade("1", 8.5), grade("2", 9.5), grade("3", 7.0)))
    private lateinit var viewModel: GradeViewModel

    @Before
    fun setUp() {
        // viewModelScope usa Dispatchers.Main, que no existe en una prueba de JVM
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = GradeViewModel(GetGradesUseCase(repository), AddGradeUseCase(repository))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillForm(activity: String, subject: String, score: String) {
        viewModel.onNavigateToForm()
        viewModel.onActivityNameChange(activity)
        viewModel.onSubjectChange(subject)
        viewModel.onScoreChange(score)
    }

    private fun success() = viewModel.uiState.value as GradeUiState.Success

    @Test
    fun `el estado inicial calcula el promedio de las calificaciones semilla`() {
        assertEquals(3, success().grades.size)
        assertEquals((8.5 + 9.5 + 7.0) / 3, success().average, 1e-9)
    }

    @Test
    fun `una lista vacia tiene promedio cero y no NaN`() {
        repository.grades.value = emptyList()
        assertEquals(0.0, success().average, 0.0)
    }

    @Test
    fun `registrar una nota valida actualiza el promedio y vuelve al listado`() {
        fillForm("Laboratorio", "Programación Móvil", "9")
        viewModel.onSaveGrade()

        assertEquals(GradeScreen.LIST, viewModel.currentScreen.value)
        assertEquals(4, success().grades.size)
        assertEquals((8.5 + 9.5 + 7.0 + 9.0) / 4, success().average, 1e-9)
        assertEquals(GradeForm(), viewModel.form.value)
    }

    @Test
    fun `una nota alfabetica produce un estado de error y no se guarda`() {
        fillForm("Laboratorio", "Programación Móvil", "abc")
        viewModel.onSaveGrade()

        assertTrue(viewModel.uiState.value is GradeUiState.Error)
        assertEquals(GradeScreen.FORM, viewModel.currentScreen.value)
        assertEquals(3, repository.grades.value.size)
    }

    @Test
    fun `una nota fuera de rango produce el error del dominio`() {
        fillForm("Laboratorio", "Programación Móvil", "11")
        viewModel.onSaveGrade()

        val error = viewModel.uiState.value as GradeUiState.Error
        assertEquals("La nota debe estar entre 0.0 y 10.0.", error.message)
        assertEquals(3, repository.grades.value.size)
    }

    @Test
    fun `el texto NaN se convierte a numero pero el dominio lo rechaza`() {
        // "NaN".toDoubleOrNull() no devuelve null: es un Double valido, por eso la validacion del rango es necesaria
        assertTrue("NaN".toDoubleOrNull()?.isNaN() == true)

        fillForm("Laboratorio", "Programación Móvil", "NaN")
        viewModel.onSaveGrade()

        assertTrue(viewModel.uiState.value is GradeUiState.Error)
        assertEquals(3, repository.grades.value.size)
    }

    @Test
    fun `acepta la coma decimal`() {
        fillForm("Laboratorio", "Programación Móvil", "8,5")
        viewModel.onSaveGrade()

        assertEquals(8.5, repository.grades.value.last().score, 0.0)
    }

    @Test
    fun `al volver a escribir se descarta el error y se recupera la lista`() {
        fillForm("Laboratorio", "Programación Móvil", "abc")
        viewModel.onSaveGrade()
        assertTrue(viewModel.uiState.value is GradeUiState.Error)

        viewModel.onScoreChange("9")

        assertEquals(3, success().grades.size)
    }
}
