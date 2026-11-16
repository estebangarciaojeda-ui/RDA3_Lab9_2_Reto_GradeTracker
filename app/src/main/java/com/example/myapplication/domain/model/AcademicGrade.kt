package com.example.myapplication.domain.model

/** Calificación obtenida en una actividad evaluada de una asignatura. */
data class AcademicGrade(
    val id: String,
    val activityName: String,
    val subject: String,
    val score: Double
)
