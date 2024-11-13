package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.AuthViewModel

@Composable
fun UserAccountScreen(navHostController: NavHostController, authViewModel: AuthViewModel) {
    // Variabili
    val coroutineScope = rememberCoroutineScope()
    val isSeller = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        isSeller.value = authViewModel.currentUser.firstOrNull()?.roles?.contains("ROLE_SELLER") ?: false
    }

    // Metodi
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            UserAccountPage(
                isSeller = isSeller.value,
                onProfileClick = { navHostController.navigate("profile") },
                onOrdersClick = { navHostController.navigate("orders") },
                onLogoutClick = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        navHostController.navigate("login") {
                            popUpTo("account") { inclusive = true }
                        }
                    }
                },
                onWalletClick = { navHostController.navigate("wallet") },
                onTransactionsClick = { navHostController.navigate("transactions") },
                onAddressClick = { navHostController.navigate("addresses") },
                onSupportClick = { navHostController.navigate("support") },
                onSellClick = { navHostController.navigate("sellProduct") },
                onUploadedProducts = { navHostController.navigate("uploadedProducts") },
                onProductRequest = { navHostController.navigate("productRequest") }
            )
        }
    }
}

@Composable
fun UserAccountPage(
    isSeller: Boolean,
    onProfileClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onWalletClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onAddressClick: () -> Unit,
    onSupportClick: () -> Unit,
    onSellClick: () -> Unit,
    onUploadedProducts: () -> Unit,
    onProductRequest: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(3.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titolo della pagina
        Text(
            text = stringResource(id = R.string.user_area),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsante per il Profilo
            UserButton(
                text = stringResource(id = R.string.user_profile),
                onClick = onProfileClick,
                unicode = "\uf2bd"
            )

            // Pulsante per gli Indirizzi
            UserButton(
                text = stringResource(id = R.string.user_address),
                onClick = onAddressClick,
                unicode = "\uf3c5"
            )

            // Pulsante per il Portafoglio
            UserButton(
                text = stringResource(id = R.string.user_wallet),
                onClick = onWalletClick,
                unicode = "\uf555"
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsante per le transazioni
            UserButton(
                text = stringResource(id = R.string.user_transactions),
                onClick = onTransactionsClick,
                unicode = "\ue528"
            )

            // Pulsante per gli ordini
            UserButton(
                text = stringResource(id = R.string.user_orders),
                onClick = onOrdersClick,
                unicode = "\uf468"
            )

            // Pulsante per il supporto
            UserButton(
                text = stringResource(id = R.string.user_help),
                onClick = onSupportClick,
                unicode = "\uf1d8"
            )
        }

        // Area Venditore
        if(isSeller) {
            Text(
                text = stringResource(id = R.string.user_seller_area),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 3.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pulsante per il caricamento del prodotto
                UserButton(
                    text = stringResource(id = R.string.user_seller_sell),
                    onClick = onSellClick,
                    unicode = "\uf81d"
                )

                // Pulsante per visualizzare i prodotti caricati
                UserButton(
                    text = stringResource(id = R.string.user_seller_uploaded),
                    onClick = onUploadedProducts,
                    unicode = "\uf1ea"
                )

                // Pulsante per inviare una richiesta di nuovo prodotto
                UserButton(
                    text = stringResource(id = R.string.user_seller_request),
                    onClick = onProductRequest,
                    unicode = "\uf658"
                )
            }
        }

        // Pulsante di Logout
        UserLogoutButton(onLogout = onLogoutClick)
    }
}

@Composable
fun UserButton(text: String, onClick: () -> Unit, unicode: String) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(110.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly // Spazio uniforme tra icona e testo
        ) {
            FontAwesomeIcon(
                unicode = unicode,
                fontSize = 40.sp
            )
            Text(
                text = if (text.length > 8) text.substring(0, 8) + "..." else text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun UserLogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = stringResource(id = R.string.user_logout),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onError
        )
    }
}
