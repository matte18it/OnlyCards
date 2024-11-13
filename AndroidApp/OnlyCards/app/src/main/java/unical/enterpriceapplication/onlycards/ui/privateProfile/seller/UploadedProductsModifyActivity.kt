package unical.enterpriceapplication.onlycards.ui.privateProfile.seller

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.UploadedProductsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition

@Composable
fun UploadedProductsModify(productId: String, navHostController: NavHostController, uploadedProductsViewModel: UploadedProductsViewModel) {
    val error by uploadedProductsViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val scrollState = rememberScrollState() // stato di scorrimento
    val product by uploadedProductsViewModel.product.collectAsState()  // Prodotto
    var isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        uploadedProductsViewModel.getProduct(productId)
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
    ) {
        Box(modifier = Modifier.padding(it)) {
            if(product != null) {
                Column(Modifier.verticalScroll(state = scrollState, enabled = !isLoading.value)) {
                    Text(
                        text = stringResource(id = R.string.uploaded_products_modify_title),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                    // Form di modifica
                    product?.let { it1 -> ModifyForm(uploadedProductsViewModel, it1, navHostController, isLoading) }
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
                        text = stringResource(id = R.string.uploaded_products_no_product),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ModifyForm(uploadedProductsViewModel: UploadedProductsViewModel, product: ProductData, navHostController: NavHostController, isLoading: MutableState<Boolean>) {
    // Variabili
    var isSaving by remember { mutableStateOf(false) }
    var isCancel by remember { mutableStateOf(false) }
    var isDelete by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }  // Variabile per il menu espanso
    val focusManager = LocalFocusManager.current // Gestore del focus
    val coroutineScope = rememberCoroutineScope() // Scope per coroutine
    val application = LocalContext.current  // Contesto

    // Variabili per la modifica del prodotto
    var condition by remember { mutableStateOf(product.condition) }
    var stateDescription by remember { mutableStateOf(product.stateDescription) }
    var price by remember { mutableDoubleStateOf(product.price.amount) }
    var images by remember { mutableStateOf(product.images) }
    var selectedImages by remember { mutableStateOf(listOf<Uri>()) }
    val imageCount = selectedImages.size + images.size

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImages = selectedImages + uri
            }
        }
    )

    BoxWithConstraints (modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 5.dp)) {
        val textFieldWidth = constraints.maxWidth

        OutlinedTextField(
            value = condition.value,
            onValueChange = {},
            label = {
                Row {
                    Text(text = stringResource(id = R.string.uploaded_products_condition_label))
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
            enabled = !isLoading.value,
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
    OutlinedTextField(
        value = stateDescription,
        onValueChange = { stateDescription = it },
        label = {
            Row {
                Text(text = stringResource(id = R.string.uploaded_products_description_label))
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
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp).height(200.dp),
        enabled = !isLoading.value
    )

    OutlinedTextField(
        value = price.toString(),
        onValueChange = { price = it.toDouble() },
        label = {
            Row {
                Text(text = stringResource(id = R.string.uploaded_products_price_label))
                Text(text = "*", color = Color.Red)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        enabled = !isLoading.value,
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

    //Sezione delle immagini
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 10.dp)
    ) {
        Text(text = stringResource(id = R.string.uploaded_products_image_label))
        Text(text = "*", color = Color.Red)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 10.dp)
    ) {
        // Immagini già esistenti (nel database)
        images.forEachIndexed { index, image ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(image.photo).memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).error(R.drawable.error_card).build(),
                    contentDescription = "Product image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )

                IconButton(
                    onClick = {
                        if(!isLoading.value) {
                            coroutineScope.launch {
                                // Rimuovo l'immagine selezionata se non è l'unica immagine del db online
                                if(images.size == 1) {
                                    Toast.makeText(application, application.getString(R.string.uploaded_products_image_delete_error_unique), Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                isLoading.value = true
                                isDelete = true

                                val success = uploadedProductsViewModel.deleteImage(product.id.toString(), image.id.toString())

                                isLoading.value = false
                                isDelete = false

                                if(success) {
                                    images = images.filterIndexed { i, _ -> i != index }
                                    Toast.makeText(application, application.getString(R.string.uploaded_products_image_delete_success), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(application, application.getString(R.string.uploaded_products_image_delete_error), Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    FontAwesomeIcon(
                        unicode = "\uf2ed",
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Immagini selezionate dall'utente
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
                        if(!isLoading.value)
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

        // Bottone per aggiungere immagini
        if (imageCount < 5) {
            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                enabled = !isLoading.value
            ) {
                 Text(text = stringResource(id = R.string.uploaded_products_add_image), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }

    if(imageCount < 5)
        HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 10.dp))

    // Sezione dei bottoni
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 10.dp)
    ) {
        // Bottone per salvare
        Button(
            onClick = {
                coroutineScope.launch {
                    isSaving = true
                    isLoading.value = true
                    val success = uploadedProductsViewModel.updateProduct(product.id.toString(), stateDescription, condition, price, selectedImages)

                    if(success) {
                        Toast.makeText(application, application.getString(R.string.uploaded_products_modify_success), Toast.LENGTH_SHORT).show()
                        navHostController.popBackStack()
                    } else {
                        Toast.makeText(application, application.getString(R.string.uploaded_products_modify_error), Toast.LENGTH_SHORT).show()
                    }

                    isSaving = false
                    isLoading.value = false
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = (stateDescription.isNotEmpty() && stateDescription.length in 3..200) &&
                    (price > 0.01 && price < 500000) && (imageCount in 1..5) && !isLoading.value
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.uploaded_products_modify_save), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        // Bottone per annullare
        Button(
            onClick = {
                isCancel = true
                isLoading.value = true

                navHostController.popBackStack()

                isCancel = false
                isLoading.value = false
            },
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            enabled = !isLoading.value
        ) {
            if(isCancel) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            }
            else {
                Text(text = stringResource(id = R.string.uploaded_products_modify_cancel), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
            }
        }
    }
}