package unical.enterpriceapplication.onlycards.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.jaikeerthick.composable_graphs.composables.line.LineGraph
import com.jaikeerthick.composable_graphs.composables.line.model.LineData
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphColors
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphFillType
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphStyle
import com.jaikeerthick.composable_graphs.composables.line.style.LineGraphVisibility
import com.jaikeerthick.composable_graphs.style.LabelPosition
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIconColor
import unical.enterpriceapplication.onlycards.viewmodels.ProductTypeDetailsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTypeDetails(productId: String?, navHostController: NavHostController, productTypeDetailsViewModel: ProductTypeDetailsViewModel) {
    val productTypeDetails by productTypeDetailsViewModel.singleProductType.collectAsState() // Dati del prodotto
    val products by productTypeDetailsViewModel.products.collectAsState() // Dati dei prodotti
    val scrollState = rememberScrollState() // Stato di scorrimento
    var showBottomSheet by remember { mutableStateOf(false) } // Mostra il BottomSheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) // Stato del BottomSheet
    var page = 0    // variabile che contiene la pagina dei prodotti da prendere
    val maxLength = 15  // lunghezza massima del testo
    var displayText = ""    // testo da visualizzare
    val cellCount = 2 // numero di celle per riga
    val lazyGridState = rememberLazyGridState() // stato della lista
    var set = ""    // set
    var activeGame = "" // gioco attivo
    var showFab by remember { mutableStateOf(false) }   // Mostra il pulsante per tornare in cima
    val coroutineScope = rememberCoroutineScope()   // Coroutine
    val error by productTypeDetailsViewModel.error.collectAsState()   // Errore
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore

    LaunchedEffect(productId, lazyGridState) {
        scrollState.scrollTo(0)
        if (productId != null) {
            productTypeDetailsViewModel.getSingleProductType(productId)
            productTypeDetailsViewModel.getProducts(productId, page)
        }

        // Gestione della page
        snapshotFlow { lazyGridState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (totalItems != 0 && lastVisibleItem == totalItems - 1 && productTypeDetailsViewModel.hasMoreProducts.value) {
                    page++
                    if (productId != null)
                        productTypeDetailsViewModel.getProducts(productId, page)
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
            Column(
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (showFab) {
                    ToTheTopListFloatingButton(
                        onClick = {
                            coroutineScope.launch { lazyGridState.animateScrollToItem(0) }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FloatingGraphButton(onClick = {
                    coroutineScope.launch {
                        showBottomSheet = true
                        sheetState.show()
                    }
                })
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
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
                            set = productTypeDetails?.features?.let { findFeature("set", it) } ?: ""

                            FontAwesomeIconColor("\uf015", fontSize = 12.sp, modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(bottom = 2.dp)
                                .clickable {
                                    navHostController.navigate("home")
                                }, color = Color(0xFF17A2B8))
                            Text(text = "/ ", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                            productTypeDetails?.let { it1 -> Text(text = "${it1.game} ", fontSize = 13.sp, modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .clickable { navHostController.navigate("home") }, fontWeight = FontWeight.Bold, color = Color(0xFF17A2B8))
                                activeGame = it1.game
                            }
                            if (set != "") {
                                displayText = if (set.length > maxLength) set.substring(0, maxLength) + "..." else set
                                Text(text = "/ ", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                                Text(text = "$displayText ", fontSize = 13.sp, modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .clickable { navHostController.navigate("set/$set/$activeGame") }, fontWeight = FontWeight.Bold, color = Color(0xFF17A2B8))
                            }
                            displayText = if((productTypeDetails?.name?.length ?: 0) > maxLength) productTypeDetails?.name?.substring(0, maxLength) + "..." else productTypeDetails?.name ?: ""
                            Text(text = "/ $displayText", fontSize = 13.sp, modifier = Modifier.align(Alignment.CenterVertically), fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.padding(top = 5.dp))

                        Text (
                            text = "${productTypeDetails?.name}",
                            modifier = Modifier.padding(start = 20.dp),
                            fontSize = if ((productTypeDetails?.name?.length ?: 0) > 20) 20.sp else 25.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            val imageRequest = ImageRequest.Builder(LocalContext.current)
                                .data(productTypeDetails?.photo)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .error(R.drawable.error_card)
                                .build()

                            AsyncImage(
                                model = imageRequest,
                                contentDescription = "ProductType Photo",
                                modifier = Modifier.height(400.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.product_type_page_title),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_page_name),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = (" " + productTypeDetails?.name) ?: "",
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_page_cards_sold),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = (" " + productTypeDetails?.numSell) ?: "0",
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_page_membership_set),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = (" $set") ?: "",
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_card_number),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = " " + findFeature("set card number", productTypeDetails?.features ?: emptyList()),
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_card_rarity),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = " " + findFeature("rarity", productTypeDetails?.features ?: emptyList()),
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_card_type),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = " " + findFeature("category", productTypeDetails?.features ?: emptyList()),
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    Text(
                                        text = stringResource(id = R.string.product_type_card_min_price),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = (" " + (productTypeDetails?.minPrice?.amount ?: 0)) + " €",
                                        fontSize = 16.sp,
                                    )
                                }
                                Row {
                                    if((products?.size ?: 0) > 35){
                                        FontAwesomeIconColor(
                                            unicode = "\uf118",
                                            fontSize = 25.sp,
                                            color = Color(0xFF69AE21),
                                        )
                                        Text(
                                            text = stringResource(id = R.string.product_type_card_available),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF69AE21),
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                    if((products?.size ?: 0) > 10 && (products?.size ?: 0) <= 35) {
                                        FontAwesomeIconColor(
                                            unicode = "\uf579",
                                            fontSize = 25.sp,
                                            color = Color(0xFFBDB76B),
                                        )
                                        Text(
                                            text = stringResource(id = R.string.product_type_card_almost_sold_out),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFBDB76B),
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                    if((products?.size ?: 0) > 0 && (products?.size ?: 0) <= 10) {
                                        FontAwesomeIconColor(
                                            unicode = "\uf57a",
                                            fontSize = 25.sp,
                                            color = Color(0xFFF54900),
                                        )
                                        Text(
                                            text = stringResource(id = R.string.product_type_card_last_pieces),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFF54900),
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                    if((products?.size ?: 0) == 0) {
                                        FontAwesomeIconColor(
                                            unicode = "\uf5b3",
                                            fontSize = 25.sp,
                                            color = Color(0xFFBF0001),
                                        )
                                        Text(
                                            text = stringResource(id = R.string.product_type_card_not_available),
                                            fontSize = 25.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFBF0001),
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (products?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.padding(bottom = 15.dp))
                            Text (
                                text = stringResource(id = R.string.product_type_offers),
                                modifier = Modifier.padding(start = 20.dp),
                                fontSize = if ((productTypeDetails?.name?.length ?: 0) > 20) 20.sp else 25.sp,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))
                        }
                    }
                }

                products?.takeIf { it.isNotEmpty() }?.let { nonNullProducts ->
                    items(nonNullProducts.size) { index ->
                        ProductCard(nonNullProducts[index], navHostController)
                    }
                }
            }
        }

        // Mostra il BottomSheet se showBottomSheet è true
        if (showBottomSheet)
            MenuBottomSheet(sheetState = sheetState, onDismiss = { showBottomSheet = false }, products = products ?: emptyList())
    }
}   // Componente per i dettagli del prodotto

@Composable
fun ProductCard(product: ProductData, navHostController: NavHostController) {
    val imageWidth = 170.dp
    val imageHeight = 180.dp
    val totalHeight = 260.dp
    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(product.images.firstOrNull()?.photo)
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
            navHostController.navigate("product/${product.id}")
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
                contentDescription = "Product Photo",
                modifier = Modifier
                    .width(imageWidth)
                    .height(imageHeight)
                    .clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if ((product.account?.username?.length ?: 0) > 10) product.account?.username?.substring(0, 10) + "..." else product.account?.username ?: "Default",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = if((product.account?.username?.length ?: 0) > 10) 14.sp else 16.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )

                val conditionColor = when (product.condition) {
                    Condition.MINT -> Color(0xFF1E90FF)
                    Condition.NEAR_MINT -> Color(0xFF228B22)
                    Condition.EXCELLENT -> Color(0xFF556B2F)
                    Condition.GOOD -> Color(0xFFB8860B)
                    Condition.LIGHT_PLAYED -> Color(0xFFFFA500)
                    Condition.PLAYED -> Color(0xFFFF69B4)
                    Condition.POOR -> Color(0xFF8B0000)
                }

                Text(
                    text = getConditionAbbreviation(product.condition),
                    color = conditionColor,
                    modifier = Modifier
                        .background(conditionColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp)
                )
            }


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
        }
    }
}   // Componente della card del prodotto

