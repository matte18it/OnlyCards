package unical.enterpriceapplication.onlycards.ui.privateProfile.seller

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.GameViewModel
import unical.enterpriceapplication.onlycards.viewmodels.ProductRequestViewModel

@Composable
fun ProductRequest(gameViewModel: GameViewModel, productRequestViewModel: ProductRequestViewModel) {
    // Variabili
    val isGlobalLoading = remember { mutableStateOf(false) } // Stato di caricamento
    val games = gameViewModel.games.collectAsState()  // giochi
    val scrollState = rememberScrollState() // stato di scorrimento
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val error by productRequestViewModel.error.collectAsState()   // Errore

    LaunchedEffect(key1 = Unit) {
        scrollState.scrollTo(0)

        gameViewModel.getGames()
    }
    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
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
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(Modifier.verticalScroll(state = scrollState, enabled = !isGlobalLoading.value).padding(15.dp)) {
                Text(
                    text = stringResource(id = R.string.request_product_title),
                    modifier = Modifier.padding(start = 5.dp, top = 15.dp),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(bottom = 5.dp))

                // Form di richiesta prodotto
                RequestProductForm(games.value, productRequestViewModel, isGlobalLoading)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestProductForm(games: Map<String, String>, productRequestViewModel: ProductRequestViewModel, isGlobalLoading: MutableState<Boolean>) {
    // variabili per il form
    val gamesList = games.values.toList()
    var gameProduct by remember { mutableStateOf("") }  // Gioco
    var nameProduct by remember { mutableStateOf("") }  // Nome della carta
    var note by remember { mutableStateOf("") } // Note
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) } // Uri dell'immagine

    // variabili di stato
    var filteredGames by remember { mutableStateOf(gamesList) }
    var expandedGame by remember { mutableStateOf(false) }
    var isReset by remember { mutableStateOf(false) } // Stato di caricamento
    var isSendingEmail by remember { mutableStateOf(false) } // Stato di caricamento
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                selectedImageUri = it // Aggiorna la variabile di stato
            }
        }
    )

    // Sezione per scegliere il gioco
    ExposedDropdownMenuBox(
        expanded = expandedGame,
        onExpandedChange = {
            expandedGame = !expandedGame
        }
    ) {
        OutlinedTextField(
            value = gameProduct,
            onValueChange = {
                gameProduct = it
                filteredGames = if (it.isNotEmpty()) {
                    gamesList.filter { game -> game.contains(it, ignoreCase = true) }
                } else {
                    emptyList()
                }
                expandedGame = filteredGames.isNotEmpty()
            },
            label = {
                Row {
                    Text(text = stringResource(id = R.string.request_product_game))
                    Text(text = "*", color = Color.Red)
                }
            },
            leadingIcon = {
                FontAwesomeIcon(
                    unicode = "\uf11b",
                    fontSize = 18.sp
                )
            },
            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable).fillMaxWidth(),
            enabled = !isGlobalLoading.value
        )
        ExposedDropdownMenu(
            expanded = expandedGame,
            onDismissRequest = { expandedGame = false }
        ) {
            filteredGames.forEach { game ->
                DropdownMenuItem(
                    text = { Text(text = game) },
                    onClick = {
                        gameProduct = game
                        expandedGame = false
                    }
                )
            }
        }
    }

    // Sezione per il nome della carta
    OutlinedTextField(
        value = nameProduct,
        onValueChange = { nameProduct = it },
        label = {
            Row {
                Text(text = stringResource(id = R.string.request_product_name))
                Text(text = "*", color = Color.Red)
            }
        },
        leadingIcon = {
            FontAwesomeIcon(
                unicode = "\uf02b",
                fontSize = 18.sp
            )
        },
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        enabled = !isGlobalLoading.value
    )

    // Sezione per le note
    OutlinedTextField(
        value = note,
        onValueChange = { note = it },
        label = {
            Text(text = stringResource(id = R.string.request_product_note))
        },
        leadingIcon = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopStart
            ) {
                FontAwesomeIcon(
                    unicode = "\uf249",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp).height(200.dp),
        enabled = !isGlobalLoading.value
    )

    // Sezione per l'immagine
    Row(modifier = Modifier.padding(top = 10.dp)) {
        Text(text = stringResource(id = R.string.request_product_image_title))
        Text(text = "*", color = Color.Red)
    }
    if(selectedImageUri == null) {
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
                    text = stringResource(id = R.string.request_product_image_load),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = stringResource(id = R.string.request_product_image_details),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit
            )

            IconButton(
                onClick = {
                    if(!isGlobalLoading.value) {
                        isGlobalLoading.value = true
                        imagePickerLauncher.launch("image/*")
                        isGlobalLoading.value = false
                    }
                },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                FontAwesomeIcon(
                    unicode = "\uf044",
                    fontSize = 20.sp
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        // Bottone per inviare l'email
        Button(
            onClick = {
                coroutineScope.launch {
                    isGlobalLoading.value = true
                    isSendingEmail = true
                    val success = selectedImageUri?.let {
                        productRequestViewModel.sendRequestProduct(gameProduct, nameProduct, note, it)
                    }

                    if(success == true) {
                        gameProduct = ""
                        nameProduct = ""
                        note = ""
                        selectedImageUri = null
                        Toast.makeText(application, application.getString(R.string.request_product_send_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(application, application.getString(R.string.request_product_send_error), Toast.LENGTH_SHORT).show()
                    }

                    isGlobalLoading.value = false
                    isSendingEmail = false
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = (gameProduct.isNotEmpty() && gamesList.contains(gameProduct)) &&
                      (nameProduct.isNotEmpty() && nameProduct.length in 3..100) &&
                      (note.length in 0..500) &&
                       selectedImageUri != null && !isGlobalLoading.value
        ) {
            if(isSendingEmail) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.request_product_send), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        // Bottone per resettare il form
        Button(
            onClick = {
                coroutineScope.launch {
                    isReset = true
                    isGlobalLoading.value = true
                    gameProduct = ""
                    nameProduct = ""
                    note = ""
                    selectedImageUri = null
                    isReset = false
                    isGlobalLoading.value = false
                }
            },
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            enabled = !isGlobalLoading.value
        ) {
            if(isReset) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.request_product_reset), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
            }
        }
    }
}
