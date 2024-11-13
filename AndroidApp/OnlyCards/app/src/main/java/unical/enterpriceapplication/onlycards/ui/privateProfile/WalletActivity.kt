package unical.enterpriceapplication.onlycards.ui.privateProfile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.WalletViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.TransactionsData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WalletData

@Composable
fun Wallet(walletViewModel: WalletViewModel, navHostController: NavHostController) {
    // Variabili
    val wallet by walletViewModel.wallet.collectAsState(null)    // Wallet
    val scrollState = rememberScrollState() // stato di scorrimento
    val error by walletViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val globalLoading = remember { mutableStateOf(false) }    // Stato di caricamento

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        walletViewModel.getWallet(0)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(errorSnackbar) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor =  MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError ,
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            if(wallet != null) {
                Column(Modifier.verticalScroll(state = scrollState, enabled = !globalLoading.value)) {
                    Text(
                        text = stringResource(id = R.string.wallet_title),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                    wallet?.let { nonNullWallet ->
                        WalletComponent(globalLoading, walletViewModel, navHostController, nonNullWallet)
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FontAwesomeIcon(
                        unicode = "\uf05a",
                        fontSize = 60.sp
                    )
                    Text(
                        text = stringResource(id = R.string.wallet_no_found),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletComponent(isGlobalLoading: MutableState<Boolean>, walletViewModel: WalletViewModel, navHostController: NavHostController, wallet: WalletData) {
    var showBottomSheetRecharge by remember { mutableStateOf(false) }    // Mostra il bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }    // Mostra il bottom sheet
    val sheetState = rememberModalBottomSheetState()    // stato del bottom sheet
    val scope = rememberCoroutineScope()   // scope
    val maxLength = 10  // Lunghezza massima
    val displayText = if (wallet.balance.toString().length > maxLength) { wallet.balance.toString().take(maxLength) + "..." } else { wallet.balance.toString() }

    // Card principale
    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
            .heightIn(max = 200.dp)
    ) {
        Text(
            text = stringResource(id = R.string.wallet_balance),
            modifier = Modifier.padding(start = 20.dp, top = 15.dp),
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$displayText €",
            modifier = Modifier.padding(start = 20.dp, top = 5.dp, bottom = 15.dp),
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
    }

    // Sezione delle entrate e delle spese
    Row(modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)) {
        Card(
            shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 5.dp)
                .heightIn(max = 200.dp)
        ) {
            Row {
                FontAwesomeIcon(
                    unicode = "\uf0d7",
                    fontSize = 30.sp,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp).align(Alignment.CenterVertically)
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.wallet_receipt),
                        modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = receiptTotal(wallet.transactions) + " €",
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 10.dp),
                        fontSize = 20.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.padding(start = 5.dp, end = 5.dp))
        Card(
            shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
            modifier = Modifier
                .weight(1f)
                .padding(top = 5.dp, bottom = 5.dp)
                .heightIn(max = 200.dp)
        ) {
            Row {
                FontAwesomeIcon(
                    unicode = "\uf0d8",
                    fontSize = 30.sp,
                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp).align(Alignment.CenterVertically)
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.wallet_expense),
                        modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = expenseTotal(wallet.transactions) + " €",
                        modifier = Modifier.padding(start = 10.dp, top = 5.dp, bottom = 10.dp),
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    // Sezione dei bottoni
    Row(modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)) {
        // Bottone per ricaricare
        Button(
            onClick = {
                scope.launch {
                    showBottomSheetRecharge = true
                    sheetState.show()
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = !isGlobalLoading.value
        ) {
            if(showBottomSheetRecharge) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.wallet_recharge), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.padding(start = 5.dp, end = 5.dp))

        // Bottone per prelevare
        Button(
            onClick = {
                scope.launch {
                    showBottomSheet = true
                    sheetState.show()
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = !isGlobalLoading.value
        ) {
            if(showBottomSheet) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.wallet_withdraw), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }

    if(showBottomSheetRecharge){
        WalletModal (
            onDismiss = { showBottomSheetRecharge = false },
            sheetState = sheetState,
            type = "recharge",
            isGlobalLoading = isGlobalLoading,
            walletViewModel = walletViewModel,
            navHostController = navHostController
        )
    }
    if(showBottomSheet){
        WalletModal (
            onDismiss = { showBottomSheet = false },
            sheetState = sheetState,
            type = "withdraw",
            isGlobalLoading = isGlobalLoading,
            walletViewModel = walletViewModel,
            navHostController = navHostController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletModal(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    type: String,
    isGlobalLoading: MutableState<Boolean>,
    walletViewModel: WalletViewModel,
    navHostController: NavHostController
) {
    var price by remember { mutableDoubleStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        if(type == "recharge") {
            Column {
                Text(
                    text = stringResource(id = R.string.wallet_recharge),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                )
                OutlinedTextField(
                    value = price.toString(),
                    onValueChange = { price = it.toDouble() },
                    label = {
                        Row {
                            Text(text = stringResource(id = R.string.wallet_recharge_amount))
                            Text(text = "*", color = Color.Red)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isGlobalLoading.value,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 5.dp)
                        .fillMaxWidth(),
                    leadingIcon = {
                        FontAwesomeIcon(
                            unicode = "\uf155",
                            fontSize = 20.sp
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        enabled = !isGlobalLoading.value && price > 0.01,
                        onClick = {
                            coroutineScope.launch {
                                isGlobalLoading.value = true
                                val success = walletViewModel.rechargeWallet(price)

                                if(success) {
                                    Toast.makeText(application, application.getString(R.string.wallet_recharge_success), Toast.LENGTH_SHORT).show()
                                    price = 0.0
                                    onDismiss()
                                    navHostController.navigate("wallet") {
                                        popUpTo("wallet") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    Toast.makeText(application, application.getString(R.string.wallet_recharge_error), Toast.LENGTH_SHORT).show()
                                }

                                isGlobalLoading.value = false
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.wallet_recharge))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            price = 0.0
                            onDismiss()
                        },
                        enabled = !isGlobalLoading.value
                    ) {
                        Text(text = stringResource(id = R.string.wallet_close))
                    }
                }
            }
        }
        else {
            Column {
                Text(
                    text = stringResource(id = R.string.wallet_withdraw),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                )

                OutlinedTextField(
                    value = price.toString(),
                    onValueChange = { price = it.toDouble() },
                    label = {
                        Row {
                            Text(text = stringResource(id = R.string.wallet_withdraw_amount))
                            Text(text = "*", color = Color.Red)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isGlobalLoading.value,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 5.dp)
                        .fillMaxWidth(),
                    leadingIcon = {
                        FontAwesomeIcon(
                            unicode = "\uf155",
                            fontSize = 20.sp
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        enabled = !isGlobalLoading.value && price > 0.01,
                        onClick = {
                            coroutineScope.launch {
                                isGlobalLoading.value = true
                                val success = walletViewModel.withdrawFromWallet(price)

                                if(success) {
                                    Toast.makeText(application, application.getString(R.string.wallet_withdraw_success), Toast.LENGTH_SHORT).show()
                                    price = 0.0
                                    onDismiss()
                                    navHostController.navigate("wallet") {
                                        popUpTo("wallet") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } else {
                                    Toast.makeText(application, application.getString(R.string.wallet_withdraw_error), Toast.LENGTH_SHORT).show()
                                }

                                isGlobalLoading.value = false
                            }
                        }
                    ) {
                        Text(text = stringResource(id = R.string.wallet_withdraw))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            price = 0.0
                            onDismiss()
                        },
                        enabled = !isGlobalLoading.value
                    ) {
                        Text(text = stringResource(id = R.string.wallet_close))
                    }
                }
            }
        }
    }
}

fun receiptTotal(transactions: List<TransactionsData>): String {
    var total = 0.0

    for (transaction in transactions) {
        if (transaction.type) {
            total += transaction.value.amount
        }
    }

    return total.toString()
}   // Funzione per ottenere il totale delle entrate
fun expenseTotal(transactions: List<TransactionsData>): String {
    var total = 0.0

    for (transaction in transactions) {
        if (!transaction.type) {
            total += transaction.value.amount
        }
    }

    return total.toString()
}   // Funzione per ottenere il totale delle spese