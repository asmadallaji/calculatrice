package com.projet.calchistorique.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.projet.calchistorique.model.Operation
import com.projet.calchistorique.ui.theme.CalcHistoriqueTheme
import com.projet.calchistorique.viewmodel.CalculatorViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CalculatorButton(symbol: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(4.dp)
    ) {
        Text(text = symbol, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun Display(expression: String, result: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Expression: $expression", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Result: $result",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Blue
        )
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onOperationClick: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit
) {
    Column {
        val buttonLabels = listOf(
            listOf("7", "8", "9", "/", "Back"),
            listOf("4", "5", "6", "*", "C"),
            listOf("1", "2", "3", "-", "("),
            listOf("0", ".", "+" ,"=", ")"),
            listOf("sqrt", "^"),
            listOf("sin", "cos", "tan"),
        )

        buttonLabels.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                row.forEach { label ->
                    if (label.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                        return@forEach
                    } else {
                        Button(
                            onClick = {
                                when (label) {
                                    "=" -> onEquals()
                                    "C" -> onClear()
                                    in listOf(
                                        "+",
                                        "-",
                                        "*",
                                        "/",
                                        "(",
                                        ")",
                                        "sqrt",
                                        "^",
                                        "sin",
                                        "cos",
                                        "tan"
                                    ) -> onOperationClick(label)

                                    "Back" -> onBackspace()
                                    else -> onNumberClick(label)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            if (label == "Back") {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = Color.White
                                )
                                return@Button
                            }
                            if (label == "sqrt") {
                                Text("√", style = MaterialTheme.typography.titleMedium)
                                return@Button
                            }
                            Text(label, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(viewModel: CalculatorViewModel = viewModel()) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val operations by viewModel.operations.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculatrice Historique") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.clearHistory() }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear History")
            }
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .padding(16.dp)) {
            Display(expression = expression, result = result)
            Spacer(modifier = Modifier.height(8.dp))
            NumberPad(
                onNumberClick = viewModel::onNumberClick,
                onOperationClick = viewModel::onOperationClick,
                onEquals = viewModel::onEquals,
                onClear = viewModel::onClear,
                onBackspace = viewModel::onBackspace
            )
            Spacer(modifier = Modifier.height(8.dp))
            HistoryListScroll(operations = operations, viewModel = viewModel)
        }
    }
}

@Composable
fun HistoryListScroll(operations: List<Operation>, viewModel: CalculatorViewModel) {
    val listState = rememberLazyListState()
    val showTopArrow = remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val showBottomArrow = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex + listState.layoutInfo.visibleItemsInfo.size < listState.layoutInfo.totalItemsCount
        }
    }
    val iconSize = 48.dp
    val coroutineScope = rememberCoroutineScope()
    val scrollToTop by viewModel.scrollToTop.collectAsState()

    LaunchedEffect(scrollToTop) {
        if (scrollToTop) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
            viewModel.resetScrollToTop()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, bottom = 50.dp)
        ) {
            items(operations) { operation ->
                Text(
                    text = "${operation.expression} = ${operation.result}",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 18.sp,  // Increased font size
                        fontWeight = FontWeight.Bold  // Bold font weight
                    ),
                    modifier = Modifier.padding(8.dp)  // Added padding for better spacing
                )
            }
        }
        LaunchedEffect(listState.firstVisibleItemIndex) {
            snapshotFlow { listState.isScrollInProgress }
                .collect { isScrolling ->
                    if (!isScrolling) {
                        coroutineScope.launch {
                            listState.animateScrollToItem(listState.firstVisibleItemIndex)
                        }
                    }
                }
        }
        if (showTopArrow.value) {
            Icon(
                imageVector = Icons.Default.ArrowDropUp,
                contentDescription = "Scroll Up",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(iconSize)
                    .padding(top = 8.dp),
                tint = Color.Gray
            )
        }

        if (showBottomArrow.value) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Scroll Down",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .size(iconSize)
                    .padding(bottom = 8.dp),
                tint = Color.Gray
            )
        }
    }
}
