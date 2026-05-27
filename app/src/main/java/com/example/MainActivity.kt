package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.SafetyDatabase
import com.example.data.repository.SafetyRepository
import com.example.ui.SafetyViewModel
import com.example.ui.SafetyViewModelFactory
import com.example.ui.WomenSafetyApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup edge to edge display
        enableEdgeToEdge()

        // Initialize Local Database Engine
        val database = SafetyDatabase.getDatabase(applicationContext)
        val repository = SafetyRepository(database.safetyDao())

        // Initialize State Core ViewModel
        val viewModel = ViewModelProvider(
            this,
            SafetyViewModelFactory(application, repository)
        )[SafetyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                WomenSafetyApp(viewModel = viewModel)
            }
        }
    }
}
