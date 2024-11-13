package unical.enterpriceapplication.onlycards.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.UserWishlist
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.SingleWishlistViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.SingleUserWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.SingleWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistData
import java.time.format.DateTimeFormatter
import java.util.UUID






@Composable
fun WishlistSharingOptionScreen(singleWishlistViewModel: SingleWishlistViewModel, navHostController: NavHostController, currentUser: State<AuthUser?>, isTokenOrPublic:Boolean) {
    val wishlistData by singleWishlistViewModel.wishlistData.collectAsState(Wishlist(UUID.randomUUID(), stringResource(R.string.loading), isPublic = false))
    val wishlist by singleWishlistViewModel.wishlistDataOnline.collectAsState()
    val accountData by singleWishlistViewModel.wishlistAccounts.collectAsState(listOf())
    val loading by singleWishlistViewModel.isLoading.collectAsState()
    val error by singleWishlistViewModel.error.collectAsState()
    val success by singleWishlistViewModel.success.collectAsState()
    val isOwner  by singleWishlistViewModel.isOwner.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val openAlertDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(  snackbarHost = { SnackbarHost(snackbarHostState) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            containerColor = if (error!=null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, // Change color based on error
            contentColor = if (error==null) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary // Corresponding content color
        )
    }}) { values ->
        Box(modifier = Modifier.padding(values).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LaunchedEffect(error){
                error?.let { error ->
                    snackbarHostState.showSnackbar(error.message)
                    if(error.code==401){
                        navHostController.navigate("account")
                    }
                }

            }
            LaunchedEffect(success){
                success?.let {
                    snackbarHostState.showSnackbar(it)

                }

            }
            if(openAlertDialog.value){
                WishlistAddNewUserDialog(onDismiss = {openAlertDialog.value=false}, onConfirm = {username -> kotlin.run {  openAlertDialog.value=false;
                    singleWishlistViewModel.addUserToWishlist(wishlistData.id, username);


                }})
            }
            if(loading){
                CircularLoadingIndicator()
            } else {
                if(!isTokenOrPublic) {
                    UsersListData(wishlistData = wishlistData,
                        isOwner = isOwner,
                        currentUser = currentUser.value?.id,
                        onAddUserClick = { openAlertDialog.value = true },
                        onDeleteUser = { userId: UUID ->
                            kotlin.run {
                                singleWishlistViewModel.deleteUserFromWishlist(
                                    wishlistData.id,
                                    userId
                                )

                            }
                        },
                        accountsData = accountData,
                        onGeneratingToken = { singleWishlistViewModel.generateToken(wishlistData.id) },
                        onDeleteToken = {
                            wishlistData.token?.let {
                                singleWishlistViewModel.deleteToken(
                                    wishlistData.id,
                                    it
                                )
                            }
                        })
                }else{
                    UsersList(wishlistData = wishlist)

                }

        }

    }

}}

