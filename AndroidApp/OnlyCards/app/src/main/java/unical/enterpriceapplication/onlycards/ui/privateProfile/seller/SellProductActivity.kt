package unical.enterpriceapplication.onlycards.ui.privateProfile.seller

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.GameViewModel
import unical.enterpriceapplication.onlycards.viewmodels.SellProductsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.AdvancedSearchData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition

@Composable
fun SellProduct(navHostController: NavHostController, sellProductsViewModel: SellProductsViewModel, gameViewModel: GameViewModel) {
    val page = remember { mutableIntStateOf(0) }  // pagina
    val error by sellProductsViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val scrollState = rememberScrollState() // stato di scorrimento
    val games = gameViewModel.games.collectAsState()  // giochi
    val gamesError by gameViewModel.error.collectAsState()  // errore
    val productTypes by sellProductsViewModel.productType.collectAsState()  // tipo di prodotto
    var isGlobalLoading = remember { mutableStateOf(false) } // Variabile per il caricamento globale
    val advancedSearchResult by sellProductsViewModel.advancedSearchResult.collectAsState()  // Risultato della ricerca avanzata

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(gamesError) {
        gamesError?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        gameViewModel.getGames()
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
            if(games.value.isNotEmpty()){
                Column(Modifier.verticalScroll(state = scrollState).padding(15.dp)) {
                    Text(
                        text = stringResource(id = R.string.sell_title),
                        modifier = Modifier.padding(start = 5.dp, top = 15.dp),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(bottom = 5.dp))

                    SellProductForm(games.value.values, sellProductsViewModel, productTypes, isGlobalLoading, advancedSearchResult, navHostController, page)
                }
            }
            else {
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
                        text = stringResource(id = R.string.sell_no_data),
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
fun SellProductForm(games: Collection<String>, sellProductsViewModel: SellProductsViewModel, productTypes: List<ProductTypeData>, isGlobalLoading: MutableState<Boolean>, advancedSearchResult: List<AdvancedSearchData>, navHostController: NavHostController, page: MutableIntState) {
    // Variabili utili
    val coroutineScope = rememberCoroutineScope()   // scope
    val focusManager = LocalFocusManager.current // Gestore del focus
    var expandedGame by remember { mutableStateOf(false) }  // Variabile per il menu espanso
    var expanded by remember { mutableStateOf(false) }  // Variabile per il menu espanso
    var expandedProductType by remember { mutableStateOf(false) }  // Variabile per il menu espanso
    var isGame by remember { mutableStateOf(false) } // Variabile per il tipo di gioco
    val application = LocalContext.current.applicationContext as Application    // Applicazione
    var filteredGames by remember { mutableStateOf(productTypes) }  // Variabile per i giochi filtrati
    var showImageDialog by remember { mutableStateOf(false) }   // Variabile per il dialog dell'immagine
    var showAdvancedSearchDialog by remember { mutableStateOf(false) }  // Variabile per il dialog della ricerca avanzata
    var isSearchLoading by remember { mutableStateOf(false) }   // Variabile per il caricamento della ricerca
    val lazyGridStateScroll = rememberLazyGridState() // Stato della lista
    var isSaving by remember { mutableStateOf(false) }  // stato di salvataggio
    var isCancel by remember { mutableStateOf(false) }  // stato di eliminazione

    // Variabili per i dati
    var gameType by remember { mutableStateOf("") } // Tipo di gioco
    var productType by remember { mutableStateOf("") } // ProductType
    var productTypeId by remember { mutableStateOf("") } // Id del ProductType
    var selectedProductImage by remember { mutableStateOf<String?>(null) }  // Immagine del prodotto selezionato
    var description by remember { mutableStateOf("") }  // descrizione del prodotto
    var price by remember { mutableDoubleStateOf(0.0) }   // prezzo del prodotto
    var condition: Any by remember { mutableStateOf("") }  // condizione del prodotto
    var selectedImages by remember { mutableStateOf(listOf<Uri>()) }    // immagini del prodotto
    val hasMoreProducts by sellProductsViewModel.hasMoreProducts.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImages = selectedImages + uri
            }
        }
    )

    LaunchedEffect(productTypes) {
        if(productType.isEmpty())
            filteredGames = productTypes
        else
            filteredGames = productTypes.filter { product -> product.name.contains(productType, ignoreCase = true) }
    }   // Effetto lanciato quando cambia il productType

    BoxWithConstraints (modifier = Modifier.padding(top = 5.dp)) {
        val textFieldWidth = constraints.maxWidth

        OutlinedTextField(
            value = gameType,
            onValueChange = {},
            label = {
                Row {
                    Text(text = stringResource(id = R.string.sell_game_label))
                    Text(text = "*", color = Color.Red)
                }
            },
            leadingIcon = {
                FontAwesomeIcon(
                    unicode = "\uf11b",
                    fontSize = 20.sp
                )
            },
            modifier = Modifier.fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        expandedGame = !expandedGame
                        focusManager.clearFocus()
                    }
                },
            readOnly = true,
            enabled = !isGlobalLoading.value
        )

        DropdownMenu(
            expanded = expandedGame,
            onDismissRequest = { expandedGame = false },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldWidth.toDp() })
        ) {
            games.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        coroutineScope.launch {
                            // resetto le variabili
                            productType = ""
                            productTypeId = ""
                            selectedProductImage = null
                            description = ""
                            price = 0.0
                            condition = ""
                            selectedImages = listOf()
                            expandedGame = false
                            gameType = it
                            page.intValue = 0
                            val success = sellProductsViewModel.getAllProductType(gameType, page.intValue)

                            if(success) {
                                isGame = true
                            } else {
                                Toast.makeText(application, application.getString(R.string.sell_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        }
    }   // Sezione per scegliere il tipo di gioco
    if(isGame) {
        // Sezione per scegliere il modello
        ExposedDropdownMenuBox(
            modifier = Modifier.padding(top = 5.dp),
            expanded = expandedProductType,
            onExpandedChange = {
                expandedProductType = !expandedProductType
            }
        ) {
            OutlinedTextField(
                value = productType,
                onValueChange = {
                    productType = it
                    filteredGames = if (it.isNotEmpty()) {
                        productTypes.filter { product ->
                            product.name.contains(it, ignoreCase = true)
                        }
                    } else {
                        emptyList()
                    }
                },
                label = {
                    Row {
                        Text(text = stringResource(id = R.string.sell_productType_name))
                        Text(text = "*", color = Color.Red)
                    }
                },
                leadingIcon = {
                    FontAwesomeIcon(
                        unicode = "\uf03a",
                        fontSize = 18.sp
                    )
                },
                trailingIcon = {
                    if (selectedProductImage != null) {
                        FontAwesomeIcon(
                            unicode = "\uf058",
                            fontSize = 18.sp,
                            modifier = Modifier.clickable {
                                showImageDialog = true
                            }
                        )
                    }
                    else if(isSearchLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable).fillMaxWidth(),
                enabled = !isGlobalLoading.value
            )

            ExposedDropdownMenu(
                expanded = expandedProductType,
                onDismissRequest = { expandedProductType = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(id = R.string.sell_productType_advanced_research1) + " ")

                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(stringResource(id = R.string.sell_productType_advanced_research2))
                                }
                            }
                        )
                    },
                    onClick = {
                        coroutineScope.launch {
                            expandedProductType = false
                            expandedGame = false
                            expanded = false

                            isSearchLoading = true
                            isGlobalLoading.value = true
                            val success = sellProductsViewModel.advancedSearch(gameType, productType)

                            if(success) {
                                showAdvancedSearchDialog = true
                            } else {
                                Toast.makeText(application, application.getString(R.string.sell_advanced_research_error), Toast.LENGTH_SHORT).show()
                            }
                            isGlobalLoading.value = false
                            isSearchLoading = false
                        }
                    },
                    modifier = Modifier.padding(bottom = 10.dp),
                    enabled = !isGlobalLoading.value
                )
                filteredGames.forEach { game ->
                    DropdownMenuItem(
                        text = { Text(game.name) },
                        onClick = {
                            productType = game.name
                            productTypeId = game.id.toString()
                            selectedProductImage = game.photo
                            expandedProductType = false
                            focusManager.clearFocus()
                        },
                        leadingIcon = {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(game.photo).memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).error(R.drawable.error_card).build(),
                                contentDescription = "ProductType image",
                                modifier = Modifier
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                contentScale = ContentScale.Fit
                            )
                        },
                        modifier = Modifier.padding(bottom = 10.dp),
                        enabled = !isGlobalLoading.value
                    )
                }

                if(hasMoreProducts && productTypes.isNotEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.sell_show_product),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            coroutineScope.launch {
                                isGlobalLoading.value = true
                                expandedProductType = false

                                page.intValue++
                                val success = sellProductsViewModel.getMoreProductType(gameType, page.intValue)

                                if(!success)
                                    Toast.makeText(application, application.getString(R.string.sell_show_product_error), Toast.LENGTH_SHORT).show()

                                isGlobalLoading.value = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGlobalLoading.value
                    )
                }
            }
        }

        // descrizione del prodotto
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = {
                Row {
                    Text(text = stringResource(id = R.string.sell_products_description_label))
                    Text(text = "*", color = Color.Red)
                }
            },
            leadingIcon = {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.TopStart
                ) {
                    FontAwesomeIcon(
                        unicode = "\uf15c",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(200.dp),
            enabled = !isGlobalLoading.value
        )

        // condizioni del prodotto
        BoxWithConstraints (modifier = Modifier.padding(top = 5.dp)) {
            val textFieldWidth = constraints.maxWidth

            OutlinedTextField(
                value = if(condition is Condition) (condition as Condition).value else "",
                onValueChange = {},
                label = {
                    Row {
                        Text(text = stringResource(id = R.string.sell_products_condition_label))
                        Text(text = "*", color = Color.Red)
                    }
                },
                leadingIcon = {
                    FontAwesomeIcon(
                        unicode = "\uf005",
                        fontSize = 20.sp
                    )
                },
                modifier = Modifier.fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            expanded = !expanded
                            focusManager.clearFocus()
                        }
                    },
                enabled = !isGlobalLoading.value,
                readOnly = true,
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(LocalDensity.current) { textFieldWidth.toDp() })
            ) {
                Condition.getValues().forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            condition = Condition.fromString(it)
                            expanded = false
                        }
                    )
                }
            }
        }

        // prezzo del prodotto
        OutlinedTextField(
            value = price.toString(),
            onValueChange = { price = it.toDouble() },
            label = {
                Row {
                    Text(text = stringResource(id = R.string.sell_products_price_label))
                    Text(text = "*", color = Color.Red)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isGlobalLoading.value,
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth(),
            leadingIcon = {
                FontAwesomeIcon(
                    unicode = "\uf155",
                    fontSize = 20.sp
                )
            }
        )

        // Sezione delle immagini
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
        ) {
            Text(text = stringResource(id = R.string.sell_products_image_label))
            Text(text = "*", color = Color.Red)
        }
        if(selectedImages.isEmpty()) {
            Card(
                shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clickable(enabled = !isGlobalLoading.value) {
                        imagePickerLauncher.launch("image/*")
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    FontAwesomeIcon(
                        unicode = "\ue09a",
                        fontSize = 40.sp
                    )
                    Text(
                        text = stringResource(id = R.string.sell_products_load_images),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        else {
            selectedImages.forEachIndexed { index, image ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(image).memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).error(R.drawable.error_card).build(),
                        contentDescription = "Product image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = {
                            // Rimuovo l'immagine selezionata
                            if(!isGlobalLoading.value)
                                selectedImages = selectedImages.filterIndexed { i, _ -> i != index }
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        FontAwesomeIcon(
                            unicode = "\uf2ed",
                            fontSize = 20.sp,
                        )
                    }
                }
            }
        }

        //Bottone per aggiungere altre immagini
        if(selectedImages.size in 1..4) {
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                enabled = !isGlobalLoading.value
             ) {
                Text(text = stringResource(id = R.string.uploaded_products_add_image), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }   // Se il tipo di gioco è stato scelto, mostro il resto del form

    if(selectedImages.size in 1..4 && isGame)
        HorizontalDivider(modifier = Modifier.padding(top = 10.dp))

    // Sezione dei pulsanti
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        // Bottone per salvare
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    isGlobalLoading.value = true

                    val success = sellProductsViewModel.saveProduct(description, condition as Condition, price, productTypeId, selectedImages)

                    if(success) {
                        // resetto tutte le variabili
                        gameType = ""
                        productType = ""
                        productTypeId = ""
                        selectedProductImage = null
                        description = ""
                        price = 0.0
                        condition = ""
                        selectedImages = listOf()

                        // resetto le variabili di stato
                        expandedGame = false
                        expanded = false
                        expandedProductType = false
                        isGame = false
                        filteredGames = productTypes
                        showImageDialog = false
                        showAdvancedSearchDialog = false
                        isSearchLoading = false
                        isSaving = false

                        Toast.makeText(application, application.getString(R.string.sell_products_save_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(application, application.getString(R.string.sell_products_save_error), Toast.LENGTH_SHORT).show()
                    }

                    isSaving = false
                    isGlobalLoading.value = false
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = (gameType.isNotEmpty() && gameType.isNotBlank() && games.contains(gameType)) &&
                      (productType.isNotEmpty() && productType.isNotBlank() && productTypes.any { it.name == productType }) &&
                      (description.isNotEmpty() && description.isNotBlank() &&  description.length in 3..200) &&
                      (condition is Condition) &&
                      (price > 0.01 && price < 500000) &&
                      (selectedImages.size in 1..5) && !isGlobalLoading.value
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.sell_products_save), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        // Bottone per annullare
        Button(
            onClick = {
                isCancel = true
                isGlobalLoading.value = true

                // resetto tutte le variabili
                gameType = ""
                productType = ""
                productTypeId = ""
                selectedProductImage = null
                description = ""
                price = 0.0
                condition = ""
                selectedImages = listOf()

                // resetto le variabili di stato
                expandedGame = false
                expanded = false
                expandedProductType = false
                isGame = false
                filteredGames = productTypes
                showImageDialog = false
                showAdvancedSearchDialog = false
                isSearchLoading = false
                isSaving = false

                isCancel = false
                isGlobalLoading.value = false
            },
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            enabled = !isGlobalLoading.value
        ) {
            if(isCancel) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            else {
                Text(text = stringResource(id = R.string.sell_products_cancel), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showImageDialog && selectedProductImage != null) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(id = R.string.sell_productType_selected),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(selectedProductImage)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .error(R.drawable.error_card)
                            .build(),
                        contentDescription = "Selected productType image",
                        modifier = Modifier
                            .size(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showImageDialog = false }) {
                        Text(text = stringResource(id = R.string.sell_close))
                    }
                }
            }
        }
    }   // dialog che mostra la foto del modello selezionato
    if (showAdvancedSearchDialog) {
        AlertDialog(
            onDismissRequest = { showAdvancedSearchDialog = false },
            title = {
                Text(text = stringResource(id = R.string.sell_advanced_research_title), fontWeight = FontWeight.Bold)
            },
            text = {
                if(advancedSearchResult.isNotEmpty()) {
                    if(!isGlobalLoading.value) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            state = lazyGridStateScroll,
                        ) {
                            items(advancedSearchResult.size) { index ->
                                val product = advancedSearchResult[index]
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .fillMaxWidth()
                                        .clickable(enabled = !isGlobalLoading.value) {
                                            coroutineScope.launch {
                                                isGlobalLoading.value = true
                                                val success = sellProductsViewModel.saveAdvancedSearch(gameType, product.id)

                                                if(success) {
                                                    productTypeId = ""
                                                    selectedProductImage = null
                                                    productType = ""
                                                    Toast.makeText(application, application.getString(R.string.sell_advanced_research_save_success), Toast.LENGTH_SHORT).show()
                                                    showAdvancedSearchDialog = false
                                                } else {
                                                    Toast.makeText(application, application.getString(R.string.sell_advanced_research_save_error), Toast.LENGTH_SHORT).show()
                                                }
                                                isGlobalLoading.value = false
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier.padding(5.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(product.image)
                                                .memoryCachePolicy(CachePolicy.DISABLED)
                                                .diskCachePolicy(CachePolicy.DISABLED)
                                                .error(R.drawable.error_card)
                                                .build(),
                                            contentDescription = "Product image",
                                            modifier = Modifier
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(5.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(text = product.name, fontSize = 15.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                    else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.padding(10.dp))
                            Text(text = stringResource(id = R.string.sell_advanced_research_saving))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        FontAwesomeIcon(
                            unicode = "\uf7a9",
                            fontSize = 60.sp
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(id = R.string.sell_advanced_research_no_data1) + " ")

                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(stringResource(id = R.string.sell_advanced_research_no_data2))
                                }
                            },
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clickable {
                                navHostController.navigate("productRequest")
                                showAdvancedSearchDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdvancedSearchDialog = false }, enabled = !isGlobalLoading.value) {
                    Text(text = stringResource(id = R.string.address_close))
                }
            },
            modifier = Modifier.heightIn(min = 200.dp, max = 540.dp)
        )
    } // dialog che mostra i risultati della ricerca avanzata
}