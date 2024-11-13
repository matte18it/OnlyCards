package unical.enterpriceapplication.onlycards.ui

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.model.entity.WishlistProduct
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.ProductViewModel
import unical.enterpriceapplication.onlycards.viewmodels.SingleWishlistViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.PageData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistProductData
import java.util.UUID

@Composable
fun WishlistDetail(
    application: Application,
    navHostController: NavHostController,
    currentUser: State<AuthUser?>,
    wishlistsViewModel: SingleWishlistViewModel,
    productViewModel: ProductViewModel,
    id: String? = null,
    token: String? = null,
    username: String? = null,
    wishlsitName: String? = null
) {
    Log.d("WishlistDetail", "id : $id")
    Log.d("WishlistDetail", "token: $token")
    Log.d("WishlistDetail", "username: $username")
    Log.d("WishlistDetail", "wishlistName: $wishlsitName")

    LaunchedEffect(navHostController) {
        if (id.isNullOrBlank() && token.isNullOrBlank()) {
            if (username.isNullOrBlank() || wishlsitName.isNullOrBlank()) {
                navHostController.navigate("home")
            } else {
                wishlistsViewModel.retrievePublicWishlist(username, wishlsitName)
                wishlistsViewModel.retrieveProductsFromPublicWishlist(username, wishlsitName)
            }
        }
        if (!id.isNullOrBlank()) {
            if (currentUser.value == null || (currentUser.value?.roles?.contains("ROLE_BUYER") == false)) {
                navHostController.navigate("account")
            }

            wishlistsViewModel.retriveWishlist(UUID.fromString(id))
            wishlistsViewModel.retrieveProducts(UUID.fromString(id))
        }
        if (!token.isNullOrBlank()) {
            wishlistsViewModel.retrieveWishlistByToken(token)
            wishlistsViewModel.retrieveProductsByToken(token)
        }
    }

    if (!id.isNullOrBlank()) {
        WhishlistPage(application,wishlistsViewModel, navHostController, productViewModel, UUID.fromString(id))
    } else if (!token.isNullOrBlank()) {
        WhishlistPage(application,wishlistsViewModel, navHostController, productViewModel,null, token)
    } else {
        WhishlistPage(application, wishlistsViewModel, navHostController,productViewModel, null, null, wishlsitName, username)
    }
}

@Composable
fun NoWishlistFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_wishlist_found),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        FontAwesomeIcon(unicode = "\uf5d2", fontSize = 48.sp)
    }
}

