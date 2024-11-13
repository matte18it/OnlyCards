import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.ProductTypeEditViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.util.UUID

@Composable
fun ProductTypeEditScreen(
    productTypeId: String?,
    productTypeEditViewModel: ProductTypeEditViewModel
) {
    val productTypeData by productTypeEditViewModel.productTypeData.collectAsState()
    val isLoading by productTypeEditViewModel.isLoading.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val productTypeUuid = remember(productTypeId) {
        productTypeId?.let {
            try {
                UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                productTypeEditViewModel.updateSelectedImagePath(it)
            }
        }
    )

    // Ottieni il CoroutineScope per eseguire le chiamate in modo asincrono
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(productTypeUuid) {
        productTypeUuid?.let { productTypeEditViewModel.getProductType(it) }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        productTypeData?.let { productType ->
            ProductTypeEditPage(
                productTypeData = productType,
                onAddFeature = { featureName, featureValue ->
                    productTypeEditViewModel.addFeatureTemp(featureName, featureValue)
                },
                onDeleteFeature = { feature ->
                    productTypeEditViewModel.deleteFeatureTemp(feature)
                },
                onSave = { updatedFeatures ->
                    productTypeUuid?.let {
                        // Usa il coroutineScope per eseguire la funzione `suspend` dentro una coroutine
                        coroutineScope.launch {
                            productTypeEditViewModel.saveProductType(
                                it,
                                updatedFeatures,
                                onSuccess = {
                                    selectedImageUri = null
                                    showSuccessMessage = true
                                },
                                onError = {
                                    showErrorMessage = true
                                }
                            )
                        }
                    }
                },
                onSelectImage = { imagePickerLauncher.launch("image/*") },
                selectedImageUri = selectedImageUri
            )
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            Toast.makeText(
                context,
                context.getString(R.string.save_success),
                Toast.LENGTH_SHORT
            ).show()
            showSuccessMessage = false
        }
    }

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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTypeEditPage(
    productTypeData: ProductTypeData,
    onAddFeature: (String, String) -> Unit,
    onDeleteFeature: (FeatureData) -> Unit,
    onSave: (List<FeatureData>) -> Unit,
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?
) {
    var name by remember { mutableStateOf(productTypeData.name) }
    var language by remember { mutableStateOf(productTypeData.language) }
    var game by remember { mutableStateOf(productTypeData.game) }
    var type by remember { mutableStateOf(productTypeData.type) }
    var features by remember { mutableStateOf(productTypeData.features.toMutableList()) }

    var showDialog by remember { mutableStateOf(false) }
    var newFeatureName by remember { mutableStateOf("") }
    var newFeatureValue by remember { mutableStateOf("") }
    var newFeatureNameTouched by remember { mutableStateOf(false) }
    var newFeatureValueTouched by remember { mutableStateOf(false) }
    val regex = "^[a-zA-Z0-9 -]*$".toRegex()

    val isNameTooShort = name.length < 3
    val isNameInvalidCharacters = !regex.matches(name)
    val isNameValid = !isNameTooShort && !isNameInvalidCharacters

    val isLanguageTooShort = language.length < 2
    val isLanguageInvalidCharacters = !regex.matches(language)
    val isLanguageValid = !isLanguageTooShort && !isLanguageInvalidCharacters

    val isGameTooShort = game.length < 3
    val isGameValid = !isGameTooShort

    val isTypeTooShort = type.length < 3
    val isTypeInvalidCharacters = !regex.matches(type)
    val isTypeValid = !isTypeTooShort && !isTypeInvalidCharacters

    val isFormValid = isNameValid && isLanguageValid && isGameValid && isTypeValid

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isNameValid
        )
        if (isNameTooShort) {
            Text(stringResource(id = R.string.product_name_error_short), color = MaterialTheme.colorScheme.error)
        } else if (isNameInvalidCharacters) {
            Text(stringResource(id = R.string.product_name_error_invalid), color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = language,
            onValueChange = { language = it },
            label = { Text(stringResource(id = R.string.language)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isLanguageValid
        )
        if (isLanguageTooShort) {
            Text(stringResource(id = R.string.product_language_error_short), color = MaterialTheme.colorScheme.error)
        } else if (isLanguageInvalidCharacters) {
            Text(stringResource(id = R.string.product_language_error_invalid), color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = game,
            onValueChange = { game = it },
            label = { Text(stringResource(id = R.string.game)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isGameValid
        )
        if (isGameTooShort) {
            Text(stringResource(id = R.string.product_game_error_short), color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text(stringResource(id = R.string.type)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isTypeValid
        )
        if (isTypeTooShort) {
            Text(stringResource(id = R.string.product_type_error_short), color = MaterialTheme.colorScheme.error)
        } else if (isTypeInvalidCharacters) {
            Text(stringResource(id = R.string.product_type_error_invalid), color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            // Visualizza l'immagine selezionata dall'utente
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = stringResource(id = R.string.product_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
        } ?: run {
            // Se non c'è un'immagine selezionata, visualizza l'immagine esistente (se presente)
            Image(
                painter = rememberAsyncImagePainter(model = productTypeData.photo),
                contentDescription = stringResource(id = R.string.product_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
        }

        Button(
            onClick = {
                onSelectImage()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.change_image))
        }

        Spacer(modifier = Modifier.height(16.dp))

        features.forEach { feature ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${feature.name}: ${feature.value}")
                IconButton(onClick = {
                    features = features.filter { it != feature }.toMutableList()
                    onDeleteFeature(feature)
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.add_feature))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                onSave(features)
            },
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.save_changes))
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = stringResource(id = R.string.add_new_feature)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFeatureName,
                        onValueChange = {
                            newFeatureName = it
                            newFeatureNameTouched = true
                        },
                        label = { Text(stringResource(id = R.string.feature_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = (newFeatureName.length < 2 || !regex.matches(newFeatureName) || features.any { it.name == newFeatureName }) && newFeatureNameTouched
                    )
                    if (newFeatureNameTouched && newFeatureName.length < 2) {
                        Text(stringResource(id = R.string.feature_name_error), color = MaterialTheme.colorScheme.error)
                    } else if (newFeatureNameTouched && !regex.matches(newFeatureName)) {
                        Text(stringResource(id = R.string.feature_name_invalid), color = MaterialTheme.colorScheme.error)
                    } else if (newFeatureNameTouched && features.any { it.name == newFeatureName }) {
                        Text(stringResource(id = R.string.feature_name_exists), color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newFeatureValue,
                        onValueChange = {
                            newFeatureValue = it
                            newFeatureValueTouched = true
                        },
                        label = { Text(stringResource(id = R.string.feature_value)) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = (newFeatureValue.length < 2 || !regex.matches(newFeatureValue)) && newFeatureValueTouched
                    )
                    if (newFeatureValueTouched && newFeatureValue.length < 2) {
                        Text(stringResource(id = R.string.feature_value_error), color = MaterialTheme.colorScheme.error)
                    } else if (newFeatureValueTouched && !regex.matches(newFeatureValue)) {
                        Text(stringResource(id = R.string.feature_value_invalid), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAddFeature(newFeatureName, newFeatureValue)
                        features = features.toMutableList().apply {
                            add(FeatureData(name = newFeatureName, value = newFeatureValue))
                        }
                        newFeatureName = ""
                        newFeatureValue = ""
                        showDialog = false
                        newFeatureNameTouched = false
                        newFeatureValueTouched = false
                    },
                    enabled = newFeatureName.length >= 2 && regex.matches(newFeatureName) && !features.any { it.name == newFeatureName } &&
                            newFeatureValue.length >= 2 && regex.matches(newFeatureValue)
                ) {
                    Text(stringResource(id = R.string.add))
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    newFeatureNameTouched = false
                    newFeatureValueTouched = false
                }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
}