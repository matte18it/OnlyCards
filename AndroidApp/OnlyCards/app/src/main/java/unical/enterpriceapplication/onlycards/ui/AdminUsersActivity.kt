package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.UserViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData

@Composable
fun AdminUsersScreen(
    navHostController: NavHostController,
    userViewModel: UserViewModel
) {
    // Stati per gli utenti e la gestione del caricamento
    val users by userViewModel.users.collectAsState()

    // Stati locali per la ricerca dinamica
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10

    // Effettua la ricerca ogni volta che cambia username o email
    LaunchedEffect(username, email, currentPage) {
        userViewModel.loadUsers(page = currentPage, size = pageSize, username = username, email = email)
    }

    // Stato dello scroll della LazyColumn
    val listState = rememberLazyListState()

    Scaffold(
        content = { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Elementi di ricerca (all'inizio della lista)
                item {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { newValue -> username = newValue },
                        label = { Text(stringResource(id = R.string.search_by_username)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { newValue -> email = newValue },
                        label = { Text(stringResource(id = R.string.search_by_email)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Elementi della tabella degli utenti
                items(users) { user ->
                    AdminUserRow(user, onEdit = {
                        navHostController.navigate("editUser/${user.id}")
                    })
                }

                // Controlli di paginazione (alla fine della lista)
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        PaginationControls(
                            currentPage = currentPage,
                            totalPages = if (users.size == pageSize) currentPage + 2 else currentPage + 1,
                            onPageChange = { newPage -> currentPage = newPage }
                        )
                    }
                }
            }

        }
    )
}


// Componenti di riga per ciascun utente
@Composable
fun AdminUserRow(user: UserData, onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Colonna Nome
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(user.username, modifier = Modifier.padding(start = 0.dp))
        }

        // Colonna Email
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(user.email, modifier = Modifier.padding(start = 10.dp))
        }

        // Colonna Telefono
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(user.cellphoneNumber ?: stringResource(id = R.string.phone_not_available), modifier = Modifier.padding(start = 20.dp))
        }

        // Icona di Modifica
        Button(
            onClick = { onEdit() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(id = R.string.edit),
            )
        }
    }

    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Icona per la prima pagina
        IconButton(
            onClick = { onPageChange(0) },
            enabled = currentPage > 0,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(
                imageVector = Icons.Default.FirstPage,
                contentDescription = stringResource(id = R.string.first_page),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Icona per la pagina precedente
        IconButton(
            onClick = { onPageChange(currentPage - 1) },
            enabled = currentPage > 0,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                contentDescription = stringResource(id = R.string.previous_page),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Visualizza i numeri di pagina
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0 until totalPages).forEach { pageIndex ->
                Box(
                    modifier = Modifier
                        .size(32.dp, 32.dp)
                        .border(
                            width = if (pageIndex == currentPage) 2.dp else 1.dp,
                            color = if (pageIndex == currentPage) Color.Transparent else MaterialTheme.colorScheme.onSurface,
                            shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp))
                        )
                        .background(
                            color = if (pageIndex == currentPage) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp))
                        )
                        .clickable { onPageChange(pageIndex) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${pageIndex + 1}",
                        color = if (pageIndex == currentPage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Icona per la pagina successiva
        IconButton(
            onClick = { onPageChange(currentPage + 1) },
            enabled = currentPage < totalPages - 1,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = stringResource(id = R.string.next_page),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Icona per l'ultima pagina
        IconButton(
            onClick = { onPageChange(totalPages - 1) },
            enabled = currentPage < totalPages - 1,
            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.LastPage,
                contentDescription = stringResource(id = R.string.last_page),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
