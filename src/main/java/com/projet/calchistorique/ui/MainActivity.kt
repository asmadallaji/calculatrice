package com.projet.calchistorique.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import com.projet.calchistorique.database.AppDatabase
import com.projet.calchistorique.model.OperationRepository
import com.projet.calchistorique.ui.theme.CalcHistoriqueTheme
import com.projet.calchistorique.viewmodel.CalculatorViewModel
import com.projet.calchistorique.viewmodel.CalculatorViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CalculatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database by lazy {
            AppDatabase.getDatabase(this)
        }
        val repository by lazy {
            OperationRepository(database.operationDao())
        }

        val viewModelFactory = CalculatorViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(CalculatorViewModel::class.java)

        setContent {
            CalcHistoriqueTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CalculatorScreen(viewModel)
                }
            }
        }
    }
}