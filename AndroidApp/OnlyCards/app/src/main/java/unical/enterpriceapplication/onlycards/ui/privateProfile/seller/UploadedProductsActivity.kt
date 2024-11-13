package unical.enterpriceapplication.onlycards.ui.privateProfile.seller

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.ToTheTopListFloatingButton
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.UploadedProductsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData

@Composable
fun UploadedProducts(navHostController: NavHostController, uploadedProductsViewModel: UploadedProductsViewModel) {
    val scope = rememberCoroutineScope()   // scope
    var showFab by remember { mutableStateOf(false) }   // Mostra il pulsante per tornare in cima
    val error by uploadedProductsViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val gridListState = rememberLazyListState()   // stato della lista
    val page = remember { mutableIntStateOf(0) }    // Pagina
    val products by uploadedProductsViewModel.products.collectAsState()  // Prodotti
    val isLoading = remember { mutableStateOf(false) }    // Stato di caricamento

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        uploadedProductsViewModel.getProducts(page.intValue)
    }
    LaunchedEffect(gridListState) {
        snapshotFlow { gridListState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (totalItems != 0 && lastVisibleItem == totalItems - 1 && uploadedProductsViewModel.hasMoreProducts.value){
                    page.intValue++
                    uploadedProductsViewModel.getProducts(page.intValue)
                }

                showFab = totalItems > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
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
                ToTheTopListFloatingButton { scope.launch { gridListState.scrollToItem(0) } }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.padding(it)) {
            products?.takeIf { it.isNotEmpty() }?.let { nonNullProducts ->
                LazyColumn(state = gridListState, userScrollEnabled = !isLoading.value) {
                    item {
                        Text(
                            text = stringResource(id = R.string.uploaded_products_title),
                            modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))
                    }

                    items(nonNullProducts) { product ->
                        UploadedProductsCard(product, uploadedProductsViewModel, navHostController, isLoading)
                    }
                }
            } ?: run {
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
                        text = stringResource(id = R.string.uploaded_products_no_products),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun UploadedProductsCard(product: ProductData, uploadedProductsViewModel: UploadedProductsViewModel, navHostController: NavHostController, isLoading: MutableState<Boolean>) {
    val imageHeight = 180.dp
    val imageWith = 130.dp
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoadingDelete by remember { mutableStateOf(false) }
    var isLoadingModify by remember { mutableStateOf(false) }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(product.images.first().photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
            .heightIn(max = 200.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(10.dp),
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Product Photo",
                modifier = Modifier
                    .height(imageHeight)
                    .width(imageWith)
                    .clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = product.productType.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Row {
                    Text(
                        text = stringResource(id = R.string.uploaded_products_release_data),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = " " + product.releaseDate,
                        fontSize = 15.sp
                    )
                }
                Row {
                    Text(
                        text = stringResource(id = R.string.uploaded_products_price),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = " " + product.price.amount + "€",
                        fontSize = 15.sp
                    )
                }
                if(!product.sold) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        Button(
                            onClick = {
                                isLoadingModify = true
                                isLoading.value = true
                                navHostController.navigate("uploadedProductEdit/${product.id}")
                                isLoading.value = false
                                isLoadingModify = false
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.padding(end = 5.dp),
                            enabled = !isLoading.value
                        ) {
                            if(isLoadingModify){
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                            else {
                                Text(
                                    text = stringResource(id = R.string.uploaded_products_modify_label),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoadingDelete = true
                                    isLoading.value = true
                                    val success = uploadedProductsViewModel.deleteProduct(product.id.toString())

                                    if(success) {
                                        navHostController.navigate("uploadedProducts") {
                                            popUpTo("uploadedProducts") { inclusive = true }
                                            launchSingleTop = true
                                        }
                                        Toast.makeText(context, context.getString(R.string.uploaded_products_cancel_success), Toast.LENGTH_SHORT).show()
                                    }
                                    else {
                                        Toast.makeText(context, context.getString(R.string.uploaded_products_cancel_error), Toast.LENGTH_SHORT).show()
                                    }

                                    isLoadingDelete = false
                                    isLoading.value = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
                            enabled = !isLoading.value
                        ) {
                            if (isLoadingDelete) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            }
                            else {
                                Text(
                                    text = stringResource(id = R.string.uploaded_products_cancel),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}