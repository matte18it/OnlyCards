package unical.enterpriceapplication.onlycards.ui.privateProfile

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import androidx.compose.material3.FilterChip
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshotFlow
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import unical.enterpriceapplication.onlycards.ui.FloatingOrderButton
import unical.enterpriceapplication.onlycards.ui.ToTheTopListFloatingButton
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.ProfileOrdersViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FilterOrderData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Status
import unical.enterpriceapplication.onlycards.viewmodels.filter.FilterViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Orders(navHostController: NavHostController, ordersViewModel: ProfileOrdersViewModel, filterViewModel: FilterViewModel) {
    // Variabili
    val application = LocalContext.current.applicationContext as Application
    val error by ordersViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val orders by ordersViewModel.orders.collectAsState(null)    // Ordini
    val scope = rememberCoroutineScope()   // scope
    var showBottomSheet by remember { mutableStateOf(false) }    // Mostra il bottom sheet
    val sheetState = rememberModalBottomSheetState()    // stato del bottom sheet
    val page = remember { mutableIntStateOf(0) }    // Pagina
    val gridListState = rememberLazyListState()   // stato della lista
    var showFab by remember { mutableStateOf(false) }   // Mostra il pulsante per tornare in cima
    val isLoading = remember { mutableStateOf(false) }    // Stato di caricamento

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        // Inizializzo i valori
        filterViewModel.resetFilters()

        // Prendo i valori
        ordersViewModel.getOrders("", "", "false", "9999999", "0", "", page.intValue)
    }
    LaunchedEffect(gridListState) {
        snapshotFlow { gridListState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (totalItems != 0 && lastVisibleItem == totalItems - 1 && ordersViewModel.hasMoreProducts.value){
                    page.intValue++
                    ordersViewModel.getOrders(
                        filterViewModel.productName.value,
                        Status.fromValue(filterViewModel.status.value, application),
                        filterViewModel.orderType.value.toString(),
                        filterViewModel.maxPrice.value.ifEmpty { "9999999" },
                        filterViewModel.minPrice.value.ifEmpty { "0" },
                        (filterViewModel.selectedDate.value?.let { convertMillisToDate(it) } ?: "").replace("/", "-"),
                        page.intValue
                    )
                }

                showFab = totalItems > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
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
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (showFab) {
                    ToTheTopListFloatingButton(
                        onClick = {
                            scope.launch { gridListState.scrollToItem(0) }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FloatingOrderButton(
                    onClick = {
                        scope.launch {
                            showBottomSheet = true
                            sheetState.show()
                        }
                    }
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            orders?.takeIf { it.isNotEmpty() }?.let { nonNullOrders ->
                LazyColumn(state = gridListState, userScrollEnabled = !isLoading.value) {
                    item {
                        Text(
                            text = stringResource(id = R.string.orders_title),
                            modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))
                    }

                    items(nonNullOrders) { order ->
                        OrdersCard(order, ordersViewModel, navHostController, filterViewModel, isLoading)
                    }
                }
            } ?: run {
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
                        text = stringResource(id = R.string.orders_filter_error),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if(showBottomSheet){
            FilterOrderModal(
                onDismiss = { showBottomSheet = false },
                sheetState = sheetState,
                application = application,
                filterViewModel = filterViewModel,
                ordersViewModel = ordersViewModel,
                page = page,
                isLoading = isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOrderModal(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    application: Application,
    filterViewModel: FilterViewModel,
    ordersViewModel: ProfileOrdersViewModel,
    page: MutableIntState,
    isLoading: MutableState<Boolean>
) {
    // Ottieni i valori dai filtri
    val productName = filterViewModel.productName
    val status = filterViewModel.status
    val orderType = filterViewModel.orderType
    val maxPrice = filterViewModel.maxPrice
    val minPrice = filterViewModel.minPrice
    val selectedDate = filterViewModel.selectedDate.value?.let { convertMillisToDate(it) } ?: ""

    // Variabili per lo stato
    val datePickerState = rememberDatePickerState(filterViewModel.selectedDate.value)   // stato del DatePicker

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(id = R.string.orders_filter),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                )
            }

            // Sezione per filtrare in base al nome
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = productName.value,
                    onValueChange = { filterViewModel.setProductName(it) },
                    label = { Text(text = stringResource(id = R.string.orders_filter_name)) },
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    leadingIcon = {
                        FontAwesomeIcon(
                            unicode = "\uf303",
                            fontSize = 20.sp
                        )
                    }
                )
            }

            // Sezione per il prezzo massimo
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = maxPrice.value,
                    onValueChange = {
                        // Controllo che il valore sia un numero
                        if (it.all { char -> char.isDigit() || char == '.' }) {
                            filterViewModel.setMaxPrice(it)
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.orders_filter_max_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    leadingIcon = {
                        FontAwesomeIcon(
                            unicode = "\uf155",
                            fontSize = 20.sp
                        )
                    }
                )
            }

            // Sezione per il prezzo minimo
            item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(
                    value = minPrice.value,
                    onValueChange = {
                        // Controllo che il valore sia un numero
                        if (it.all { char -> char.isDigit() }) {
                            filterViewModel.setMinPrice(it)
                        }
                    },
                    label = { Text(text = stringResource(id = R.string.orders_filter_min_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .fillMaxWidth(),
                    leadingIcon = {
                        FontAwesomeIcon(
                            unicode = "\uf155",
                            fontSize = 20.sp
                        )
                    }
                )
            }

            // Sezione per filtrare in base allo stato dell'ordine
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(id = R.string.orders_filter_status),
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }

            items(Status.getValues(application)) { index ->
                FilterChip(
                    label = { Text(text = index) },
                    selected = status.value == index,
                    onClick = { filterViewModel.setStatus(if (status.value == index) "" else index) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    FilterChip(
                        label = { Text(text = stringResource(R.string.orders_filter_all)) },
                        selected = status.value.isEmpty(),
                        onClick = { filterViewModel.setStatus("") },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                    Spacer(modifier = Modifier.padding(bottom = 10.dp))
                }
            }

            // Sezione per il tipo dell'ordine
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                    Text(
                        text = stringResource(id = R.string.orders_filter_type),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                    ) {
                        FilterChip(
                            label = { Text(text = stringResource(id = R.string.orders_filter_buy)) },
                            selected = !orderType.value,
                            onClick = { filterViewModel.setOrderType(false) },
                            modifier = Modifier.padding(end = 5.dp)
                        )
                        FilterChip(
                            label = { Text(text = stringResource(id = R.string.orders_filter_sell)) },
                            selected = orderType.value,
                            onClick = { filterViewModel.setOrderType(true) },
                            modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.padding(bottom = 10.dp)) }

            // Sezione per la data
            item(span = { GridItemSpan(maxLineSpan) }) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                        .fillMaxWidth()
                )

                // Osserva lo stato del datePicker e aggiorna la data nel ViewModel
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let { millis ->
                        filterViewModel.setSelectedDate(millis)
                    }
                }
            }

            // Sezione per il bottone
            item(span = { GridItemSpan(maxLineSpan) }) {
                TextButton(
                    enabled = !isLoading.value,
                    onClick = {
                        // Chiamo la funzione per filtrare gli ordini
                        page.intValue = 0
                        ordersViewModel.getOrders(
                            productName.value,
                            Status.fromValue(status.value, application),
                            orderType.value.toString(),
                            maxPrice.value.ifEmpty { "9999999" },
                            minPrice.value.ifEmpty { "0" },
                            selectedDate.replace("/", "-"),
                            page.intValue
                        )

                        // Chiudo il bottom sheet
                        onDismiss()
                    }
                ) {
                    Text(text = stringResource(id = R.string.orders_filter_show))
                }
            }
        }
    }
}

@Composable
fun OrdersCard(orderData: FilterOrderData, ordersViewModel: ProfileOrdersViewModel, navHostController: NavHostController, filterViewModel: FilterViewModel, isLoading: MutableState<Boolean>) {
    val imageHeight = 180.dp
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoadingDelete by remember { mutableStateOf(false) }
    var isLoadingShipped by remember { mutableStateOf(false) }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(orderData.transactions[0].product?.productType?.photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
            .heightIn(max = 200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(10.dp),
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Product Photo",
                modifier = Modifier
                    .height(imageHeight)
                    .clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = orderData.transactions[0].product?.productType?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Row {
                    Text(
                        text = stringResource(id = R.string.orders_date),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = " " + orderData.addDate,
                        fontSize = 15.sp
                    )
                }
                Row {
                    Text(
                        text = stringResource(id = R.string.orders_status),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = " " + Status.fromKey(orderData.status, LocalContext.current),
                        fontSize = 15.sp
                    )
                }
                Row {
                    Text(
                        text = stringResource(id = R.string.orders_total),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = " " + orderData.transactions[0].value.amount + " €",
                        fontSize = 15.sp
                    )
                }

                if (Status.fromKey(orderData.status, LocalContext.current) == Status.PENDING.getLocalizedValue(LocalContext.current)) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoadingDelete = true
                                    isLoading.value = true
                                    val success = ordersViewModel.changeStatus(orderData.id.toString(), "Cancellato")

                                    if (success) {
                                        navHostController.navigate("orders") {
                                            popUpTo("orders") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        Toast.makeText(context, context.getString(R.string.orders_cancel_success), Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        Toast.makeText(context, context.getString(R.string.orders_cancel_error), Toast.LENGTH_SHORT).show()
                                    }

                                    isLoadingDelete = false
                                    isLoading.value = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                            enabled = !isLoading.value
                        ) {
                            if (isLoadingDelete) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                            else {
                                Text(
                                    text = stringResource(id = R.string.orders_cancel),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if(filterViewModel.orderType.value) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isLoadingShipped = true
                                        isLoading.value = true
                                        val success = ordersViewModel.changeStatus(orderData.id.toString(), "Spedito")

                                        if (success) {
                                            navHostController.navigate("orders") {
                                                popUpTo("orders") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                            Toast.makeText(context, context.getString(R.string.orders_shipped_success), Toast.LENGTH_SHORT).show()
                                        }
                                        else {
                                            Toast.makeText(context, context.getString(R.string.orders_shipped_error), Toast.LENGTH_SHORT).show()
                                        }

                                        isLoadingShipped = false
                                        isLoading.value = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.padding(start = 5.dp),
                                enabled = !isLoading.value
                            ) {
                                if (isLoadingShipped) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                }
                                else {
                                    FontAwesomeIcon(
                                        unicode = "\uf48b",
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Funzioni di supporto
fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    return formatter.format(Date(millis))
}   // Funzione per convertire i millisecondi in una data