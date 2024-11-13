package unical.enterpriceapplication.onlycards.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.CartViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductCartData
import java.util.UUID

@Composable
fun Cart(viewModel: CartViewModel, navHostController: NavHostController) {
    val cartProducts by viewModel.cartProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showSummaryDialog by remember { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    val application = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = Unit) {
        viewModel.getCartItems()
    }

    Scaffold {
        Box(modifier = Modifier.padding(it)) {
            if(cartProducts.isNotEmpty()){
                Column(modifier = Modifier.fillMaxSize().padding(bottom = 56.dp).verticalScroll(state = scrollState)) {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                        error != null -> {
                            Text(
                                text = stringResource(id = R.string.cart_error_message),
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                        cartProducts.isEmpty() -> {
                            EmptyCartMessage(navHostController)
                        }
                        else -> {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(cartProducts) { product ->
                                    ProductItem(product, onRemoveClick = {
                                        coroutine.launch {
                                            val success = viewModel.removeProduct(it.toString())

                                            if(success) {
                                                Toast.makeText(application, application.getString(R.string.cart_remove_success), Toast.LENGTH_SHORT).show()
                                            }
                                            else {
                                                Toast.makeText(application, application.getString(R.string.cart_remove_error), Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }, navHostController = navHostController)
                                }
                            }
                        }
                    }
                }

                if (cartProducts.isNotEmpty()) {
                    Button(
                        onClick = { showSummaryDialog = true },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        enabled = cartProducts.isNotEmpty()
                    ) {
                        Text(text = stringResource(id = R.string.checkout_button))
                    }
                }

                if (showSummaryDialog) {
                    ProductSummaryDialog(
                        products = cartProducts,
                        viewModel = viewModel,
                        navHostController,
                        onConfirm = {
                            showSummaryDialog = false
                        },
                        onDismiss = {
                            showSummaryDialog = false
                        }
                    )
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
                        text = stringResource(id = R.string.empty_cart_message),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCartMessage(navHostController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.empty_cart_message),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navHostController.navigate("home") }) {
            Text(
                text = stringResource(id = R.string.go_to_homepage),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun ProductItem(product: ProductCartData, onRemoveClick: (UUID) -> Unit, navHostController: NavHostController) {
    val imageHeight = 180.dp

    Card(
        shape = RoundedCornerShape(bottomEnd = 5.dp, bottomStart = 5.dp, topStart = 5.dp, topEnd = 5.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp)
            .heightIn(max = 200.dp)
            .clickable {
                navHostController.navigate("product/${product.id}")
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(product.images.first().photo).memoryCachePolicy(CachePolicy.DISABLED).diskCachePolicy(CachePolicy.DISABLED).error(R.drawable.error_card).build(),
                contentDescription = "Product Photo",
                modifier = Modifier
                    .height(imageHeight)
                    .clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.cardName, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(id = R.string.product_language, product.cardLanguage),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(id = R.string.product_condition) + " " + product.condition,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(id = R.string.product_price) + " " + product.price.amount + " €",
                    fontSize = 18.sp
                )
                Button(
                    onClick = { onRemoveClick(product.id) },
                    modifier = Modifier.padding(top = 30.dp)
                ) {
                    Text(text = stringResource(id = R.string.remove_button))
                }
            }
        }
    }
}

@Composable
fun ProductSummaryDialog(
    products: List<ProductCartData>,
    viewModel: CartViewModel,
    navHostController: NavHostController,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.summary_title)) },
        text = {
            Column {
                products.forEach { product ->
                    Text(text = "${product.cardName} - ${product.price.amount} ${product.price.currency}")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = viewModel.confirmOrder()
                        if (success) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.confirm_order_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearCart()
                            navHostController.navigate("home")
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.confirm_order_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
            ) {
                Text(text = stringResource(id = R.string.confirm_purchase))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}


