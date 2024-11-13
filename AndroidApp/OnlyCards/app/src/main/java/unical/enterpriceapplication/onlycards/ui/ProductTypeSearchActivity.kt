package unical.enterpriceapplication.onlycards.ui


import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

import androidx.compose.runtime.Composable

import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.ProductTypeViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.util.Locale



@Composable
fun SearchScreen(
    query: String? = null,
    game: String,
    onGameSelected: (String) -> Unit,
    navHostController: NavHostController,
    productTypeViewModel: ProductTypeViewModel,
    isAdmin: Boolean = false // Aggiunto il parametro isAdmin
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    LaunchedEffect(navHostController) {
        productTypeViewModel.retrieveProductTypes(game, name = query)
        productTypeViewModel.retrieveLanguages()
        productTypeViewModel.retriveTypes()
        productTypeViewModel.retriveSortingOptions()
        productTypeViewModel.retriveFeatures(game)
        productTypeViewModel.retriveGames()

    }
    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            ProductTypesPage(
                onGameSelected = onGameSelected,
                productTypeViewModel = productTypeViewModel,
                currentGame = game,
                navHostController = navHostController,
                isAdmin = isAdmin // Passa isAdmin a ProductTypesPage
            )
        }
    }
}




@Composable
fun ProductTypesPage(
    productTypeViewModel: ProductTypeViewModel,
    onGameSelected: (String) -> Unit,
    currentGame: String,
    navHostController: NavHostController,
    isAdmin: Boolean = false // Aggiungi parametro per identificare se l'utente è admin
) {

    val productTypes = productTypeViewModel.productTypesData.collectAsState()
    val loading = productTypeViewModel.isLoading.collectAsState()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var showFab by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for scroll state changes
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItemsCount = layoutInfo.totalItemsCount
                if (lastVisibleItemIndex == totalItemsCount - 1 && productTypes.value.number < productTypes.value.totalPages - 1) {
                    productTypeViewModel.retrieveProductTypes(currentGame, productTypes.value.number + 1)
                }
                showFab = totalItemsCount > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
    }
    Scaffold(
        topBar = { FilterBar(productTypeViewModel, onGameSelected = onGameSelected, currentGame = currentGame) },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                ToTheTopListFloatingButton { coroutineScope.launch { gridState.animateScrollToItem(0) } }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.padding(it)) {
            val message = stringResource(id = R.string.search_text)
            LaunchedEffect(productTypes.value.error) {
                if (productTypes.value.error) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
            if (productTypes.value.content.isEmpty() && !loading.value) {
                // Display when no items are found
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.search_no_result), textAlign = TextAlign.Center)
                }
            } else {
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(2)
                ) {
                    items(productTypes.value.content) { productType ->
                        key(productType.id) {
                            ProductTypeCard(productType = productType, isAdmin = isAdmin,
                                onEdit = {
                                    navHostController.navigate("editProduct/${productType.id}")
                                },
                                onDelete = {
                                    productTypeViewModel.retrieveProductTypes(currentGame) // Aggiorna la lista dopo l'eliminazione
                                },
                                navHostController = navHostController,
                                productTypeViewModel = productTypeViewModel, // Passa il ViewModel
                                currentGame = currentGame
                            ) // Passa il parametro isAdmin
                        }
                    }
                    if (loading.value) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}