@Composable
fun UsersListData(
    wishlistData: Wishlist,
    accountsData : List<UserWishlist> ,
    isOwner: Boolean,
    currentUser: UUID? = null,
    onAddUserClick: () -> Unit,
    onDeleteUser: (UUID) -> Unit,
    onGeneratingToken: () -> Unit,
    onDeleteToken: () -> Unit
) {
    val url = stringResource(id = R.string.app_url)
    val clipboardManager = LocalClipboardManager.current
    // Formatta LocalDateTime per visualizzare in un formato leggibile
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = wishlistData.lastUpdate?.format(formatter)

    // Costruisci la stringa di stato della wishlist in modo flessibile
    val visibilityStatus = buildString {
        if (wishlistData.isPublic) {
            append(stringResource(R.string.wishlists_public))
            append(" ")
        } else {
            append(stringResource(R.string.wishlists_private))
            append(" ")
        }
        if(isOwner){
        if (accountsData.isNotEmpty() && accountsData.size > 1) {

            if (wishlistData.token != null) {
                // Condivisa sia con utenti che tramite token
                append("${stringResource(R.string.wishlist_shared_with_user)} ${stringResource(R.string.wishlist_shared_with_token)}")
            } else {
                // Condivisa solo con utenti
                append(stringResource(R.string.wishlist_shared_with_user))
            }
        } else if (wishlistData.token != null) {
            // Condivisa solo tramite token
            append(stringResource(R.string.wishlist_shared_with_token))
        }
    }}



    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Nome della wishlist e ultima modifica con stato
        item {
            Text(
                text = wishlistData.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            // Ultima modifica
            Text(
                text = "${stringResource(R.string.wishlist_last_edit)} $formattedDate",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {Spacer(modifier = Modifier.height(8.dp))}
        item {
            // Stato della wishlist (pubblica, condivisa, privata, ecc.)
            Text(
                text = visibilityStatus,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        if(isOwner){
        item {
            // Titolo "Url di condivisione"
            Text(
                text = stringResource(R.string.wishlist_sharing_url),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }}
        item {
            if(isOwner){
                // Controlla se il token è presente
                if (wishlistData.token.isNullOrEmpty()) {
                    // Se il token non è presente, mostra il pulsante per generarlo

                    Button(
                        onClick = {
                            onGeneratingToken()
                        },

                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.elevatedButtonElevation()
                    ) {
                        Text(text = stringResource(R.string.wishlist_generate_token), style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    // Se il token è presente, visualizzalo
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text =  "$url/wishlist/${wishlistData.token}" ,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pulsanti "Elimina Token" e "Copia" affiancati
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween // Spazio tra i pulsanti
                    ) {
                        // Pulsante per eliminare il token
                        Button(
                            onClick = {
                                onDeleteToken()
                            },
                            modifier = Modifier.weight(1f), // Distribuisce lo spazio in modo equo
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                        ) {
                            Text(text = stringResource(R.string.delete), style = MaterialTheme.typography.bodyLarge)
                        }

                        Spacer(modifier = Modifier.width(8.dp)) // Spazio tra i due pulsanti

                        // Pulsante per copiare il token
                        Button(
                            onClick = {
                                // Azione per copiare il token negli appunti


                                wishlistData.token.let {
                                    clipboardManager.setText(AnnotatedString( "$url/wishlist/${wishlistData.token}" ))
                                }
                            },
                            modifier = Modifier.weight(1f), // Distribuisce lo spazio in modo equo
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = stringResource(R.string.copy), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
        if(isOwner){
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = stringResource(R.string.wishlist_users),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }}else{
            item {
            Text(
                text = stringResource(R.string.wishlist_owner),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )}
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(items = accountsData, key = { it.userId }) { account ->
            UserCardData(
                account,
                onDelete = { onDeleteUser(account.userId) },
                isOwner = isOwner,
                currentUser = currentUser
            )
        }

        if (accountsData.size < 15 && isOwner) {
            item {
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(100.dp)
                        .clickable { onAddUserClick() }, // Make the card clickable to add a user
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {

                        FontAwesomeIcon(unicode = "\uf234", fontSize = 48.sp)
                    }
                }
            }
        }
    }

}
@Composable
fun UsersList(
    wishlistData: SingleWishlistData) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    val formattedDate = wishlistData.lastUpdate.format(formatter)
    val visibilityStatus = buildString {
        if (wishlistData.isPublic) {
            append(stringResource(R.string.wishlists_public))
        } else {
            append(stringResource(R.string.wishlists_private))
        }}


    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        // Nome della wishlist e ultima modifica con stato
        item {
            Text(
                text = wishlistData.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            // Ultima modifica
            Text(
                text = "${stringResource(R.string.wishlist_last_edit)} $formattedDate",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {Spacer(modifier = Modifier.height(8.dp))}
        item {
            // Stato della wishlist (pubblica, condivisa, privata, ecc.)
            Text(
                text = visibilityStatus,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }


            item {
                Text(
                    text = stringResource(R.string.wishlist_owner),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.fillMaxWidth()
                )}

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(items = wishlistData.accounts, key = { it.id }) { account ->
            UserCard(
                account
            )
        }
    }

}

@Composable
fun UserCardData(it: UserWishlist, onDelete: () -> Unit, isOwner: Boolean = false, currentUser: UUID?=null) {
    // Track whether the card is in delete mode



    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Display username
            Text(
                text = it.username,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display ownership value
            if(isOwner){
            Text(
                text = it.valueOwnership,
                style = MaterialTheme.typography.bodyMedium,
            )}

            Spacer(modifier = Modifier.height(16.dp))
            if(it.keyOwnership!="owner" && isOwner && it.userId!=currentUser){
                // Delete button
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onDelete() // Trigger the deletion action
                    },

                ) {
                    Text(
                        text = stringResource(R.string.delete),
                    )
                }
            }
        }
    }}
@Composable
fun UserCard(it: SingleUserWishlistData) {
    // Track whether the card is in delete mode
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Display username
            Text(
                text = it.username,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(8.dp))


        }
    }}

@Composable
fun WishlistAddNewUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var username by remember { mutableStateOf(TextFieldValue("")) }
    var showError by remember { mutableStateOf(false) }
    val usernamePattern = "^[a-zA-Z0-9_]*$".toRegex()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                if (username.text.length in 3..20 && username.text.matches(usernamePattern)) {
                    onConfirm(username.text)
                } else {
                    showError = true
                }
            }) {
                Text(stringResource(R.string.wishlists_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.add_new_user)) },
        text = {
            Column {
                // OutlinedTextField per l'input dell'username
                OutlinedTextField(
                    value = username,
                    onValueChange = { newValue ->
                        username = newValue
                        showError = false // Resetta l'errore quando l'utente cambia il testo
                    },
                    label = { Text(stringResource(R.string.username_label)) },
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Messaggio di errore dinamico
                if (showError) {
                    Text(
                        text = stringResource(R.string.username_validation),
                        color = Color.Red,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}