# GradeTracker (Laboratorio 9.2, reto)

Aplicación para registrar y controlar calificaciones académicas, construida con Kotlin y Jetpack Compose
bajo Clean Architecture, MVVM y flujo unidireccional de datos (UDF). Programación Móvil, PUCE.

## Capas

| Capa | Paquete | Contenido |
|------|---------|-----------|
| Dominio | `domain.model`, `domain.repository`, `domain.usecase` | `AcademicGrade`, contrato `GradeRepository`, `GetGradesUseCase`, `AddGradeUseCase` |
| Datos | `data.repository` | `InMemoryGradeRepository` (MutableStateFlow con tres notas de ejemplo) |
| Presentación | `ui.presentation.grades` | `GradeUiState`, `GradeViewModel`, `GradeScreens` (listado y formulario) |

## Reglas de negocio

- La actividad y la asignatura no pueden estar vacías.
- La nota debe estar en el rango 0.0 a 10.0 (se rechazan también NaN e infinito).
- La nota se convierte con `toDoubleOrNull()` y el operador Elvis; una entrada no numérica produce un estado de
  error controlado y la aplicación no se cierra.
- El promedio general se calcula en el ViewModel con operaciones funcionales de colección.

## Pruebas

`./gradlew testDebugUnitTest` ejecuta las pruebas de los casos de uso y del ViewModel (JVM, sin emulador).

## Entorno

Android Studio Meerkat, API 36, JDK 21, Gradle 8.10.2, AGP 8.7.0.
