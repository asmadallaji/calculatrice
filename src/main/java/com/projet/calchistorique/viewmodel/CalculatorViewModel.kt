package com.projet.calchistorique.viewmodel


import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.projet.calchistorique.model.Operation
import com.projet.calchistorique.model.OperationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.objecthunter.exp4j.ExpressionBuilder
import androidx.compose.runtime.State

class CalculatorViewModel(private val repository: OperationRepository) : ViewModel() {

    init {
        loadOperations()
    }

    private val _expression = MutableStateFlow("")
    private val _result = MutableStateFlow("")
    private val _operations = MutableStateFlow<List<Operation>>(emptyList())
    private val _shouldClearExpression = mutableStateOf(false)
    private val _scrollToTop = MutableStateFlow(false)

    val expression: StateFlow<String> = _expression
    val result: StateFlow<String> = _result
    val operations: StateFlow<List<Operation>> = _operations
    val shouldClearExpression: State<Boolean> get() = _shouldClearExpression
    val scrollToTop: StateFlow<Boolean> = _scrollToTop

    fun onNumberClick(number: String) {
        if (_shouldClearExpression.value) {
            onClear()
            _expression.value = number
            _shouldClearExpression.value = false
        } else {
            _expression.value += number
        }
    }

    fun onOperationClick(operator: String) {
        if (_shouldClearExpression.value) {
            onClear()
            _shouldClearExpression.value = false
        }

        if (operator == "sqrt") {
            _expression.value += "√"
        } else if (operator in listOf("sin", "cos", "tan")) {
            _expression.value += "$operator("
        } else {
            _expression.value += operator
        }
    }

    fun onEquals() {
        if (_expression.value.isNotEmpty()) {
            val result = evaluateExpression(_expression.value)
            _result.value = result
            saveOperation(_expression.value, result)
            _shouldClearExpression.value = true
            _scrollToTop.value = true
        }
    }

    fun onClear() {
        _expression.value = ""
        _result.value = ""
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteAllOperations()
                _operations.value = emptyList()
            }
        }
    }

    private fun saveOperation(expression: String, result: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val operation = Operation(
                    expression = expression,
                    result = result,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertOperation(operation)
                loadOperations()
            }
        }
    }

    private fun loadOperations() {
        viewModelScope.launch {
            repository.allOperations.collect { operationsList ->
                _operations.value = operationsList
            }
        }
    }

    private fun evaluateExpression(expression: String): String {
        return try {
            Log.d("EXPRESSION", expression)
            val newExpression = expression
                .replace("√", "sqrt")
                .replace(Regex("(sin|cos|tan)\\((?!rad\\()([^)]*)\\)"), "$1(rad($2))")
                .replace("tan(rad(90))", "NaN")
                .replace("tan(rad(-90))", "NaN")
            Log.d("NEW_EXPRESSION", newExpression)
            val expr = ExpressionBuilder(newExpression)
                .function(object : net.objecthunter.exp4j.function.Function("rad", 1) {
                    override fun apply(vararg args: Double): Double = Math.toRadians(args[0])
                })
                .build()
            val result = expr.evaluate()

            if (result % 1.0 == 0.0) {
                String.format("%.0f", result)
            } else {
                result.toString()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.toString())
            "ERROR"
        }
    }

    fun onBackspace() {
        val currentExpression = _expression.value
        if (currentExpression.isNotEmpty()) {
            when {
                currentExpression.endsWith("sin(") -> {
                    _expression.value = currentExpression.dropLast(4)
                }
                currentExpression.endsWith("cos(") -> {
                    _expression.value = currentExpression.dropLast(4)
                }
                currentExpression.endsWith("tan(") -> {
                    _expression.value = currentExpression.dropLast(4)
                }
                else -> {
                    _expression.value = currentExpression.dropLast(1)
                }
            }
        }
    }
    fun resetScrollToTop() {
        _scrollToTop.value = false
    }
}
