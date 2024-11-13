package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIconColor
import unical.enterpriceapplication.onlycards.viewmodels.GameViewModel
import unical.enterpriceapplication.onlycards.viewmodels.HomeProductTypeViewModel
import unical.enterpriceapplication.onlycards.viewmodels.HomeProductViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(selectedGame: String, navHostController: NavHostController, productViewModel: HomeProductViewModel, homeProductTypeViewModel: HomeProductTypeViewModel, gameViewModel: GameViewModel) {
    val productData by productViewModel.productData.collectAsState()    // dati del prodotto
    val tooltipLastProductState = rememberTooltipState()
    val tooltipBestSellingState = rememberTooltipState()
    val tooltipBestPriceState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    val bestSelling by homeProductTypeViewModel.bestSellings.collectAsState()  // dati del prodotto più venduto
    val bestPrice by homeProductTypeViewModel.bestPrice.collectAsState()  // dati del prodotto con il prezzo più basso
    val scrollState = rememberScrollState() // stato di scorrimento
    val games = gameViewModel.games.collectAsState()  // giochi
    val lastAdd = rememberPagerState(pageCount = { productData?.size ?: 1 })
    val bestProduct = rememberPagerState(pageCount = { bestSelling?.size ?: 1 })
    val bestPriceProduct = rememberPagerState(pageCount = { bestPrice?.size ?: 1 })
    val errorProductType by homeProductTypeViewModel.error.collectAsState()   // Errore
    val errorProduct by productViewModel.error.collectAsState()  // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore

    AutoScrollPager(lastAdd) // scorre automaticamente le pagine degli ultimi prodotti
    AutoScrollPager(bestProduct) // scorre automaticamente le pagine dei prodotti più venduti
    AutoScrollPager(bestPriceProduct) // scorre automaticamente le pagine dei prodotti con il prezzo più basso

    LaunchedEffect(selectedGame) {
        scrollState.scrollTo(0)

        if (gameViewModel.getGames()) {
            productViewModel.lastProducts(selectedGame, games.value)
            homeProductTypeViewModel.getBestSelling(selectedGame, games.value)
            homeProductTypeViewModel.getBestPrice(selectedGame, games.value)
        }
    }   // Effetto lanciato quando il gioco selezionato cambia
    LaunchedEffect(errorProduct) {
        errorProduct?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }   // Effetto lanciato quando si verifica un errore
    LaunchedEffect(errorProductType) {
        errorProductType?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }   // Effetto lanciato quando si verifica un errore

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
            Column(modifier = Modifier.verticalScroll(state = scrollState, enabled = true)) {
                // Sezione per i prodotti più recenti
                Row {
                    Text(
                        text = stringResource(id = R.string.last_add),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.last_add_tooltip) ) } },
                        state = tooltipLastProductState
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(35.dp)
                                .padding(top = 10.dp),
                            onClick = { scope.launch { tooltipLastProductState.show() } }
                        ) {
                            FontAwesomeIcon(
                                unicode = "\uf05a",
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp)
                )
                Spacer(modifier = Modifier.size(5.dp))
                if(productData.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.no_product_available), modifier = Modifier.padding(16.dp))
                    }
                }
                else {
                    HorizontalPager(
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                        state = lastAdd
                    ) { page ->
                        productData?.getOrNull(page)?.let { product ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                LastProduct(product = product, navController = navHostController)
                            }
                        }
                    }
                }

                // Sezione per i prodotti più venduti
                Row {
                    Text(
                        text = stringResource(id = R.string.best_sellers),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.best_sellers_tooltip) ) } },
                        state = tooltipBestSellingState
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(35.dp)
                                .padding(top = 10.dp),
                            onClick = { scope.launch { tooltipBestSellingState.show() } }
                        ) {
                            FontAwesomeIcon(
                                unicode = "\uf05a",
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp)
                )
                Spacer(modifier = Modifier.size(5.dp))
                if(bestSelling.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.no_product_available), modifier = Modifier.padding(16.dp))
                    }
                }
                else {
                    HorizontalPager(
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                        state = bestProduct
                    ) { page ->
                        bestSelling?.getOrNull(page)?.let { productType ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                BestSelling(productType = productType, navController = navHostController, index = page)
                            }
                        }
                    }
                }

                // Sezione per i prodotti con il prezzo migliore
                Row {
                    Text(
                        text = stringResource(id = R.string.best_price),
                        modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                        tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.best_price_tooltip) ) } },
                        state = tooltipBestPriceState
                    ) {
                        IconButton(
                            modifier = Modifier
                                .size(35.dp)
                                .padding(top = 10.dp),
                            onClick = { scope.launch { tooltipBestPriceState.show() } }
                        ) {
                            FontAwesomeIcon(
                                unicode = "\uf05a",
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))
                Spacer(modifier = Modifier.size(5.dp))
                if(bestPrice.isNullOrEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(id = R.string.no_product_available), modifier = Modifier.padding(16.dp))
                    }
                }
                else {
                    HorizontalPager(
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                        state = bestPriceProduct
                    ) { page ->
                        bestPrice?.getOrNull(page)?.let { productType ->
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                BestPrice(productType = productType, navController = navHostController, index = page)
                            }
                        }
                    }
                }
            }
        }
    }   // Schermata principale
}

