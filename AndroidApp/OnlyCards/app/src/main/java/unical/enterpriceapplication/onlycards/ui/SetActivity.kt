package unical.enterpriceapplication.onlycards.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIconColor
import unical.enterpriceapplication.onlycards.viewmodels.SetViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData

@Composable
fun SetActivity(setName: String, navHostController: NavHostController, game: String, viewModel: SetViewModel) {
    // Variabili
    val cellCount = 2 // numero di celle per riga
    val lazyGridState = rememberLazyGridState() // stato della lista
    val maxLength = 25  // lunghezza massima del testo
    val productType by viewModel.productTypeSet.collectAsState()  // Dati del prodotto
    var page = 0    // Pagina
    var showFab by remember { mutableStateOf(false) }   // Mostra il pulsante per tornare in cima
    val coroutineScope = rememberCoroutineScope()   // Coroutine
    val error by viewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // Snackbar di errore

    LaunchedEffect(setName, game) {
        viewModel.getProducts(setName, game, page)

        // Gestione della page
        snapshotFlow { lazyGridState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if(totalItems != 0 && lastVisibleItem == totalItems - 1 && viewModel.hasMoreProducts.value) {
                    page++
                    viewModel.getProducts(setName, game, page)
                }

                showFab = totalItems > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
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
                ToTheTopListFloatingButton { coroutineScope.launch { lazyGridState.animateScrollToItem(0) } }
        },
        floatingActionButtonPosition = FabPosition.End
    ) {
        Box(modifier = Modifier.padding(it)) {
            if(productType != null) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cellCount),
                    state = lazyGridState,
                ) {
                    item(span = { GridItemSpan(cellCount) }) {
                        Column {
                            Row (
                                Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                FontAwesomeIconColor("\uf015", fontSize = 12.sp, modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .padding(bottom = 2.dp)
                                    .clickable {
                                        navHostController.navigate("home")
                                    }, color = Color(0xFF17A2B8)
                                )
                                Text(text = "/ ", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                                Text(text = game, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically).clickable { navHostController.navigate("home") }, fontWeight = FontWeight.Bold, color = Color(0xFF17A2B8))
                                Text(text = " / ", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                                Text(text = if (setName.length > maxLength) setName.substring(0, maxLength) + "..." else setName, fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.padding(top = 5.dp))

                            Text (
                                text = setName,
                                modifier = Modifier.padding(start = 20.dp),
                                fontSize = if (setName.length > 20) 18.sp else 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))
                        }
                    }

                    productType?.takeIf { it.isNotEmpty() }?.let { nonNullProductType ->
                        items(nonNullProductType.size) { index ->
                            ProductTypeSetCard(nonNullProductType[index], navHostController)
                        }
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
                        text = stringResource(id = R.string.product_type_set_error),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun ProductTypeSetCard(productType: ProductTypeData, navHostController: NavHostController) {
    val imageWidth = 170.dp
    val totalHeight = 270.dp
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(productType.photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Card(
        modifier = Modifier
            .padding(start = 15.dp, end = 15.dp, bottom = 15.dp)
            .width(imageWidth)
            .height(totalHeight),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        onClick = {
            navHostController.navigate("productTypeDetails/${productType.id}")
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .height(totalHeight)
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = "Product Type Photo",
                modifier = Modifier
                    .width(imageWidth)
                    .clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (productType.name.length > 20) productType.name.substring(0, 20) + "..." else productType.name,
                fontSize = if (productType.name.length > 20) 14.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
            )
        }
    }
}