@Composable
fun WhishlistPage(
    application: Application,
    singleWishlistViewModel: SingleWishlistViewModel,
    navHostController: NavHostController,
    productViewModel: ProductViewModel,
    id: UUID? = null,
    token: String? = null,
    wishlistName: String? = null,
    username: String? = null
) {
    val wishlistProductData by singleWishlistViewModel.wishlistProductData.collectAsState(emptyList())
    val wishlistProduct by singleWishlistViewModel.wishlistProduct.collectAsState()
    val wishlistData by singleWishlistViewModel.wishlistData.collectAsState(Wishlist(UUID.randomUUID(), stringResource(R.string.loading), isPublic = false))
    val loading by singleWishlistViewModel.isLoading.collectAsState()
    val error by singleWishlistViewModel.error.collectAsState()
    val success by singleWishlistViewModel.success.collectAsState()
    val gridState = rememberLazyGridState()
    val showFab = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val isOwner by singleWishlistViewModel.isOwner.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val openAlertDialog = remember { mutableStateOf(false) }

    // Preleva la stringa da stringResource() all'esterno del contesto LaunchedEffect
    val errorMessage = stringResource(R.string.wishlist_error)

    Scaffold(
        floatingActionButton = {
            if (showFab.value) {
                ToTheTopListFloatingButtonWishlist {
                    coroutineScope.launch { gridState.animateScrollToItem(0) }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = if (error == null) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        topBar = {
            Column {
                if (!wishlistProduct.error)
                    WishlistFilterBar(id = id, token = token, singleWishlistViewModel = singleWishlistViewModel, username = username, wishlistName = wishlistName)
                if (!wishlistProduct.error)
                    WishlistTopOptions(
                        onClickChangeName = { openAlertDialog.value = true },
                        disabled = !isOwner,
                        onClickShowSharingOptions = {
                            if (id != null) {
                                navHostController.navigate("wishlist-sharing-options?isPublicOrToken=false")
                            } else {
                                navHostController.navigate("wishlist-sharing-options?isPublicOrToken=true")
                            }
                        },
                        id = id
                    )
            }
        }
    ) { it ->
        Box(modifier = Modifier.padding(it).fillMaxSize()) {
            if(openAlertDialog.value){
                ChangeWishlistNameDialog(
                    onDismiss = { openAlertDialog.value = false },
                    onConfirm = { newName, newVisibility ->
                        if (id != null) {
                            singleWishlistViewModel.updateWishlist(newName, newVisibility, id)
                        }
                    },
                    currentName = wishlistData.name,
                    currentVisibility = wishlistData.isPublic,
                    onDelete = {
                        if (id != null) {
                            singleWishlistViewModel.delete(id)
                        }
                        navHostController.navigate("wishlist")
                    }
                )
            }
            LaunchedEffect(error) {
                error?.let {
                    snackbarHostState.showSnackbar(it.message)
                }
            }
            LaunchedEffect(success) {
                success?.let {
                    snackbarHostState.showSnackbar(it)
                }
            }

            if (loading) {
                CircularLoadingIndicator()
            } else {
                if (wishlistProduct.error) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FontAwesomeIcon(unicode = "\uf7a9", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.wishlist_not_found),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.wishlist_not_found_explanation),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    if ((wishlistProduct.content.isEmpty() && id == null) || (wishlistProductData.isEmpty() && id != null)) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            FontAwesomeIcon(unicode = "\uf5d2", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            if (id != null) {
                                Text(
                                    text = stringResource(R.string.wishlist_empty),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.wishlist_empty_explanation),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            } else if (token != null || (!username.isNullOrBlank() && !wishlistName.isNullOrBlank())) {
                                Text(
                                    text = stringResource(R.string.wishlist_no_products),
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.wishlist_no_products_explanation),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        WishlistListData(navHostController, application, wishlistProduct, wishlistProductData, singleWishlistViewModel,productViewModel,  showFab, gridState, isOwner, id, token, wishlistName, username)
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeWishlistNameDialog(onDismiss: () -> Unit, onConfirm:  (String, Boolean ) -> Unit, currentName: String, currentVisibility:Boolean, onDelete: () -> Unit) {
    var wishlistName by remember { mutableStateOf(currentName) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val validNameRegex = "^[a-zA-Z0-9 ]{3,30}$".toRegex()
    var expanded by remember { mutableStateOf(false) }
    val itemsMap = mapOf(
        stringResource(R.string.wishlists_public) to true,
        stringResource(R.string.wishlists_private) to false
    )
    val selectedVisibility = itemsMap.entries.find { it.value == currentVisibility }?.key.orEmpty()
    var selectedOption by remember { mutableStateOf(selectedVisibility) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${stringResource(R.string.edit)} $currentName") },
        text = {
            Column {
                Text(stringResource(R.string.wishlist_insert_new_name))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = wishlistName,  // bind the state to the text field
                    onValueChange = { wishlistName = it; errorMessage=null },  // update state when text changes
                    label = { Text(stringResource(R.string.name)) },
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),  // Ensure the row takes up full width
                    horizontalArrangement = Arrangement.End // Align content (button) to the right
                ) {

                }
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.wishlists_select_visibility)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )


                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        itemsMap.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.key) },
                                onClick = {
                                    selectedOption = item.key
                                    expanded = false
                                }
                            )
                        }

                    }
                }
                val errorValidation = stringResource(R.string.wishlists_name_validation)
                Button(
                    onClick = {
                        if (!validNameRegex.matches(wishlistName)) {
                            errorMessage = errorValidation
                        } else {
                            itemsMap[selectedOption]?.let { onConfirm(wishlistName, it) }
                            onDismiss()  // Dismiss the dialog
                        }
                    },
                    enabled = wishlistName != currentName || selectedOption != selectedVisibility,
                ) {
                    Text(stringResource(R.string.edit))
                }


                // Danger Zone Section
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.danger_zone),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Button(
                    onClick = {
                        onDelete()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.delete_wishlist), color = MaterialTheme.colorScheme.onError)
                }
            }
        },
        confirmButton = {

        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

}