@Composable
fun LastProduct(product: ProductData, navController: NavController) {
    val maxLength = 20
    val displayText = if (product.productType.name.length > maxLength) { product.productType.name.take(maxLength) + "..." } else { product.productType.name }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(product.productType.photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp, topStart = 12.dp, topEnd = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = {
                navController.navigate("product/${product.id}")
            },
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = "ProductType Photo",
                    modifier = Modifier.height(400.dp),
                    alignment = Alignment.Center,
                )

                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayText,
                            fontSize = if (product.productType.name.length > maxLength) 18.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Row {
                            FontAwesomeIconColor(
                                unicode = "\uf02b",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = product.price.amount.toString() + " €",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = stringResource(id = R.string.loaded_date) + " " + product.releaseDate,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}   // Card degli ultimi prodotti

@Composable
fun BestSelling(productType: ProductTypeData, navController: NavController, index: Int) {
    val maxLength = 20
    val displayText = if (productType.name.length > maxLength) { productType.name.take(maxLength) + "..." } else { productType.name }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(productType.photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp, topStart = 12.dp, topEnd = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = {
                navController.navigate("productTypeDetails/${productType.id}")
            },
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    AsyncImage(
                        modifier = Modifier.height(400.dp),
                        model = imageRequest,
                        contentDescription = "ProductType Photo",
                        alignment = Alignment.Center
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent, CircleShape)
                            .offset(y = 15.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayText,
                            fontSize = if (productType.name.length > maxLength) 18.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )


                        Row {
                            FontAwesomeIconColor(
                                unicode = "\uf02b",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = productType.minPrice.amount.toString() + " €",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.size(8.dp))

                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = stringResource(id = R.string.number_sales) + " " + productType.numSell,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}   // Card dei prodotti più venduti

@Composable
fun BestPrice(productType: ProductTypeData, navController: NavController, index: Int) {
    val maxLength = 20
    val displayText = if (productType.name.length > maxLength) { productType.name.take(maxLength) + "..." } else { productType.name }
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(productType.photo)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .error(R.drawable.error_card)
        .build()

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(bottomEnd = 12.dp, bottomStart = 12.dp, topStart = 12.dp, topEnd = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            onClick = {
                navController.navigate("productTypeDetails/${productType.id}")
            },
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    AsyncImage(
                        modifier = Modifier.height(400.dp),
                        model = imageRequest,
                        contentDescription = "ProductType Photo",
                        alignment = Alignment.Center
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.Transparent, CircleShape)
                            .offset(y = 15.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, CircleShape)
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = displayText,
                            fontSize = if (productType.name.length > maxLength) 18.sp else 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        Row {
                            FontAwesomeIconColor(
                                unicode = "\uf151",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = productType.price.amount.toString() + " €",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row {
                            FontAwesomeIconColor(
                                unicode = "\uf150",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = productType.minPrice.amount.toString() + " €",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}  // Card dei prodotti con il prezzo migliore

@Composable
fun AutoScrollPager(pagerState: PagerState, delayMillis: Long = 10000L) {
    LaunchedEffect(pagerState) {
        while(true) {
            delay(delayMillis)
            if (pagerState.pageCount > 1) {
                val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }
}
