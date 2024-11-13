package unical.enterpriceapplication.onlycards.ui.privateProfile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.ToTheTopListFloatingButton
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIconColor
import unical.enterpriceapplication.onlycards.viewmodels.TransactionsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.TransactionsData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun Transactions(transactionsViewModel: TransactionsViewModel) {
    // Variabili
    val transactions = transactionsViewModel.transactions.collectAsState(emptyList())   // Transazioni
    val filteredTransactions = remember { mutableStateOf(transactions.value) }   // Transazioni filtrate
    val lazyColumn = rememberLazyListState()    // Stato della lista
    val error by transactionsViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    var showFab by remember { mutableStateOf(false) }    // Mostra il pulsante
    val coroutineScope = rememberCoroutineScope()   // CoroutineScope
    val isGlobalLoading = remember { mutableStateOf(false) }    // Stato di caricamento
    val page = remember { mutableIntStateOf(0) }   // Pagina
    val selectedChipIndex = remember { mutableIntStateOf(0) }   // Indice del chip selezionato
    val chipLabels = listOf(stringResource(id = R.string.transaction_all), stringResource(id = R.string.transaction_recharge), stringResource(id = R.string.transaction_withdraw))  // Etichette dei chip

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        transactionsViewModel.getTransactions(page.intValue)
    }
    LaunchedEffect(lazyColumn) {
        snapshotFlow { lazyColumn.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (totalItems != 0 && lastVisibleItem == totalItems - 1 && transactionsViewModel.hasMoreProducts.value){
                    page.intValue++
                    transactionsViewModel.getTransactions(page.intValue)
                }

                showFab = totalItems > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
    }
    LaunchedEffect(transactions.value) {
        filteredTransactions.value = transactions.value
        selectedChipIndex.intValue = 0
    }
    LaunchedEffect(selectedChipIndex.intValue) {
        filteredTransactions.value = when (selectedChipIndex.intValue) {
            0 -> transactions.value
            1 -> transactions.value.filter { it.type }
            2 -> transactions.value.filter { !it.type }
            else -> emptyList()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(errorSnackbar) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            }
        },
        floatingActionButton = {
            if (showFab)
                ToTheTopListFloatingButton { coroutineScope.launch { lazyColumn.scrollToItem(0) } }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (transactions.value.isNotEmpty() && filteredTransactions.value.isNotEmpty()) {
                LazyColumn(state = lazyColumn, userScrollEnabled = !isGlobalLoading.value) {
                    item {
                        Text(
                            text = stringResource(id = R.string.transaction_title),
                            modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                        // Chip Group centrato
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            chipLabels.forEachIndexed { index, label ->
                                val isSelected = selectedChipIndex.intValue == index
                                FilterChip(
                                    label = { Text(text = label) },
                                    selected = isSelected,
                                    onClick = { selectedChipIndex.intValue = index },
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }

                    items(filteredTransactions.value) { transaction ->
                        TransactionItem(transaction)
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FontAwesomeIcon(
                        unicode = "\uf05a",
                        fontSize = 60.sp
                    )
                    Text(
                        text = stringResource(id = R.string.transaction_no_found),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionsData) {
    val maxLength = 10  // Lunghezza massima
    val displayPrice = if (transaction.value.amount.toString().length > maxLength) transaction.value.amount.toString().substring(0, maxLength) + "..." else transaction.value.amount    // Prezzo visualizzato
    val formattedDate = formatDate(transaction.date)    // Data formattata

    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
            .heightIn(max = 200.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (transaction.type) {
                        // Ricarica: colore verde
                        FontAwesomeIconColor(
                            unicode = "\uf063",
                            color = Color.Green,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(id = R.string.transaction_recharge_single),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else {
                        // Prelievo: colore rosso
                        FontAwesomeIconColor(
                            unicode = "\uf062",
                            color = Color.Red,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(id = R.string.transaction_withdraw_single),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Text(
                    text = "$displayPrice €",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = formattedDate,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

fun formatDate(dateString: String): String {
    // Parsing della stringa in LocalDateTime
    val dateTime = LocalDateTime.parse(dateString)

    // Formattazione della data nel formato desiderato
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yy, h:mm a")
    return dateTime.format(formatter)
}   // Formattazione della data