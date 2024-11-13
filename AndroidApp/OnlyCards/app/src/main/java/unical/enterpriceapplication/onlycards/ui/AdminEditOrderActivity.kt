import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.OrderViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.TransactionData
import java.time.format.DateTimeFormatter

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun EditOrderScreen(
    orderViewModel: OrderViewModel,
    navHostController: NavHostController,
    orderId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Ottieni il contesto per il Toast

    var selectedStatus by remember { mutableStateOf("PENDING") }
    var showErrorDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        orderViewModel.loadOrderDetails(orderId)
    }

    val selectedOrder by orderViewModel.selectedOrder.collectAsState()
    val isLoading by orderViewModel.isLoading.collectAsState()
    val error by orderViewModel.error.collectAsState()

    LaunchedEffect(selectedOrder) {
        selectedStatus = selectedOrder?.status ?: "PENDING"
    }

    // Stato attuale dell'ordine
    val currentOrderStatus = selectedOrder?.status

    // Controlla se lo stato attuale è "Delivered" o "Cancelled"
    val isStatusLocked = currentOrderStatus in listOf("DELIVERED", "CANCELLED")

    // Controllo della selezione basato sullo stato attuale
    val canSelectPending = currentOrderStatus == "PENDING"
    val canSelectShipped = currentOrderStatus != "CANCELLED" && currentOrderStatus != "DELIVERED"
    val canSelectDelivered = currentOrderStatus != "DELIVERED" && currentOrderStatus != "CANCELLED"
    val canSelectCancelled = currentOrderStatus == "SHIPPED" || currentOrderStatus == "PENDING"

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Text(
                    text = stringResource(id = R.string.error, error?.message ?: ""),
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                selectedOrder?.let { order ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            // Dettagli ordine
                            Text(
                                text = stringResource(id = R.string.modify_order),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Divider(color = Color.LightGray, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.last_modified_details),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(
                                    id = R.string.order_date,
                                    order.modifyDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"))
                                        ?: "N/A"
                                )
                            )
                            Text(
                                text = stringResource(id = R.string.modified_by, order.userLastEdit ?: "N/A")
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Sezione Prodotti
                            Text(
                                text = stringResource(id = R.string.products),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        }

                        items(order.transactions) { transaction ->
                            ProductItem(transaction)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(id = R.string.modify_status),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column {
                                RadioButtonRow(
                                    label = stringResource(id = R.string.status_pending),
                                    value = "PENDING",
                                    selectedValue = selectedStatus,
                                    onClick = { if (canSelectPending) selectedStatus = "PENDING" },
                                    enabled = canSelectPending // Abilitato solo se attualmente è in "Pending"
                                )
                                RadioButtonRow(
                                    label = stringResource(id = R.string.status_shipped),
                                    value = "SHIPPED",
                                    selectedValue = selectedStatus,
                                    onClick = { if (canSelectShipped) selectedStatus = "SHIPPED" },
                                    enabled = canSelectShipped // Abilitato se non è in "Cancelled" o "Delivered"
                                )
                                RadioButtonRow(
                                    label = stringResource(id = R.string.status_delivered),
                                    value = "DELIVERED",
                                    selectedValue = selectedStatus,
                                    onClick = { if (canSelectDelivered) selectedStatus = "DELIVERED" },
                                    enabled = canSelectDelivered // Abilitato se non è già "Delivered" o "Cancelled"
                                )
                                RadioButtonRow(
                                    label = stringResource(id = R.string.status_cancelled),
                                    value = "CANCELLED",
                                    selectedValue = selectedStatus,
                                    onClick = { if (canSelectCancelled) selectedStatus = "CANCELLED" },
                                    enabled = currentOrderStatus != "SHIPPED" // Disabilita se è in "Shipped"
                                )
                            }

                            if (isStatusLocked) {
                                Text(
                                    text = "Non è possibile modificare lo stato di un ordine annullato o completato.",
                                    color = Color.Red,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Button(
                                    onClick = { navHostController.popBackStack() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.onSecondary)
                                }

                                Button(
                                    onClick = {
                                        if (orderViewModel.isStateTransitionValid(order.status, selectedStatus) && !isStatusLocked) {
                                            coroutineScope.launch {
                                                orderViewModel.updateOrderStatus(order.id.toString(), selectedStatus)

                                                // Mostra il Toast di successo
                                                Toast.makeText(context, context.getString(R.string.order_update_success), Toast.LENGTH_SHORT).show()

                                                navHostController.navigate("adminOrders") {
                                                    popUpTo(navHostController.graph.startDestinationId) { inclusive = true }
                                                }
                                            }
                                        } else {
                                            showErrorDialog = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    enabled = !isStatusLocked // Disabilita il pulsante se è bloccato
                                ) {
                                    Text(stringResource(id = R.string.save), color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }

                    if (showErrorDialog) {
                        AlertDialog(
                            onDismissRequest = { showErrorDialog = false },
                            confirmButton = {
                                Button(onClick = { showErrorDialog = false }) {
                                    Text("OK")
                                }
                            },
                            title = { Text(stringResource(id = R.string.invalid_transition_title)) },
                            text = { Text(stringResource(id = R.string.invalid_transition_message)) }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ProductItem(transaction: TransactionData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(IntrinsicSize.Min)  // Adatta l'altezza in base al contenuto
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = transaction.productPhoto),
            contentDescription = stringResource(id = R.string.product_image),
            modifier = Modifier
                .height(80.dp)
                .width(80.dp)
                .padding(end = 8.dp)  // Spazio tra immagine e testo
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = transaction.productName,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = R.string.price, transaction.value.amount, transaction.value.currency))
        }
    }
}

@Composable
fun RadioButtonRow(label: String, value: String, selectedValue: String, onClick: (String) -> Unit, enabled: Boolean = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selectedValue == value,
            onClick = { if (enabled) onClick(value) }, // Solo chiama onClick se abilitato
            enabled = enabled // Disabilita il RadioButton se necessario
        )
        Text(label)
    }
}