@Composable
fun ProductTypeCard(
    productType: ProductTypeData,
    currentGame: String,
    isAdmin: Boolean = false,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},  // onDelete sarà chiamato al successo
    navHostController: NavHostController,
    productTypeViewModel: ProductTypeViewModel
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var isLoadingDelete by remember { mutableStateOf(false) }  // Stato per mostrare il caricamento
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isLoading = remember { mutableStateOf(false) }  // Stato generale di caricamento

    Card(
        modifier = Modifier
            .padding(5.dp)
            .fillMaxWidth()
            .clickable {
                navHostController.navigate("productTypeDetails/${productType.id}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display product image
                AsyncImage(
                    model = productType.photo,
                    contentDescription = "product type photo",
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    alignment = Alignment.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product name
                Text(
                    text = productType.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )

                // Product type
                Text(
                    text = productType.type,
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price and currency
                Text(
                    text = "${stringResource(R.string.search_from)} ${formatPrice(productType.minPrice.amount, productType.minPrice.currency)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Show "Elimina" button if the user is an admin
                if (isAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = { showDeleteConfirmation = true },
                        enabled = !isLoadingDelete // Disabilita il bottone durante il caricamento
                    ) {
                        if (isLoadingDelete) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(stringResource(R.string.search_delete))
                        }
                    }
                }
            }

            // Icon in alto a destra per modificare
            if (isAdmin) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(28.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Dialog di conferma per eliminazione
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this product type?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmation = false
                            scope.launch {
                                isLoadingDelete = true  // Inizia il caricamento
                                isLoading.value = true  // Stato di caricamento

                                // Chiama il metodo con onSuccess e onError
                                productTypeViewModel.deleteProductType(
                                    productType.id,
                                    onSuccess = {
                                        isLoadingDelete = false  // Fine del caricamento
                                        isLoading.value = false  // Fine del caricamento generale

                                        // Qui richiamiamo la funzione che recupera nuovamente i product types, aggiornando la lista
                                        productTypeViewModel.retrieveProductTypes(currentGame)

                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.product_type_delete_success),
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Callback per aggiornare l'interfaccia utente
                                        onDelete()
                                    },
                                    onError = { errorMessage ->
                                        isLoadingDelete = false  // Fine del caricamento
                                        isLoading.value = false  // Fine del caricamento generale

                                        Toast.makeText(
                                            context,
                                            errorMessage,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )

                            }
                        },
                        enabled = !isLoadingDelete // Disabilita il bottone durante il caricamento
                    ) {
                        if (isLoadingDelete) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text("Delete")
                        }
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteConfirmation = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}











// Function to format price based on currency (USD or EUR)
fun formatPrice(amount: Double, currency: String): String {
    return when (currency.uppercase(Locale.getDefault())) {
        "USD" -> "$${"%.2f".format(amount)}" // Display with dollar sign and 2 decimal places
        "EUR" -> "€${"%.2f".format(amount)}" // Display with euro sign and 2 decimal places
        else -> "$amount $currency" // Fallback if currency is not USD or EUR
    }
}
@Composable
fun ToTheTopListFloatingButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        FontAwesomeIcon(
            fontSize = 24.sp,
            unicode = "\uf062" // Arrow up icon
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(productTypeViewModel: ProductTypeViewModel, onGameSelected: (String) -> Unit, currentGame: String) {
    // State to control the visibility of the ModalBottomSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val selectedLanguage = remember { mutableStateOf("") }
    val selectedType = remember { mutableStateOf("") }
    var sliderPosition by remember { mutableStateOf(0f..1000f) }
    val selectedSorting = remember { mutableStateOf("") }
    val selectedFeatures = remember { mutableStateMapOf<String, String>() }
    val selectedGame =  remember { mutableStateOf(currentGame) }

    Column{
        Button(
            onClick = {
                scope.launch {
                    showBottomSheet = true
                    sheetState.show() // Show the bottom sheet
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.padding(16.dp, 0.dp).fillMaxWidth()
        ) {
            // Use Row to arrange the Text and Icon horizontally
            Row(
                verticalAlignment = Alignment.CenterVertically // Align text and icon vertically centered
            ) {
                // Text
                Text(text = stringResource(R.string.search_filter), color = MaterialTheme.colorScheme.onSurface)

                // Add some space between text and icon
                Spacer(modifier = Modifier.width(8.dp))

                // Icon
                FontAwesomeIcon(
                    fontSize = 16.sp,
                    unicode = "\uf0b0", // Filter icon
                )
            }
        }

        // Call the FilterModalBottomSheet composable
        if (showBottomSheet) {
            FilterModalBottomSheet(
                sliderPosition = sliderPosition,
                sheetState = sheetState,
                selectedFeatures = selectedFeatures,
                onSliderValueChange = { range ->
                    sliderPosition = range
                },
                productTypeViewModel = productTypeViewModel,
                selectedLanguages = selectedLanguage,
                selectedType = selectedType,
                selectedSorting = selectedSorting,
                selectedGame = selectedGame,
                onDismissRequest = {
                    scope.launch {
                        showBottomSheet = false
                        sheetState.hide() // Hide the bottom sheet
                        selectedLanguage.value = ""
                        selectedType.value = ""
                        sliderPosition = 0f..1000f
                        selectedSorting.value = ""
                        selectedFeatures.clear()
                        selectedGame.value = ""
                    }
                },
                onFilterRequest = {
                    scope.launch {
                        showBottomSheet = false
                        sheetState.hide() // Hide the bottom sheet
                        val minPrice = String.format(Locale.getDefault(), "%.0f", sliderPosition.start)
                        var maxPrice = String.format(Locale.getDefault(), "%.0f", sliderPosition.endInclusive)
                        if(maxPrice=="500"){
                            maxPrice=""
                        }
                        if(selectedGame.value!=currentGame){

                            onGameSelected(selectedGame.value)
                            selectedFeatures.clear()
                        }
                        productTypeViewModel.retrieveProductTypes(language = selectedLanguage.value, type = selectedType.value, minPrice = minPrice, maxPrice = maxPrice, sorting = selectedSorting.value, features = selectedFeatures, game=selectedGame.value)
                    }
                }
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterModalBottomSheet(sheetState: SheetState, onFilterRequest: () -> Unit, productTypeViewModel: ProductTypeViewModel, selectedLanguages: MutableState<String>, selectedType:MutableState<String>,
                           sliderPosition: ClosedFloatingPointRange<Float>, onSliderValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
                           selectedSorting:MutableState<String>, selectedFeatures: MutableMap<String, String>, onDismissRequest: () -> Unit, selectedGame: MutableState<String>) {
    val languages = productTypeViewModel.languages.collectAsState()
    val types = productTypeViewModel.types.collectAsState()
    val sortingOptions = productTypeViewModel.sortingOptions.collectAsState()
    val features = productTypeViewModel.featureList.collectAsState()
    val games = productTypeViewModel.games.collectAsState()


    // ModalBottomSheet
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background, // Background color
        modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp), // Adatta la griglia per min size
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Padding del contenuto
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sezione Filtri
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_filter), modifier = Modifier.fillMaxWidth())
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }

            // Sezione Lingua
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_language))
            }

            items(languages.value.keys.toList()) { code ->
                FilterChip(
                    selected = selectedLanguages.value == code,
                    onClick = {
                        selectedLanguages.value = if (selectedLanguages.value == code) "" else code
                    },
                    label = { languages.value[code]?.let { Text(text = it) }}
                )
            }
            // Sezione Giochi
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_games))
            }

            items(games.value.keys.toList()) { code ->
                FilterChip(
                    selected = selectedGame.value == code,
                    onClick = {
                        if(selectedGame.value != code){
                            selectedGame.value = code}
                    },
                    label = { games.value[code]?.let { Text(text = it) } }
                )
            }

            // Sezione Tipi
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_type))
            }

            items(types.value.keys.toList()) { code ->
                FilterChip(
                    selected = selectedType.value == code,
                    onClick = {
                        selectedType.value = if (selectedType.value == code) "" else code
                    },
                    label = { types.value[code]?.let { Text(text = it) } }
                )
            }

            // Sezione Prezzo
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_price))
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 25.dp)) {
                    RangeSlider(
                        colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer, thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary),
                        value = sliderPosition,
                        onValueChange = { range ->
                            onSliderValueChange(range)
                        },
                        valueRange = 0f..500f,
                    )
                    Text(
                        text = if (sliderPosition.endInclusive >= 500f) {
                            String.format(
                                Locale.getDefault(),
                                "%.0f€ - %.0f€ ${stringResource(R.string.search_beyond)}",
                                sliderPosition.start,
                                sliderPosition.endInclusive
                            )
                        } else {
                            String.format(
                                Locale.getDefault(),
                                "%.0f€ - %.0f€",
                                sliderPosition.start,
                                sliderPosition.endInclusive
                            )
                        }
                    )
                }
            }

            // Sezione Ordina
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text = stringResource(R.string.search_order))
            }

            items(sortingOptions.value.keys.toList()) { code ->
                FilterChip(
                    selected = selectedSorting.value == code,
                    onClick = {
                        selectedSorting.value = if (selectedSorting.value == code) "" else code
                    },
                    label = { sortingOptions.value[code]?.let { Text(text = it) } }
                )
            }
            //features
            // Lista piatta che include i titoli e le righe
            features.value.forEach { feature ->
                // Aggiungi il titolo della feature
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(text = feature.name, modifier = Modifier.fillMaxWidth())
                }

                // Creare la lista piatta degli elementi
                val chunkedList = feature.value.chunked(3) // Raggruppa per righe con 2 colonne ciascuna
                chunkedList.forEach { rowItems ->
                    // Ogni riga occupa l'intera larghezza della griglia
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Aggiungi ciascun elemento della riga
                            rowItems.forEach { code ->
                                Box(modifier = Modifier.weight(1f, fill = false).padding(horizontal = 4.dp)) {
                                    FilterChip(
                                        selected = selectedFeatures[feature.name] == code, // Selezionato se il valore nella mappa è uguale al codice
                                        onClick = {
                                            if (selectedFeatures[feature.name] == code) {
                                                // Deseleziona rimuovendo il valore o impostandolo su ""
                                                selectedFeatures[feature.name] = ""
                                            } else {
                                                // Seleziona aggiungendo il codice come valore
                                                selectedFeatures[feature.name] = code
                                            }
                                        },
                                        label = {
                                            // Mostra l'etichetta. Se la mappa contiene una stringa vuota, mostra il codice stesso.
                                            Text(text = code)
                                        }
                                    )
                                }}

                            // Gestisci gli spazi vuoti se la riga non è completa
                            if (rowItems.size < 2) {
                                repeat(2 - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f, fill = false)) // Spazi per bilanciare la riga
                                }
                            }
                        }
                    }
                }
            }

            // Sezione Bottone
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),

                        onClick = {
                            onFilterRequest()
                        },
                    ) {
                        Text(text = stringResource(R.string.search_show_result), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}