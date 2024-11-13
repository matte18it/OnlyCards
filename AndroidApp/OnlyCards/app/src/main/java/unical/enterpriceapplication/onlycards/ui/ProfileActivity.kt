package unical.enterpriceapplication.onlycards.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.FabPosition
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.ProductViewModel
import unical.enterpriceapplication.onlycards.viewmodels.UserViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.PageData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductInfoData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserPublicProfileData

@Composable
fun UserProfileScreen(navHostController: NavHostController, userViewModel: UserViewModel, username: String, productViewModel: ProductViewModel) {
    val user by userViewModel.userPublicProfileData.collectAsState(initial = null);
    val products by userViewModel.userPublicProductData.collectAsState(initial = PageData(0,0,0,0, emptyList()))
    val loading by userViewModel.isLoading.collectAsState(initial = false)
    val snackbarHostState = SnackbarHostState()
    val gridState = rememberLazyGridState()
    var showFab by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(navHostController) {
        userViewModel.getUserPublicInfo(username)
        userViewModel.getProducts(username)
    }
    // Listen for scroll state changes
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo }
            .collect { layoutInfo ->
                val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                val totalItemsCount = layoutInfo.totalItemsCount

                if (lastVisibleItemIndex == totalItemsCount - 1 && products.number < products.totalPages - 1) {
                    Log.d("UserProfileScreen", "Fetching page ${products.number + 1}")
                    userViewModel.getProducts(username, products.number + 1)
                }
                showFab = totalItemsCount > 0 && layoutInfo.visibleItemsInfo.firstOrNull()?.index != 0
            }
    }
    Scaffold ( snackbarHost = { SnackbarHost(snackbarHostState) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            containerColor =  MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError ,
        )
    }},   floatingActionButton = {
        if (showFab) {
            ToTheTopListFloatingButton { coroutineScope.launch { gridState.animateScrollToItem(0) } }
        }
    }, floatingActionButtonPosition = FabPosition.End){
        Box(modifier = Modifier.padding(it)){
            val message = stringResource(R.string.search_error)
            LaunchedEffect(products.error){
                if(products.error){
                    snackbarHostState.showSnackbar(message)
                }

            }
            if(loading){
                CircularLoadingIndicator()
                }else{
                    val userData = user
                    if(userData!=null){
                        UserProfilePage(products = products, gridState = gridState, onClickOnWishlist = {
                            navHostController.navigate("users/${userData.username}/wishlists")
                        }, user = user, navHostController, productViewModel)
                    }else{
                        UserNotFound()
            }


        }
    }

}}

@Composable
fun UserProfilePage(products: PageData<ProductInfoData>, gridState: LazyGridState, onClickOnWishlist: () -> Unit, user: UserPublicProfileData?, navHostController: NavHostController, productViewModel: ProductViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,

        ) {
        user.let {
            item (span = {GridItemSpan(2)}){
                if (it != null) {
                    UserInfo(it)
                }
            }
        }
        item(span = {GridItemSpan(2)}){
            Button(onClick = { onClickOnWishlist()}) {
                Text("${stringResource(R.string.profile_show_wishlist)} ${user?.username}")


            }

        }
        item (span = {GridItemSpan(2)}){
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(products.content.isNotEmpty()){
        item (span = {GridItemSpan(2)}){
            Text(stringResource(R.string.profile_sell_product), style = MaterialTheme.typography.titleLarge)
        }}
        items(products.content, key = {it.id}) { data ->
            SingleProductCard(data, navHostController, productViewModel)
        }
        if(products.content.isEmpty()){
            item (span = {GridItemSpan(2)}){
                NoProductsFound()
            }
        }

    }
}

@Composable
fun SingleProductCard(data: ProductInfoData, navHostController: NavHostController, productViewModel: ProductViewModel) {
    val application = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Card(
        onClick = {
           navHostController.navigate("product/${data.id}")
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
                    items(data.images, key = {it}) { imageUrl ->
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
                            coroutineScope.launch {
                                val success = productViewModel.addCart(data.id.toString())

                                if(success) Toast.makeText(application, application.getString(R.string.product_cart), Toast.LENGTH_SHORT).show()
                                else Toast.makeText(application, application.getString(R.string.product_cart_error), Toast.LENGTH_SHORT).show()
                            }
                        },
                    ) {
                        FontAwesomeIcon(unicode = "\uf07a", fontSize = 16.sp)
                    }
                }
               }



            Spacer(modifier = Modifier.height(4.dp))



        }
    }

}

@Composable
fun UserInfo(user: UserPublicProfileData) {

    val imageModel = user.profileImage.ifBlank {
        R.drawable.profile // Immagine di fallback
    }

    // Contenitore per allineare gli elementi in alto
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Aggiungi padding intorno al Box
        contentAlignment = Alignment.TopCenter // Allinea il contenuto in alto
    ) {
        // Usare una Row per mettere l'immagine a sinistra e il testo a destra
        Row(
            verticalAlignment = Alignment.CenterVertically, // Centra verticalmente gli elementi nella Row
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // Padding intorno alla Row per spaziatura
                .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) // Sfondo con angoli arrotondati
                .padding(8.dp) // Padding interno alla Row
        ) {
            // Immagine del profilo a sinistra
            AsyncImage(
                model = imageModel, // URL o risorsa dell'immagine
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(150.dp) // Dimensione dell'immagine
                    .clip(CircleShape) // Forma rotonda
                    .border(2.dp, Color.Gray, CircleShape) // Bordo grigio intorno all'immagine
            )

            Spacer(modifier = Modifier.width(16.dp)) // Spazio tra l'immagine e lo username

            // Username a destra dell'immagine
            Column(
                horizontalAlignment = Alignment.Start, // Allinea il testo a sinistra
                verticalArrangement = Arrangement.Center // Centra il testo verticalmente
            ) {
                Text(
                    text = user.username, // Mostra lo username
                    fontSize = 24.sp, // Dimensioni del testo
                    fontWeight = FontWeight.Bold, // Testo in grassetto
                    color = MaterialTheme.colorScheme.onSurface // Colore del testo
                )



            }
        }
    }
}
@Composable
fun UserNotFound() {
    // Usa uno sfondo o un gradiente per dare più enfasi visiva
    Box(
        modifier = Modifier
            .fillMaxSize()

            .padding(32.dp), // Aggiungi padding intorno ai contenuti
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icona grande con spaziatura
            FontAwesomeIcon(
                unicode = "\uf21b", // Codice dell'icona "user-slash"
                fontSize = 96.sp, // Icona molto più grande
            )

            Spacer(modifier = Modifier.height(24.dp)) // Spazio tra icona e testo

            // Testo principale con un po' più di enfasi
            Text(
                text =  stringResource(R.string.profile_not_found),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally) // Allinea il testo al centro
            )

            Spacer(modifier = Modifier.height(16.dp)) // Spazio tra testo principale e suggerimento

            // Testo di suggerimento o azione per cercare di nuovo
            Text(
                text = stringResource(R.string.profile_not_found_explanation),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = TextAlign.Center // Allinea il testo al centro
            )
        }
    }
}
@Composable
fun NoProductsFound(){
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FontAwesomeIcon(
                unicode = "\uf6d5",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_no_sell),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


