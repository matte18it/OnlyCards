package unical.enterpriceapplication.onlycards.ui.privateProfile

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.ProfileViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Componenti della schermata del profilo privato
@Composable
fun PrivateProfile(profileViewModel: ProfileViewModel, navHostController: NavHostController) {
    // Variabili
    val user by profileViewModel.user.collectAsState(null)  // Utente
    val userPhoto by profileViewModel.userImage.collectAsState("")  // Foto dell'utente
    val errorSnackbar = remember { SnackbarHostState() }    // Snackbar di errore
    val error by profileViewModel.error.collectAsState()   // Errore
    val isGlobalLoading = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        profileViewModel.getUser()
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
            if(user != null){
                Column(Modifier.verticalScroll(rememberScrollState(), enabled = !isGlobalLoading.value)) {
                    user?.let { it1 -> PhotoSection(it1, userPhoto.toString(), profileViewModel, isGlobalLoading) }
                    user?.let { it1 -> PrivateInfoSection(it1, profileViewModel, isGlobalLoading, onPublicProfileClick = {
                        navHostController.navigate("users/$it")
                    }) }
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
                        text = stringResource(id = R.string.profile_not_found),
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
fun PhotoSection(user: UserData, userPhoto: String, profileViewModel: ProfileViewModel, isGlobalLoading: MutableState<Boolean>) {
    val context = LocalContext.current  // Contesto
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Uri dell'immagine
    var isLoading by remember { mutableStateOf(false) } // Stato di caricamento
    val tooltipCondition = rememberTooltipState()   // Tooltip per la foto
    val coroutineScope = rememberCoroutineScope()   // CoroutineScope

    // Launcher per l'Intent di selezione immagine
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val realPath = getRealPathFromURI(context, it)

            realPath?.let { imagePath ->
                isLoading = true // Inizia il caricamento
                isGlobalLoading.value = true

                profileViewModel.viewModelScope.launch {
                    val result = profileViewModel.updateImage(imagePath, user.id.toString())
                    isLoading = false // Fine caricamento
                    isGlobalLoading.value = false

                    if (result) Toast.makeText(context, context.getString(R.string.user_image_profile_success), Toast.LENGTH_SHORT).show()
                    else Toast.makeText(context, context.getString(R.string.user_image_profile_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val imageModel = imageUri?.toString() ?: userPhoto.ifBlank { R.drawable.profile.toString() } // Modello immagine
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(imageModel)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.profile)
        .build()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(150.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.Gray, CircleShape)
                                .clickable {
                                    if(!isGlobalLoading.value)
                                        imagePickerLauncher.launch("image/*")
                                }
                        )
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                            tooltip = { PlainTooltip { Text(text = stringResource(R.string.user_modify_photo)) } },
                            state = tooltipCondition
                        ) {
                            FontAwesomeIcon(
                                unicode = "\uf05a",
                                fontSize = 25.sp,
                                modifier = Modifier.padding(5.dp).clickable(onClick = { coroutineScope.launch { tooltipCondition.show() } })
                            )
                        }
                    }
                }
            }

            Text(
                text = user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}
@Composable
fun PrivateInfoSection(user: UserData, profileViewModel: ProfileViewModel, isGlobalLoading: MutableState<Boolean>, onPublicProfileClick: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()  // CoroutineScope
    var currentTime by remember { mutableStateOf(getCurrentTime()) }    // Ora corrente
    var showModifyMenu by remember { mutableStateOf(false) }    // Mostra il menu di modifica
    var username by remember { mutableStateOf(user.username) }  // Username
    var email by remember { mutableStateOf(user.email) }    // Email
    var phoneNumber by remember { mutableStateOf(user.cellphoneNumber) }    // Numero di telefono
    var isLoading by remember { mutableStateOf(false) }
    var context = LocalContext.current

    // Aggiorna l'ora ogni secondo
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = getCurrentTime()
            delay(1000L)
        }
    }

    // Inizializzazione dei campi
    LaunchedEffect(user) {
        username = user.username
        email = user.email
        phoneNumber = user.cellphoneNumber
    }

    Column(
        modifier = Modifier.padding(10.dp)
    ) {
        Text(
            text = stringResource(id = R.string.user_title),
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
        )
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.user_username),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = " $username",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            FontAwesomeIcon(
                unicode = "\uf304",
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = {
                    if(!isGlobalLoading.value)
                        showModifyMenu = true
                })
            )
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.user_email),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = " $email",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            if(!user.oauthUser)
                FontAwesomeIcon(
                    unicode = "\uf304",
                    fontSize = 18.sp,
                    modifier = Modifier.clickable(onClick = {
                        if(!isGlobalLoading.value)
                            showModifyMenu = true
                    })
                )
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.user_phone),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = " $phoneNumber",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            FontAwesomeIcon(
                unicode = "\uf304",
                fontSize = 18.sp,
                modifier = Modifier.clickable(onClick = {
                    if(!isGlobalLoading.value)
                        showModifyMenu = true
                })
            )
        }
        Spacer(modifier = Modifier.padding(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.user_current_date),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Text(
                text = " $currentTime",
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
        if(user.roles.contains(UserData.Role("ROLE_BUYER")) || user.roles.contains(UserData.Role("ROLE_SELLER")) ){
        Spacer(modifier = Modifier.padding(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                onPublicProfileClick(username)

            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(id = R.string.user_public_profile_show))
            }
        }
    }}


    if(showModifyMenu) {
        AlertDialog(
            onDismissRequest = { showModifyMenu = false },
            title = { Text(text = stringResource(id = R.string.user_modify_informations)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.user_label_username))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf007",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.user_label_email))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf0e0",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.padding(5.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            if (isPhoneNumber(it))
                                phoneNumber = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.user_label_phone))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf095",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !isLoading
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            isGlobalLoading.value = true
                            val success = profileViewModel.updateUser(UserData(user.id, email, username, phoneNumber, user.blocked, user.addresses, user.oauthUser, user.roles))

                            if (success) {
                                showModifyMenu = false
                                Toast.makeText(context, context.getString(R.string.user_modify_success), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, context.getString(R.string.user_modify_error), Toast.LENGTH_SHORT).show()
                            }

                            isGlobalLoading.value = false
                            isLoading = false
                        }
                    },
                    enabled = !isLoading && (username.length in 3..20 && (isValidEmail(email) && !user.oauthUser) && (isPhoneNumber(phoneNumber) && phoneNumber.length == 10))
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = stringResource(id = R.string.user_modify_save))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Ristabilisco i valori originali
                        username = user.username
                        email = user.email
                        phoneNumber = user.cellphoneNumber

                        showModifyMenu = false
                    },
                    enabled = !isLoading
                ) {
                    Text(text = stringResource(id = R.string.user_modify_close))
                }
            }
        )
    }
}

// Funzioni di supporto
private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
    val cursor = context.contentResolver.query(contentUri, null, null, null, null)
    cursor?.use {
        val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
        if (it.moveToFirst() && index != -1) {
            return it.getString(index)
        }
    }
    return null
}   // Metodo per ottenere il percorso reale dell'immagine
fun getCurrentTime(): String {
    val currentTime = LocalTime.now(ZoneId.of("Europe/Rome"))
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return currentTime.format(formatter) + " (Europe/Rome)"
}   // Metodo per ottenere l'ora corrente
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}   // Metodo per verificare la validità dell'email
fun isPhoneNumber(phoneNumber: String): Boolean {
    return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
}   // Metodo per verificare la validità del numero di telefono