@Composable
fun WishlistListData(navHostController: NavHostController, application: Application,  wishlistProductData: PageData<WishlistProductData>, wishlistProduct:List<WishlistProduct>, singleWishlistViewModel: SingleWishlistViewModel, productViewModel: ProductViewModel, showFab: MutableState<Boolean>, gridState: LazyGridState, isOwner:Boolean, id: UUID?=null,token: String?=null, wishlistName: String?=null, username: String?=null) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                // Check if the user has scrolled to the end
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItemsCount = layoutInfo.totalItemsCount

                if (lastVisibleItemIndex == totalItemsCount - 1 && wishlistProductData.number < wishlistProductData.totalPages - 1) {
                    // Load more data
                    if(id!=null){
                        singleWishlistViewModel.retrieveProducts(id, wishlistProductData.number + 1)
                    }else if(token!=null){
                        singleWishlistViewModel.retrieveProductsByToken(token, wishlistProductData.number + 1)
                    }else{
                        if(!username.isNullOrBlank() && !wishlistName.isNullOrBlank()){
                            singleWishlistViewModel.retrieveProductsFromPublicWishlist(username, wishlistName, wishlistProductData.number + 1)
                    }
                }}
                // Show the FAB only if there are items and the first visible item is not the first one
                showFab.value = totalItemsCount > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,

    ) {
        if(id==null){
            items(wishlistProductData.content, key = {it.id}) { data ->
                WishlistProductCardData(data, onAddToCart = {
                    coroutineScope.launch {


                        val success = productViewModel.addCart(data.id.toString())

                        if(success) Toast.makeText(application, application.getString(R.string.product_cart), Toast.LENGTH_SHORT).show()
                        else Toast.makeText(application, application.getString(R.string.product_cart_error), Toast.LENGTH_SHORT).show()
                    }
                },
                    onClick = {
                    navHostController.navigate("product/${data.id}")
                })
            }
        }else{
        items(wishlistProduct, key = {it.id}) { data ->
            WishlistProductCard(data, isOwner, onDelete = {
                    singleWishlistViewModel.deleteProduct(data.id, id)
                coroutineScope.launch {
                    gridState.scrollToItem(0)
                }
            }, onAddToCart = {
                coroutineScope.launch {

                    val success = productViewModel.addCart(data.id.toString())

                    if(success) Toast.makeText(application, application.getString(R.string.product_cart), Toast.LENGTH_SHORT).show()
                    else Toast.makeText(application, application.getString(R.string.product_cart_error), Toast.LENGTH_SHORT).show()
                }
            }, onClick = {
                navHostController.navigate("product/${data.id}")
            });
        }}

}

}
@Composable
fun WishlistList( wishlistProductData: PageData<WishlistProductData>, wishlitProduct:List<WishlistProduct>,  singleWishlistViewModel: SingleWishlistViewModel, showFab: MutableState<Boolean>, gridState: LazyGridState, isOwner:Boolean, id: UUID?=null,token: String?=null, wishlistName: String?=null, username: String?=null) {
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                // Check if the user has scrolled to the end
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItemsCount = layoutInfo.totalItemsCount
                if (lastVisibleItemIndex == totalItemsCount - 1 && wishlistProductData.number < wishlistProductData.totalPages - 1) {
                    // Load more data
                    if(id!=null){
                        singleWishlistViewModel.retrieveProducts(id, wishlistProductData.number + 1)
                    }else if(token!=null){
                        singleWishlistViewModel.retrieveProductsByToken(token, wishlistProductData.number + 1)
                    }else{
                        if(!username.isNullOrBlank() && !wishlistName.isNullOrBlank()){
                            singleWishlistViewModel.retrieveProductsFromPublicWishlist(username, wishlistName, wishlistProductData.number + 1)
                        }
                    }}
                // Show the FAB only if there are items and the first visible item is not the first one
                showFab.value = totalItemsCount > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,

        ) {
        if(id==null){
        items(wishlistProductData.content, key = {it.id}) { data ->
            WishlistProductCardData(data)
        }}else{
            items(wishlitProduct, key = {it.id}) { data ->
                WishlistProductCard(data, isOwner, onDelete = {
                        singleWishlistViewModel.deleteProduct(data.id, id)

                })
            }
        }

    }

}
@Composable
fun WishlistProductCard(data: WishlistProduct, isOwner:Boolean, onDelete: () -> Unit = {},  onAddToCart: () -> Unit = {}, onClick:()->Unit={}) {
    // Listen for scroll state changes

    Card(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Immagine del prodotto
            if (data.images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(data.images, key = {it+ data.id}) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "product photo",
                            modifier = Modifier
                                .height(150.dp)
                                .width(150.dp),  // Puoi regolare la larghezza se necessario
                            alignment = Alignment.Center
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Nome del prodotto
            Text(
                text = data.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium, // You can adjust the text style if needed
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Gioco
            Text(
                text = data.game,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Lingua
            Text(
                text = "${stringResource(R.string.profile_language)} ${data.language}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            //Prorpietario
            Text(
                text = data.account.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Condizione del prodotto
            Text(
                text = data.condition,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prezzo
            Text(
                text = "${data.price.amount} ${data.price.currency}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante per aggiungere al carrello
            Row {
                Column(                    modifier = Modifier.weight(1f),
                ) {
                Button(
                    contentPadding = PaddingValues(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                    onClick = {
                        onAddToCart()
                    },
                ) {
                    FontAwesomeIcon(unicode = "\uf07a", fontSize = 16.sp)
                }}
                if(isOwner){
                Spacer(modifier = Modifier.width(8.dp))
                Column (                    modifier = Modifier.weight(1f)
                ){
                Button(
                    contentPadding = PaddingValues(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        onDelete()
                    },
                ) {

                    FontAwesomeIcon(unicode = "\uf1f8", fontSize = 16.sp) // Icona del cestino
                }}
            }}



            Spacer(modifier = Modifier.height(4.dp))



        }
    }
}
@Composable
fun WishlistProductCardData(data: WishlistProductData, onAddToCart: () -> Unit = {}, onClick:()->Unit={}) {
    // Listen for scroll state changes

    Card(
        onClick = {
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Immagine del prodotto
            if (data.images.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(data.images, key = {it+data.id}) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "product photo",
                            modifier = Modifier
                                .height(150.dp)
                                .width(150.dp),  // Puoi regolare la larghezza se necessario
                            alignment = Alignment.Center
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            // Nome del prodotto
            Text(
                text = data.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium, // You can adjust the text style if needed
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Gioco
            Text(
                text = data.game,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Lingua
            Text(
                text = "${stringResource(R.string.profile_language)} ${data.language}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            //Prorpietario
            Text(
                text = data.account.username,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Condizione del prodotto
            Text(
                text = data.condition,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Prezzo
            Text(
                text = "${data.price.amount} ${data.price.currency}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante per aggiungere al carrello
            Row {
                Column(                    modifier = Modifier.weight(1f),
                ) {
                    Button(
                        contentPadding = PaddingValues(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                        onClick = {
                            onAddToCart()
                        },
                    ) {
                        FontAwesomeIcon(unicode = "\uf07a", fontSize = 16.sp)
                    }}
              }



            Spacer(modifier = Modifier.height(4.dp))



        }
    }
}
@Composable
fun ToTheTopListFloatingButtonWishlist(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        FontAwesomeIcon(
            fontSize = 24.sp,
            unicode = "\uf062" // Arrow up icon
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistFilterBar( singleWishlistViewModel: SingleWishlistViewModel, id: UUID?=null, token: String?=null, username: String?=null, wishlistName: String?=null) {
    val filterMap = mapOf(
        "popolarity" to stringResource(R.string.popolarity),
        "price%20asc" to stringResource(R.string.price_asc),
        "price%20desc" to stringResource(R.string.price_desc)
    )
    val name = remember { mutableStateOf("") }
    val sort = remember { mutableStateOf("") }
    val owner = remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
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
                Text(text = stringResource(R.string.find_by), color = MaterialTheme.colorScheme.onSurface)

                // Add some space between text and icon
                Spacer(modifier = Modifier.width(8.dp))

                // Icon
                FontAwesomeIcon(
                    fontSize = 16.sp,
                    unicode = "\uf002", // Filter icon
                )
            }
            // Call the FilterModalBottomSheet composable
            if (showBottomSheet) {
                WishlistFilterSheet(
                    sheetState = sheetState,
                    name = name,
                    sort = sort,
                    owner = owner,
                    onDismissRequest = {
                        scope.launch {
                            showBottomSheet = false
                            sheetState.hide() // Hide the bottom sheet
                        }
                    }, onSearch = { nameValue, sortValue, ownerValue ->
                        name.value = nameValue
                        sort.value = sortValue
                        owner.value = ownerValue

                        if(id!=null){
                            singleWishlistViewModel.retrieveProducts(name = nameValue, productSorting =  sortValue, owner = ownerValue, wishlistId = id)
                        }else if(token!=null){
                            singleWishlistViewModel.retrieveProductsByToken(name = nameValue, productSorting =  sortValue, owner = ownerValue, token = token)
                        }else{
                            if(!username.isNullOrBlank() && !wishlistName.isNullOrBlank()){
                                singleWishlistViewModel.retrieveProductsFromPublicWishlist(username, wishlistName, name = nameValue, productSorting =  sortValue, owner = ownerValue)
                            }
                        }
                        scope.launch {
                            showBottomSheet = false
                            sheetState.hide() // Hide the bottom sheet
                        }
                    },
                    filterMap = filterMap
                )
        }
}}}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistFilterSheet(
    sheetState: SheetState,
    name: MutableState<String>,
    sort: MutableState<String>,
    owner: MutableState<String>,
    onDismissRequest: () -> Unit,
    onSearch: (String, String, String) -> Unit,
    filterMap : Map<String, String>
) {

val productName by remember { mutableStateOf(name) }
val productOwner by remember { mutableStateOf(owner) }
val sortOption by remember { mutableStateOf(sort) }


    ModalBottomSheet (sheetState = sheetState, onDismissRequest = onDismissRequest) {
        Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                ) {
            // Campo di ricerca per il nome del prodotto
            Text(text = stringResource(R.string.find_by_name), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp)) // Spazio tra il testo e il TextField
            OutlinedTextField(
                value = productName.value,
                modifier = Modifier.fillMaxWidth(),
                onValueChange = {newText-> productName.value = newText },
                placeholder = { Text(stringResource(R.string.insert_name_product)) },
            )

            Spacer(modifier = Modifier.height(16.dp)) // Spazio tra i campi

            // Campo di ricerca per il proprietario del prodotto
            Text(text = stringResource(R.string.find_by_owner), style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = productOwner.value,
                onValueChange = {newText-> productOwner.value = newText },
                placeholder = { Text(stringResource(R.string.insert_name_owner)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.wishlists_sorting_by), style = MaterialTheme.typography.bodyLarge)
            // Creazione dinamica dei FilterChip
            LazyVerticalGrid(
                columns = GridCells.Adaptive(200.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterMap.keys.toList()) { key ->
                    androidx.compose.material3.FilterChip(

                        selected = sortOption.value == key, // Controlla se il filtro è selezionato
                        onClick = {
                            if (sortOption.value == key) {
                                sortOption.value = "" // Deseleziona il filtro
                            } else {
                                sortOption.value = key // Aggiorna il filtro selezionato
                            }
                        },
                        label = { Text(text = filterMap[key] ?: "") }, // Testo del filtro
                    )
                }
            }

        }

            // Esegui azioni basate sui valori inseriti (opzionale)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    onSearch(productName.value, sortOption.value, productOwner.value)
                },
                modifier = Modifier.align(Alignment.End).padding(8.dp),
            ) {
                Text(stringResource(R.string.find))
            }
        }
}
@Composable
fun WishlistTopOptions(onClickChangeName: () -> Unit, onClickShowSharingOptions:()-> Unit, disabled: Boolean = false, id: UUID?=null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if(id!=null){
        Button(onClick = {onClickChangeName()}, modifier = Modifier.weight(1f), enabled = !disabled) {
            Text(stringResource(R.string.edit))
            FontAwesomeIcon(unicode = "\uf044", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))}
        Button(onClick = {onClickShowSharingOptions()}, modifier = Modifier.weight(1f).padding(if(id!=null) 0.dp else 10.dp)) {
            Text(stringResource(R.string.info))
            FontAwesomeIcon(unicode = "\uf064", fontSize = 16.sp)
        }
    }
}







