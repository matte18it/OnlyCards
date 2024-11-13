import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.ProductEditViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductEditData

@Composable
fun ProductEditScreen(
    productId: String?,
    navController: NavController,
    productEditViewModel: ProductEditViewModel

) {
    val productEditData by productEditViewModel.productEditData.collectAsState()
    val isLoading by productEditViewModel.isLoading.collectAsState()

    // Variabili di stato per feedback all'utente
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(productId) {
        productEditViewModel.getProduct(productId)
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        productEditData?.let { product ->
            ProductEditForm(
                product = product,
                viewModel = productEditViewModel,
                navController = navController,
                onSaveSuccess = {
                    showSuccessMessage = true
                },
                onSaveError = {
                    showErrorMessage = true
                }
            )
        }
    }

    // Mostra il Toast per il successo
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            Toast.makeText(
                context,
                context.getString(R.string.save_success),
                Toast.LENGTH_SHORT
            ).show()
            showSuccessMessage = false // Resetta il flag dopo la visualizzazione del Toast
        }
    }


    // Mostra il Toast per l'errore
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            Toast.makeText(
                context,
                context.getString(R.string.save_error),
                Toast.LENGTH_SHORT
            ).show()
            showErrorMessage = false
        }
    }
}

@Composable
fun ProductEditForm(
    product: ProductEditData,
    viewModel: ProductEditViewModel,
    navController: NavController,
    onSaveSuccess: () -> Unit,
    onSaveError: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    var stateDescription by remember { mutableStateOf(product.stateDescription) }
    var price by remember { mutableStateOf(product.price.amount.toString()) }
    var selectedCondition by remember { mutableStateOf(product.condition.toString()) }
    var selectedCurrency by remember { mutableStateOf(product.price.currency) }
    var expandedCurrency by remember { mutableStateOf(false) }
    var expandedCondition by remember { mutableStateOf(false) }

    // Variabili per gestire gli errori di validazione
    var descriptionError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    val currencies by viewModel.currencies.collectAsState() // Valute disponibili
    val images by viewModel.images.collectAsState() // Immagini esistenti nel database
    val selectedImages by viewModel.selectedImages.collectAsState() // Immagini aggiunte dall'utente
    val maxImages = 5
    val imageCount = selectedImages.size + images.size

    // Elenco delle condizioni disponibili
    val conditions = mapOf(
        "Mint" to "MINT",
        "Near Mint" to "NEAR_MINT",
        "Excellent" to "EXCELLENT",
        "Good" to "GOOD",
        "Light Played" to "LIGHT_PLAYED",
        "Played" to "PLAYED",
        "Poor" to "POOR"
    )

    // Launcher per selezionare immagini
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (imageCount < maxImages) {
                viewModel.addImage(it) // Aggiunge l'immagine alla lista delle selezionate
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Condizione della carta con menu a tendina centrato
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            OutlinedButton(
                onClick = { expandedCondition = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = conditions.entries.find { it.value == selectedCondition }?.key ?: selectedCondition)
            }

            DropdownMenu(
                expanded = expandedCondition,
                onDismissRequest = { expandedCondition = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                conditions.forEach { (displayName, enumValue) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            selectedCondition = enumValue
                            expandedCondition = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Descrizione del prodotto
        OutlinedTextField(
            value = stateDescription,
            onValueChange = {
                stateDescription = it
                descriptionError = stateDescription.length < 10
            },
            label = { Text(stringResource(id = R.string.description_label)) },
            isError = descriptionError,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), // Aumenta l'altezza del campo di testo
            maxLines = 5, // Permetti fino a 5 righe
        )

        // Messaggio di errore per la descrizione
        if (descriptionError) {
            Text(
                text = stringResource(id = R.string.description_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prezzo e selezione valuta (il prezzo deve essere un numero decimale valido)
        Row(modifier = Modifier.fillMaxWidth()) {
            // Campo per il prezzo
            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it
                    priceError = it.toDoubleOrNull() == null // Verifica se il prezzo è un numero valido
                },
                label = { Text(stringResource(id = R.string.price_label)) },
                isError = priceError,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Bottone per la selezione della valuta
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                OutlinedButton(
                    onClick = {
                        expandedCurrency = true
                        viewModel.getCurrencies() // Carica le valute se non lo sono già
                    }
                ) {
                    Text(selectedCurrency)
                }

                DropdownMenu(
                    expanded = expandedCurrency,
                    onDismissRequest = { expandedCurrency = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currencies.forEach { (currencyCode, currencyName) ->
                        DropdownMenuItem(
                            text = { Text(currencyName) },
                            onClick = {
                                selectedCurrency = currencyCode
                                expandedCurrency = false
                            }
                        )
                    }
                }
            }
        }

        // Messaggio di errore per il prezzo
        if (priceError) {
            Text(
                text = stringResource(id = R.string.price_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mostra le immagini esistenti con pulsante per eliminarle (dal database)
        images.forEach { image ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(model = image.photo),
                    contentDescription = "Immagine del prodotto",
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.removeExistingImage(image) }) {
                    Text(stringResource(id = R.string.delete_image))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Mostra le nuove immagini selezionate (non ancora salvate)
        selectedImages.forEach { uri ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(model = uri),
                    contentDescription = "Nuova immagine selezionata",
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.removeSelectedImage(uri) }) {
                    Text(stringResource(id = R.string.remove_image))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Pulsante per aggiungere una nuova immagine
        if (imageCount < maxImages) {
            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.add_image))
            }
        } else {
            Text(
                text = stringResource(id = R.string.image_limit_reached, maxImages),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pulsante per salvare le modifiche
        Button(
            onClick = {
                if (!descriptionError && !priceError && stateDescription.isNotEmpty() && price.isNotEmpty()) {
                    coroutineScope.launch {
                        viewModel.updateProduct(
                            productId = product.id,
                            stateDescription = stateDescription,
                            price = price.toDoubleOrNull() ?: 0.0,
                            condition = selectedCondition,
                            currency = selectedCurrency,
                            images = selectedImages,
                            onSuccess = {
                                onSaveSuccess()
                            },
                            onError = {
                                onSaveError()
                            }
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.save_changes))
        }
    }
}