@Composable
fun FloatingGraphButton(onClick: ()->Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        FontAwesomeIconColor("\uf201", fontSize = 24.sp, color = Color.White)
    }
}   // Componente del bottone floating

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(sheetState: SheetState, onDismiss: () -> Unit, products: List<ProductData>) {
    // Variabili
    var dailyAverage by remember { mutableStateOf(listOf<LineData>()) }
    val scope = rememberCoroutineScope()
    val tooltipGraph = rememberTooltipState()
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        dailyAverage = calculateDailyAverages(products).takeLast(15)

        // Creo lo stile del grafico
        val style = LineGraphStyle(
            visibility = LineGraphVisibility(
                isXAxisLabelVisible = true,
                isYAxisLabelVisible = true,
            ),
            yAxisLabelPosition = LabelPosition.LEFT,
            colors = LineGraphColors(
                lineColor = Color(0xFFC51D39),
                pointColor = Color(0xFFC51D39),
                fillType = LineGraphFillType.Gradient(
                    brush = Brush.verticalGradient(listOf(Color(0xFFA6172A), Color(0x00A6172A)))
                )
            )
        )

        Column {
            Row {
                Text(
                    text = stringResource(id = R.string.product_type_graph_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
                )
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
                    tooltip = { PlainTooltip { Text(text = stringResource(id = R.string.product_type_graph_tooltip) ) } },
                    state = tooltipGraph
                ) {
                    IconButton(
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 10.dp),
                        onClick = { scope.launch { tooltipGraph.show() } }
                    ) {
                        FontAwesomeIcon(
                            unicode = "\uf05a",
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            LineGraph(
                data = dailyAverage,
                style = style,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                onPointClick = { point ->
                    Toast.makeText(context, "${point.y} €", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}   // Componente del menù che si apre da sotto a sopra

// ----- Funzioni di supporto -----
fun findFeature(name: String, features: List<FeatureData>): String {
    for (feature in features) {
        if(feature.name == name)
            return feature.value
    }

    return ""
}   // Trova il valore di una feature
fun calculateDailyAverages(cardInfo: List<ProductData>): List<LineData> {
    // Inizializzazione
    var dailyAverages = mutableListOf<Double>()
    var lastDate = mutableListOf<String>()
    val now = Date()
    val dailyData = mutableMapOf<String, MutableList<Double>>()
    var dailyAveragesLine = mutableListOf<LineData>()

    // Raccogliere i dati giornalieri
    cardInfo.forEach { card ->
        val releaseDate = SimpleDateFormat("yyyy-MM-dd").parse(card.releaseDate.toString())
        val dateStr = SimpleDateFormat("yyyy-MM-dd").format(releaseDate)

        if (!dailyData.containsKey(dateStr)) {
            dailyData[dateStr] = mutableListOf()
        }
        dailyData[dateStr]?.add(card.price.amount)
    }

    dailyAverages = MutableList(30) { 0.0 } // Prepara una lista di 30 giorni inizializzata a 0.0

    var lastAverage = 0.0 // Ultimo valore medio calcolato
    var foundFirstValue = false

    for (i in 0 until 30) {
        val date = Calendar.getInstance()
        date.time = now
        date.add(Calendar.DATE, -(29 - i))
        val dateStr = SimpleDateFormat("yyyy-MM-dd").format(date.time)
        lastDate.add(dateStr)

        if (dailyData.containsKey(dateStr)) {
            dailyData[dateStr]?.let { dailyPrices ->
                val average = (dailyPrices.sum() / dailyPrices.size)
                    .toBigDecimal()
                    .setScale(2, RoundingMode.HALF_EVEN)
                    .toDouble()
                dailyAverages[i] = average
                lastAverage = average
                foundFirstValue = true
            }
        } else if (foundFirstValue)
            dailyAverages[i] = lastAverage // Riempie i valori mancanti con l'ultimo valore calcolato
         else
            dailyAverages[i] = 0.0 // Riempie i valori mancanti con 0 prima di trovare il primo valore
    }

    // Riempio la lista di LineData con giorno e valore medio
    for (i in 0 until 30) {
        val date = SimpleDateFormat("dd").format(SimpleDateFormat("yyyy-MM-dd").parse(lastDate[i]))
        dailyAveragesLine.add(LineData(date, dailyAverages[i].roundToInt().toDouble()))
    }

    return dailyAveragesLine
}   // Calcola le medie giornaliere
fun getConditionAbbreviation(condition: Condition): String {
    return when (condition) {
        Condition.MINT -> "MT"
        Condition.NEAR_MINT -> "NM"
        Condition.EXCELLENT -> "EX"
        Condition.GOOD -> "GD"
        Condition.LIGHT_PLAYED -> "LP"
        Condition.PLAYED -> "PL"
        Condition.POOR -> "PO"
        else -> "N/A"
    }
}   // Ottieni l'abbreviazione della condizione
