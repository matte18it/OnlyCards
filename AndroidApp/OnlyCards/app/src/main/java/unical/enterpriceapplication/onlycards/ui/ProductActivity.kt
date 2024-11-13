package unical.enterpriceapplication.onlycards.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.times
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.AuthViewModel
import unical.enterpriceapplication.onlycards.viewmodels.ProductViewModel
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleProduct(navHostController: NavHostController, productId: String?, authViewModel: AuthViewModel, viewModel: ProductViewModel) {
    // Variabili
    val application = LocalContext.current.applicationContext as Application    // Contesto dell'applicazione
    val product by viewModel.singleProduct.collectAsState(null)    // Prodotto
    val wishlist by viewModel.wishlist.collectAsState(null)   // Wishlist
    val currentUser = authViewModel.currentUser.collectAsState(initial = null)    // Utente corrente
    val coroutineScope = rememberCoroutineScope()   // CoroutineScope
    var showWishlistDialog by remember { mutableStateOf(false) }    // Mostra il dialog per la wishlist
    var page by remember { mutableIntStateOf(0) }   // Pagina
    val lazyColumn = rememberLazyListState()    // Stato della lista
    val tooltipCondition = rememberTooltipState()   // Tooltip per la condizione
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val error by viewModel.error.collectAsState()   // Errore
    var isBuy by remember { mutableStateOf(false) }    // Acquisto
    var isCart by remember { mutableStateOf(false) }    // Carrello
    var isGlobalLoading = remember { mutableStateOf(false) }    // Caricamento globale

    LaunchedEffect(productId) {
        viewModel.getSingleProduct(productId ?: "")
    }
    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(showWishlistDialog) {
        if (!showWishlistDialog) {
            page = 0
        }
    }
    LaunchedEffect(lazyColumn) {
        // Gestione della page
        snapshotFlow { lazyColumn.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if(totalItems != 0 && lastVisibleItem == totalItems - 1 && viewModel.hasMoreProducts.value) {
                    page++
                    viewModel.getUserWishlist(page)
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
            Column(Modifier.verticalScroll(state = rememberScrollState(), enabled = !isGlobalLoading.value)) {
                val conditionColor = when (product?.condition) {
                    Condition.MINT -> Color(0xFF1E90FF)
                    Condition.NEAR_MINT -> Color(0xFF228B22)
                    Condition.EXCELLENT -> Color(0xFF556B2F)
                    Condition.GOOD -> Color(0xFFB8860B)
                    Condition.LIGHT_PLAYED -> Color(0xFFFFA500)
                    Condition.PLAYED -> Color(0xFFFF69B4)
                    Condition.POOR -> Color(0xFF8B0000)
                    else -> Color.Gray
                }
                val availableColor = if(product?.sold == false) Color(0xFF228B22) else Color(0xFFFF0000)

                Text(
                    text = "${product?.productType?.name} (${product?.productType?.language})",
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp),
                    fontSize = if ((product?.productType?.name?.length ?: 0) > 20) 20.sp else 25.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                var scale by remember { mutableFloatStateOf(1f) } // Fattore di scala
                var offsetX by remember { mutableFloatStateOf(0f) } // Offset X
                var offsetY by remember { mutableFloatStateOf(0f) } // Offset Y

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // Ottieni la larghezza e l'altezza disponibili
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = 400f

                    HorizontalPager(
                        state = rememberPagerState(pageCount = { product?.images?.size ?: 0 }),
                        modifier = Modifier
                            .padding(start = 35.dp, end = 35.dp)
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    // Aggiorna lo zoom
                                    val newScale = max(scale * zoom, 1f)

                                    // Calcola i nuovi offset solo se lo zoom è maggiore di 1
                                    if (newScale > 1f) {
                                        scale = newScale
                                        offsetX = max(min(offsetX + pan.x, (containerWidth * (scale - 1)) / 2), -(containerWidth * (scale - 1)) / 2)
                                        offsetY = max(min(offsetY + pan.y, (containerHeight * (scale - 1)) / 2), -(containerHeight * (scale - 1)) / 2)
                                    }
                                }
                            }
                    ) { page ->
                        val imageRequest = ImageRequest.Builder(LocalContext.current)
                            .data(product?.images?.get(page)?.photo)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .error(R.drawable.error_card)
                            .build()

                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Photo of the product",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .fillMaxWidth()
                                .height(400.dp)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopStart
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.product_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Row {
                            Text(
                                text = stringResource(id = R.string.product_type),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = (" " + product?.productType?.type) ?: "",
                                fontSize = 16.sp,
                            )
                        }
                        Row {
                            Text(
                                text = stringResource(id = R.string.product_condition) + " ",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                                tooltip = { PlainTooltip { Text(text = product?.condition.toString()) } },
                                state = tooltipCondition
                            ) {
                                Text(
                                    text = getConditionAbbreviation(product?.condition ?: Condition.MINT),
                                    color = conditionColor,
                                    modifier = Modifier
                                        .background(conditionColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 4.dp)
                                        .clickable(onClick = { coroutineScope.launch { tooltipCondition.show() } })
                                )
                            }
                        }
                        Row {
                            Text(
                                text = stringResource(id = R.string.product_release_date),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = (" " + product?.releaseDate.toString()) ?: "",
                                fontSize = 16.sp,
                            )
                        }
                        Row {
                            Text(
                                text = stringResource(id = R.string.product_description),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = (" " + product?.stateDescription) ?: "",
                                fontSize = 16.sp,
                            )
                        }

                        Spacer(modifier = Modifier.padding(10.dp))

                        if(product?.account != null) {
                            Text(
                                text = stringResource(id = R.string.product_seller),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Row {
                                Text(
                                    text = stringResource(id = R.string.product_seller_username) + " ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (product?.account?.username) ?: "",
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable {
                                        navHostController.navigate("users/${product?.account?.username}")
                                    },
                                    color = Color(0xFF17A2B8)
                                )
                            }
                            Row {
                                Text(
                                    text = stringResource(id = R.string.product_seller_email),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = (" " + product?.account?.email) ?: "",
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${product?.account?.email}")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        application.startActivity(intent)

                                    },
                                    color = Color(0xFF17A2B8)
                                )
                            }
                            Row {
                                Text(
                                    text = stringResource(id = R.string.product_seller_phone),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = (" " + product?.account?.phone) ?: "",
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable {
                                        val uri = Uri.parse("tel:${product?.account?.phone}")
                                        val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }

                                        application.startActivity(intent)
                                    },
                                    color = Color(0xFF17A2B8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.padding(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.product_price),
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = (" " + product?.price?.amount + "€") ?: "",
                                fontSize = 30.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = if (product?.sold == false) stringResource(id = R.string.product_available) else stringResource(id = R.string.product_not_available),
                                color = availableColor,
                                textAlign = TextAlign.End,
                                modifier = Modifier
                                    .background(availableColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 4.dp),
                            )
                        }
                        val user = currentUser.value
                        if(user != null && !user.roles.contains("ROLE_ADMIN") && product?.sold == false && user.roles.contains("ROLE_BUYER")) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isBuy = true
                                        isGlobalLoading.value = true
                                        val success = viewModel.buyProduct(product?.id.toString())

                                        if (success) {
                                            navHostController.navigate("product/${product?.id}")  {
                                                popUpTo("product/${product?.id}") { inclusive = true }
                                                launchSingleTop = true
                                            }
                                            Toast.makeText(application, application.getString(R.string.product_bought), Toast.LENGTH_SHORT).show()
                                        }
                                        else Toast.makeText(application, application.getString(R.string.product_error), Toast.LENGTH_SHORT).show()

                                        isGlobalLoading.value = false
                                        isBuy = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                                enabled = !isGlobalLoading.value
                            ) {
                                if (isBuy){
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                }
                                else {
                                    Text(
                                        text = stringResource(id = R.string.product_buy_now),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            isCart = true
                                            isGlobalLoading.value = true
                                            val success = viewModel.addCart(product?.id.toString())
                                            isCart = false
                                            isGlobalLoading.value = false

                                            if(success) Toast.makeText(application, application.getString(R.string.product_cart), Toast.LENGTH_SHORT).show()
                                            else Toast.makeText(application, application.getString(R.string.product_cart_error), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                                    enabled = !isGlobalLoading.value
                                ) {
                                    if(isCart) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                                    }
                                    else {
                                        Text(
                                            text = stringResource(id = R.string.product_add_to_cart),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        viewModel.getUserWishlist(page)
                                        showWishlistDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                                    enabled = !isGlobalLoading.value
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.product_add_to_wishlist),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        // Bottone per modificare il prodotto, se l'utente è un amministratore
                        if (user != null && user.roles.contains("ROLE_ADMIN") && product?.sold == false) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        navHostController.navigate("editSingleProduct/${product?.id}")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                                    enabled = !isGlobalLoading.value
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.product_admin_modify),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                // Bottone per eliminare il prodotto
                                Button(
                                    onClick = {
                                        product?.id?.let { id ->
                                            coroutineScope.launch {
                                                isGlobalLoading.value = true

                                                // Chiama la funzione per eliminare il prodotto, e aspetta il risultato
                                                val success = viewModel.deleteProduct(id.toString())
                                                isGlobalLoading.value = false

                                                // Mostra il Toast e gestisci la navigazione nel contesto principale
                                                if (success) {
                                                    Toast.makeText(
                                                        application,
                                                        application.getString(R.string.product_delete_success),
                                                        Toast.LENGTH_SHORT
                                                    ).show()

                                                    // Torna alla schermata precedente dopo l'eliminazione
                                                    navHostController.popBackStack()
                                                } else {
                                                    Toast.makeText(
                                                        application,
                                                        application.getString(R.string.product_delete_error),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                                    enabled = !isGlobalLoading.value
                                ) {
                                    if (isGlobalLoading.value) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = stringResource(id = R.string.product_admin_delete),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showWishlistDialog) {
                AlertDialog(
                    onDismissRequest = { showWishlistDialog = false },
                    title = {
                        Text(
                            text = stringResource(id = R.string.product_choose_wishlist_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        // Verifica se ci sono wishlist da mostrare
                        val wishlistData = wishlist
                        if (!wishlistData.isNullOrEmpty()) {
                            val dynamicHeight = (wishlistData.size * 50.dp).coerceAtMost(300.dp)

                            Box(
                                modifier = Modifier.height(dynamicHeight)
                            ) {
                                LazyColumn(state = lazyColumn) {
                                    items(wishlistData) { wishlistItem ->
                                        Text(
                                            text = wishlistItem.name,
                                            fontSize = 18.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if(!isGlobalLoading.value) {
                                                        coroutineScope.launch {
                                                            isGlobalLoading.value = true

                                                            val success = viewModel.addProductToWishlist(
                                                                wishlistItem.id.toString(),
                                                                product?.id.toString()
                                                            )

                                                            if (success) Toast.makeText(application, application.getString(R.string.product_wishlist_added), Toast.LENGTH_SHORT).show()
                                                            else Toast.makeText(application, application.getString(R.string.product_wishlist_error), Toast.LENGTH_SHORT).show()

                                                            showWishlistDialog = false
                                                            isGlobalLoading.value = false
                                                        }
                                                    }
                                                }
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        } else {
                            Text(text = stringResource(R.string.product_no_wishlist), fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isGlobalLoading.value = true
                                showWishlistDialog = false
                                isGlobalLoading.value = false
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                            enabled = !isGlobalLoading.value
                        ) {
                            Text(
                                text = stringResource(id = R.string.product_wishlist_close),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        }
    }
}