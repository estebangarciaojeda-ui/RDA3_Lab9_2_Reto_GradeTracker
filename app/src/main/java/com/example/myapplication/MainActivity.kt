package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.InMemoryGradeRepository
import com.example.myapplication.domain.usecase.AddGradeUseCase
import com.example.myapplication.domain.usecase.GetGradesUseCase
import com.example.myapplication.ui.presentation.grades.GradeApp
import com.example.myapplication.ui.presentation.grades.GradeViewModel
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    // El ViewModel se pide al ViewModelStore con una fábrica, de modo que sobrevive a la rotación
    // y la fábrica (donde se instancian el repositorio y los casos de uso) solo corre la primera vez.
    private val viewModel: GradeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = InMemoryGradeRepository()
                return GradeViewModel(GetGradesUseCase(repository), AddGradeUseCase(repository)) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GradeApp(viewModel = viewModel)
            }
        }
    }
}
