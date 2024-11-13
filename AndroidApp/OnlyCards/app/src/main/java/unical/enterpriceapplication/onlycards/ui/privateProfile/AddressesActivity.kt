package unical.enterpriceapplication.onlycards.ui.privateProfile

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.ToTheTopListFloatingButton
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.AddressesViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import java.util.UUID

// Componenti
@Composable
fun Addresses(navHostController: NavHostController, viewModel: AddressesViewModel) {
    // Variabili
    val addresses by viewModel.addresses.collectAsState(null)    // Indirizzi
    val scrollState = rememberScrollState() // stato di scorrimento
    val coroutineScope = rememberCoroutineScope()   // CoroutineScope
    var showFab by remember { mutableStateOf(false) }    // Mostra il pulsante
    var showAddAddress by remember { mutableStateOf(false) }    // Mostra il pulsante per aggiungere un indirizzo
    val error by viewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val globalLoading = remember { mutableStateOf(false) }    // Stato di caricamento

    LaunchedEffect(scrollState.value) {
        viewModel.getAddresses()

        showFab = scrollState.value > 100
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
        },
        floatingActionButton = {
            if (showFab)
                ToTheTopListFloatingButton { coroutineScope.launch { scrollState.animateScrollTo(0) } }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (addresses != null) {
                Column(Modifier.verticalScroll(state = scrollState, enabled = !globalLoading.value)) {
                    Text(
                        text = stringResource(id = R.string.address_title),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                    addresses?.forEach { address ->
                        AddressCard(address, viewModel, navHostController, globalLoading)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showAddAddress = true
                        },
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp).fillMaxWidth().height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        enabled = !globalLoading.value
                    ) {
                        Text(
                            text = stringResource(id = R.string.address_add),
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                        text = stringResource(id = R.string.address_no_found),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AlertAddress(
            showAddAddress = showAddAddress,
            onDismiss = { showAddAddress = false },
            type = "add",
            data = listOf("", "", "", "", "", "", "", "false", "false", ""),
            viewModel = viewModel,
            navHostController = navHostController,
            globalLoading = globalLoading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressCard(address: UserData.AddressData, viewModel: AddressesViewModel, navHostController: NavHostController, globalLoading: MutableState<Boolean>) {
    val tooltipCondition = rememberTooltipState()   // Tooltip per l'indirizzo
    val coroutineScope = rememberCoroutineScope()   // CoroutineScope
    var showAddAddress by remember { mutableStateOf(false) }    // Mostra il pulsante per aggiungere un indirizzo
    val context = LocalContext.current  // Contesto
    val isAdd = remember { mutableStateOf(false) }    // Aggiungi indirizzo

    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row {
                Text(
                    text = stringResource(id = R.string.address_domicile),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                FontAwesomeIcon(
                    unicode = "\uf044",
                    fontSize = 20.sp,
                    modifier = Modifier.clickable(
                        onClick = {
                            showAddAddress = true
                        },
                        enabled = !globalLoading.value
                    )
                )

                if(!address.defaultAddress) {
                    FontAwesomeIcon(
                        unicode = "\uf1f8",
                        fontSize = 20.sp,
                        modifier = Modifier.padding(start = 10.dp).clickable(
                            onClick = {
                                if (!globalLoading.value) {
                                    coroutineScope.launch {
                                        globalLoading.value = true
                                        if(viewModel.deleteAddress(address.id.toString())) {
                                            Toast.makeText(context, context.getString(R.string.address_delete_success), Toast.LENGTH_SHORT).show()
                                            navHostController.navigate("addresses") {
                                                popUpTo("addresses") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                        }
                                        else
                                            Toast.makeText(context, context.getString(R.string.address_delete_error), Toast.LENGTH_SHORT).show()
                                        globalLoading.value = false
                                    }
                                }
                            }
                        )
                    )
                }
            }

            Row {
                Text(
                    text = address.name + " ",
                    fontSize = 15.sp
                )
                Text(
                    text = address.surname,
                    fontSize = 15.sp
                )
            }

            Text(
                text = address.street,
                fontSize = 15.sp
            )

            Row {
                Text(
                    text = address.zip + " ",
                    fontSize = 15.sp
                )
                Text(
                    text = address.city + " ",
                    fontSize = 15.sp
                )
                Text(
                    text = address.state,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if(address.defaultAddress)
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(text = stringResource(R.string.address_default)) } },
                    state = tooltipCondition
                ) {
                    FontAwesomeIcon(
                        unicode = "\uf0d1",
                        fontSize = 20.sp,
                        modifier = Modifier.clickable(
                            onClick = {
                                coroutineScope.launch { tooltipCondition.show() }
                            }
                        )
                    )
                }
        }
    }

    if (showAddAddress)
        AlertAddress(
            showAddAddress = showAddAddress,
            onDismiss = { showAddAddress = false },
            type = "modify",
            data = listOf(address.name, address.surname, address.telephoneNumber, address.street, address.zip, address.city, address.state, address.defaultAddress.toString(), address.weekendDelivery.toString(), address.id.toString()),
            viewModel = viewModel,
            navHostController = navHostController,
            globalLoading = globalLoading
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertAddress(showAddAddress: Boolean, onDismiss: () -> Unit, type: String, data: List<String>, viewModel: AddressesViewModel, navHostController: NavHostController, globalLoading: MutableState<Boolean>) {
    if (showAddAddress) {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()  // CoroutineScope
        var newName by remember { mutableStateOf(data[0]) }
        var newSurname by remember { mutableStateOf(data[1]) }
        var newPhone by remember { mutableStateOf(data[2]) }
        var newStreet by remember { mutableStateOf(data[3]) }
        var newCap by remember { mutableStateOf(data[4]) }
        var newCity by remember { mutableStateOf(data[5]) }
        var newState by remember { mutableStateOf(data[6]) }
        var newDefaultAddress by remember { mutableStateOf(data[7].toBoolean()) }
        var newWeekendDelivery by remember { mutableStateOf(data[8].toBoolean()) }
        var filteredCities by remember { mutableStateOf(emptyList<String>()) }
        var filteredState by remember { mutableStateOf(emptyList<String>()) }
        val state = readFromCSV(LocalContext.current, R.raw.nations)
        val cities = readFromCSV(LocalContext.current, R.raw.cities)
        var expandedCity by remember { mutableStateOf(false) }
        var expandedState by remember { mutableStateOf(false) }
        var isSaving by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { if(type == "add") Text(text = stringResource(id = R.string.address_add)) else Text(text = stringResource(id = R.string.address_modify)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = {
                            newName = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.address_label_name))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf007",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !globalLoading.value
                    )
                    OutlinedTextField(
                        value = newSurname,
                        onValueChange = {
                            newSurname = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.address_label_surname))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf007",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !globalLoading.value
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = {
                            newPhone = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.address_label_phone))
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
                        enabled = !globalLoading.value
                    )
                    OutlinedTextField(
                        value = newStreet,
                        onValueChange = {
                            newStreet = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.address_label_street))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf018",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !globalLoading.value
                    )
                    OutlinedTextField(
                        value = newCap,
                        onValueChange = {
                            newCap = it
                        },
                        label = {
                            Row {
                                Text(text = stringResource(id = R.string.address_label_cap))
                                Text(text = "*", color = Color.Red)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            FontAwesomeIcon(
                                unicode = "\uf3c5",
                                fontSize = 18.sp
                            )
                        },
                        enabled = !globalLoading.value
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedCity,
                        onExpandedChange = {
                            expandedCity = !expandedCity
                        }
                    ) {
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = {
                                newCity = it
                                filteredCities = if (it.isNotEmpty()) {
                                    cities.filter { city -> city.contains(it, ignoreCase = true) }
                                } else {
                                    emptyList()
                                }
                                expandedCity = filteredCities.isNotEmpty()
                            },
                            label = {
                                Row {
                                    Text(text = stringResource(id = R.string.address_label_city))
                                    Text(text = "*", color = Color.Red)
                                }
                            },
                            leadingIcon = {
                                FontAwesomeIcon(
                                    unicode = "\uf64f",
                                    fontSize = 18.sp
                                )
                            },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable),
                            enabled = !globalLoading.value
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCity,
                            onDismissRequest = { expandedCity = false }
                        ) {
                            filteredCities.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(text = city) },
                                    onClick = {
                                        newCity = city
                                        expandedCity = false
                                    }
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedState,
                        onExpandedChange = {
                            expandedState = !expandedState
                        }
                    ) {
                        OutlinedTextField(
                            value = newState,
                            onValueChange = {
                                newState = it
                                filteredState = if (it.isNotEmpty()) {
                                    state.filter { state -> state.contains(it, ignoreCase = true) }
                                } else {
                                    emptyList()
                                }
                                expandedState = filteredState.isNotEmpty()
                            },
                            label = {
                                Row {
                                    Text(text = stringResource(id = R.string.address_label_state))
                                    Text(text = "*", color = Color.Red)
                                }
                            },
                            leadingIcon = {
                                FontAwesomeIcon(
                                    unicode = "\uf024",
                                    fontSize = 18.sp
                                )
                            },
                            modifier = Modifier.menuAnchor(type = MenuAnchorType.PrimaryEditable),
                            enabled = !globalLoading.value
                        )
                        ExposedDropdownMenu(
                            expanded = expandedState,
                            onDismissRequest = { expandedState = false }
                        ) {
                            filteredState.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(text = state) },
                                    onClick = {
                                        newState = state
                                        expandedState = false
                                    }
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = newDefaultAddress,
                            onCheckedChange = {
                                newDefaultAddress = it
                            },
                            enabled = !globalLoading.value
                        )
                        Text(text = " " + stringResource(id = R.string.address_label_default), fontSize = 15.sp)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = newWeekendDelivery,
                            onCheckedChange = {
                                newWeekendDelivery = it
                            },
                            enabled = !globalLoading.value
                        )
                        Text(text = " " + stringResource(id = R.string.address_label_weekend), fontSize = 15.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                       coroutineScope.launch {
                           globalLoading.value = true
                           isSaving = true

                           if(type == "add") {
                               if(viewModel.addAddress(UserData.AddressData(UUID.randomUUID(), newState, newCity, newStreet, newCap, newName, newSurname, newPhone, newDefaultAddress, newWeekendDelivery))) {
                                   // Notifica di successo e ricarico la pagina
                                   Toast.makeText(context, context.getString(R.string.address_label_success), Toast.LENGTH_SHORT).show()
                                   onDismiss()
                                   navHostController.navigate("addresses") {
                                       popUpTo("addresses") { inclusive = true }
                                       launchSingleTop = true
                                   }
                               }
                               else
                                   Toast.makeText(context, context.getString(R.string.address_label_save_error), Toast.LENGTH_SHORT).show()
                           }
                           else {
                               if(viewModel.modifyAddress(UserData.AddressData(UUID.fromString(data[9]), newState, newCity, newStreet, newCap, newName, newSurname, newPhone, newDefaultAddress, newWeekendDelivery))) {
                                   // Notifica di successo e ricarico la pagina
                                   Toast.makeText(context, context.getString(R.string.address_label_success), Toast.LENGTH_SHORT).show()
                                   onDismiss()
                                   navHostController.navigate("addresses") {
                                       popUpTo("addresses") { inclusive = true }
                                       launchSingleTop = true
                                   }
                               }
                               else
                                   Toast.makeText(context, context.getString(R.string.address_label_save_error), Toast.LENGTH_SHORT).show()
                           }

                           globalLoading.value = false
                           isSaving = false
                       }
                    },
                    enabled = (
                            (newName.isNotEmpty() && newName.length in 3..20 && Regex("^[a-zA-Z]+\$").matches(newName)) &&
                            (newSurname.isNotEmpty() && newSurname.length in 3..20 && Regex("^[a-zA-Z]+\$").matches(newSurname)) &&
                            (newPhone.isNotEmpty() && newPhone.length == 10 && Regex("^[0-9]+\$").matches(newPhone)) &&
                            (newStreet.isNotEmpty() && newStreet.length in 3..50) &&
                            (newCap.isNotEmpty() && newCap.length == 5 && Regex("^[0-9]{5}\$").matches(newCap)) &&
                            (newCity.isNotEmpty() && cities.contains(newCity)) &&
                            (newState.isNotEmpty() && state.contains(newState))
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text(text = stringResource(id = R.string.address_save))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { onDismiss() }, enabled = !globalLoading.value) {
                    Text(text = stringResource(id = R.string.address_close))
                }
            }
        )
    }
}

// Funzioni di supporto
fun readFromCSV(context: Context, file: Int): List<String> {
    val data = mutableListOf<String>()
    val inputStream = context.resources.openRawResource(file)
    inputStream.bufferedReader().useLines { lines ->
        lines.forEach { line ->
            val element = line.split(",")[0]
            data.add(element)
        }
    }
    return data
}   // Funzione per leggere da un file CSV
