package unical.enterpriceapplication.onlycards.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.ProductTypeEditViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddProductTypeScreen(
    navHostController: NavHostController,
    productTypeEditViewModel: ProductTypeEditViewModel
) {
    val isLoading by productTypeEditViewModel.isLoading.collectAsState()
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var game by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var features by remember { mutableStateOf(mutableListOf<FeatureData>()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val regex = "^[a-zA-Z0-9 -]*$".toRegex()

    val isNameValid = name.length >= 3 && regex.matches(name)
    val isLanguageValid = language.length >= 2 && regex.matches(language)
    val isGameValid = game.length >= 3
    val isTypeValid = type.length >= 3 && regex.matches(type)
    val isImageValid = selectedImageUri != null
    val isFormValid = isNameValid && isLanguageValid && isGameValid && isTypeValid && isImageValid

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Ricorda lo scope della coroutine

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> selectedImageUri = uri }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.add_product_type)) },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ProductTypeFormPage(
                name = name,
                onNameChange = { name = it },
                language = language,
                onLanguageChange = { language = it },
                game = game,
                onGameChange = { game = it },
                type = type,
                onTypeChange = { type = it },
                features = features,
                onAddFeature = { featureName, featureValue ->
                    if (featureName.length >= 2 && regex.matches(featureName) && !features.any { it.name == featureName }) {
                        features.add(FeatureData(name = featureName, value = featureValue))
                    }
                },
                onDeleteFeature = { feature -> features.remove(feature) },
                onSave = {
                    scope.launch {
                        if (isFormValid) {
                            productTypeEditViewModel.addNewProductType(
                                name = name,
                                language = language,
                                game = game,
                                type = type,
                                features = features,
                                imageUri = selectedImageUri,
                                onSuccess = {
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
                selectedImageUri = selectedImageUri,
                isImageValid = isImageValid,
                paddingValues = paddingValues,
                isFormValid = isFormValid,
            )
        }
    }

    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            Toast.makeText(
                context,
                context.getString(R.string.product_type_add_success),
                Toast.LENGTH_SHORT
            ).show()
            showSuccessMessage = false
            navHostController.popBackStack()
        }
    }

    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            Toast.makeText(
                context,
                context.getString(R.string.product_type_add_error),
                Toast.LENGTH_SHORT
            ).show()
            showErrorMessage = false
        }
    }
}


@Composable
fun ProductTypeFormPage(
    name: String,
    onNameChange: (String) -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
    game: String,
    onGameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    features: List<FeatureData>,
    onAddFeature: (String, String) -> Unit,
    onDeleteFeature: (FeatureData) -> Unit,
    onSave: () -> Unit,
    onSelectImage: () -> Unit,
    selectedImageUri: Uri?,
    isImageValid: Boolean,
    paddingValues: PaddingValues,
    isFormValid: Boolean,
) {
    var nameTouched by remember { mutableStateOf(false) }
    var languageTouched by remember { mutableStateOf(false) }
    var gameTouched by remember { mutableStateOf(false) }
    var typeTouched by remember { mutableStateOf(false) }

    var newFeatureName by remember { mutableStateOf("") }
    var newFeatureValue by remember { mutableStateOf("") }
    var newFeatureNameTouched by remember { mutableStateOf(false) }
    var newFeatureValueTouched by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                onNameChange(it)
                nameTouched = true
            },
            label = { Text(stringResource(id = R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isNameValid && nameTouched
        )
        if (nameTouched) {
            if (isNameTooShort) {
                Text(stringResource(id = R.string.product_name_error_short), color = MaterialTheme.colorScheme.error)
            } else if (isNameInvalidCharacters) {
                Text(stringResource(id = R.string.product_name_error_invalid), color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = language,
            onValueChange = {
                onLanguageChange(it)
                languageTouched = true
            },
            label = { Text(stringResource(id = R.string.language)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isLanguageValid && languageTouched
        )
        if (languageTouched) {
            if (isLanguageTooShort) {
                Text(stringResource(id = R.string.product_language_error_short), color = MaterialTheme.colorScheme.error)
            } else if (isLanguageInvalidCharacters) {
                Text(stringResource(id = R.string.product_language_error_invalid), color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = game,
            onValueChange = {
                onGameChange(it)
                gameTouched = true
            },
            label = { Text(stringResource(id = R.string.game)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isGameValid && gameTouched
        )
        if (gameTouched) {
            if (isGameTooShort) {
                Text(stringResource(id = R.string.product_game_error_short), color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = type,
            onValueChange = {
                onTypeChange(it)
                typeTouched = true
            },
            label = { Text(stringResource(id = R.string.type)) },
            modifier = Modifier.fillMaxWidth(),
            isError = !isTypeValid && typeTouched
        )
        if (typeTouched) {
            if (isTypeTooShort) {
                Text(stringResource(id = R.string.product_type_error_short), color = MaterialTheme.colorScheme.error)
            } else if (isTypeInvalidCharacters) {
                Text(stringResource(id = R.string.product_type_error_invalid), color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        selectedImageUri?.let { uri ->
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = stringResource(id = R.string.product_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (!isImageValid) {
            Text(stringResource(id = R.string.image_is_required), color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = onSelectImage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedImageUri != null) stringResource(id = R.string.change_image) else stringResource(id = R.string.select_image))
        }

        Spacer(modifier = Modifier.height(16.dp))

        features.forEach { feature ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${feature.name}: ${feature.value}")
                IconButton(onClick = { onDeleteFeature(feature) }) {
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
            onClick = onSave,
            enabled = isFormValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(id = R.string.save_product_type))
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(stringResource(id = R.string.add_new_feature)) },
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
                            newFeatureName = ""
                            newFeatureValue = ""
                            showDialog = false
                            newFeatureNameTouched = false
                            newFeatureValueTouched = false
                        },
                        enabled = newFeatureName.length >= 2 && regex.matches(newFeatureName) && !features.any { it.name == newFeatureName }
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
}
