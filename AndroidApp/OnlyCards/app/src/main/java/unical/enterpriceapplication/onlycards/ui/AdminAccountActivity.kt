package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.AuthViewModel

@Composable
fun AdminAccountScreen(navHostController: NavHostController, authViewModel: AuthViewModel) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            AdminAccountPage(
                onProfileClick = { navHostController.navigate("profile") },
                onOrderManagementClick = { navHostController.navigate("adminOrders") },
                onUserManagementClick = { navHostController.navigate("adminUsers") },
                onAddProductTypeClick = { navHostController.navigate("adminAddProductType") }, // Nuova navigazione
                onLogoutClick = {
                    coroutineScope.launch {
                        authViewModel.logout()
                        navHostController.navigate("login") {
                            popUpTo("adminAccount") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}



@Composable
fun AdminAccountPage(
    onProfileClick: () -> Unit,
    onOrderManagementClick: () -> Unit,
    onUserManagementClick: () -> Unit,
    onAddProductTypeClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titolo della pagina
        Text(
            text = stringResource(id = R.string.admin_area_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Pulsante per il Profilo
        AdminButton(
            text = stringResource(id = R.string.user_profile),
            onClick = onProfileClick
        )

        // Pulsante per Gestione Ordini
        AdminButton(
            text = stringResource(id = R.string.orders_label),
            onClick = onOrderManagementClick
        )

        // Pulsante per Gestione Utenti
        AdminButton(
            text = stringResource(id = R.string.users_label),
            onClick = onUserManagementClick
        )

        // Nuovo pulsante per aggiungere Product Type
        AdminButton(
            text = stringResource(id = R.string.add_product_type),
            onClick = onAddProductTypeClick // Gestisci il click per la nuova pagina
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pulsante di Logout
        AdminLogoutButton(onLogout = onLogoutClick)
    }
}


@Composable
fun AdminButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun AdminLogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        )
    ) {
        Text(
            text = stringResource(id = R.string.user_logout),
            color = MaterialTheme.colorScheme.onError
        )
    }
}
