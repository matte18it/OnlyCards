package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import unical.enterpriceapplication.onlycards.viewmodels.OrderViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.OrderData
import java.time.format.DateTimeFormatter

@Composable
fun AdminOrdersScreen(
    navHostController: NavHostController,
    orderViewModel: OrderViewModel
) {
    val orders by orderViewModel.orders.collectAsState()
    var buyer by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10

    LaunchedEffect(buyer, seller, currentPage) {
        orderViewModel.loadOrders(page = currentPage, size = pageSize, buyer = buyer, seller = seller)
    }

    Scaffold(
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = buyer,
                        onValueChange = { newValue -> buyer = newValue },
                        label = { Text(stringResource(id = R.string.search_by_buyer)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = seller,
                        onValueChange = { newValue -> seller = newValue },
                        label = { Text(stringResource(id = R.string.search_by_seller)) },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(orders) { order ->
                    OrderRow(order, onEdit = { orderId ->
                        navHostController.navigate("editOrder/$orderId")
                    })
                }

                // Controlli di paginazione alla fine della lista
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        PaginationControlsOrder(
                            currentPage = currentPage,
                            totalPages = if (orders.size == pageSize) currentPage + 2 else currentPage + 1,
                            onPageChange = { newPage -> currentPage = newPage }
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun OrderRow(order: OrderData, onEdit: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .height(IntrinsicSize.Min)
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(order.addDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), modifier = Modifier.padding(start = 0.dp))
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(order.buyer, modifier = Modifier.padding(start = 10.dp))
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(order.vendorEmail, modifier = Modifier.padding(start = 20.dp))
        }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                order.status,
                fontWeight = FontWeight.Bold,
                color = when (order.status) {
                    "SHIPPED" -> Color.Blue
                    "PENDING" -> Color.Yellow
                    "DELIVERED" -> Color.Green
                    "CANCELLED" -> Color.Red
                    else -> Color.Gray
                },
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Button(
            onClick = { onEdit(order.id.toString()) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_order))
        }
    }

    HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
}





@Composable
fun PaginationControlsOrder(
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

        // Numeri di pagina
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (0 until totalPages).forEach { pageIndex ->
                Box(
                    modifier = Modifier
                        .size(32.dp, 32.dp)
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