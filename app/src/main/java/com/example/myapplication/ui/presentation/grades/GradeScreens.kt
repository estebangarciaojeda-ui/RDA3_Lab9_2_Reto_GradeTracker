package com.example.myapplication.ui.presentation.grades

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.domain.model.AcademicGrade

@Composable
fun GradeApp(viewModel: GradeViewModel) {
    val screen by viewModel.currentScreen.collectAsState()

    // Con el formulario abierto, el gesto Atrás regresa al listado en lugar de cerrar la aplicación
    BackHandler(enabled = screen == GradeScreen.FORM) { viewModel.onNavigateToList() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (screen) {
            GradeScreen.LIST -> GradeListScreen(viewModel)
            GradeScreen.FORM -> GradeFormScreen(viewModel)
        }
    }
}

@Composable
fun GradeListScreen(viewModel: GradeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.onNavigateToForm() }) {
                Text(text = "+", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is GradeUiState.Loading -> CircularProgressIndicator()
                is GradeUiState.Success -> GradeContent(state)
                is GradeUiState.Error -> ErrorCard(message = state.message, modifier = Modifier.padding(24.dp))
            }
        }
    }
}

@Composable
private fun GradeContent(state: GradeUiState.Success) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AverageCard(average = state.average, total = state.grades.size)
        Spacer(modifier = Modifier.height(16.dp))
        if (state.grades.isEmpty()) {
            Text(text = "No hay calificaciones registradas.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.grades, key = { it.id }) { grade -> GradeRow(grade) }
            }
        }
    }
}

@Composable
fun AverageCard(average: Double, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Promedio general acumulado", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "%.2f".format(average),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(text = "Sobre 10 puntos, con $total actividades", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun GradeRow(grade: AcademicGrade) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = grade.activityName, style = MaterialTheme.typography.titleMedium)
                Text(text = grade.subject, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "%.1f".format(grade.score), style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun ErrorCard(message: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun GradeFormScreen(viewModel: GradeViewModel) {
    val form by viewModel.form.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nueva calificación",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = form.activityName,
                onValueChange = viewModel::onActivityNameChange,
                label = { Text("Actividad") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = form.subject,
                onValueChange = viewModel::onSubjectChange,
                label = { Text("Asignatura") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = form.score,
                onValueChange = viewModel::onScoreChange,
                label = { Text("Nota (0.0 a 10.0)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            (uiState as? GradeUiState.Error)?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorCard(message = error.message)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(onClick = { viewModel.onNavigateToList() }) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { viewModel.onSaveGrade() },
                    enabled = form.isComplete
                ) {
                    Text("Registrar")
                }
            }
        }
    }
}
