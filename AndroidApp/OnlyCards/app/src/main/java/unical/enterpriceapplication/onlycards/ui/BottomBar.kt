package unical.enterpriceapplication.onlycards.ui

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import unical.enterpriceapplication.onlycards.R
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.wear.compose.material.Text
import unical.enterpriceapplication.onlycards.viewmodels.AuthViewModel
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon

@Composable
fun BottomBar(
    selectedPage: MutableState<String>,
    navHostController: NavHostController,
    onClick: () -> Unit,
    authViewModel: AuthViewModel
) {
    val navBackStackEntry by navHostController.currentBackStackEntryAsState()   // Ottiene l'entry corrente del backstack
    val currentUser by authViewModel.currentUser.collectAsState(initial = null) // Ottiene l'utente corrente
    val isAdmin = currentUser?.roles?.contains("ROLE_ADMIN") ?: false        // Verifica se l'utente è un admin

    LaunchedEffect(navBackStackEntry) {
        selectedPage.value = navBackStackEntry?.destination?.route ?: "home"
    }

    BottomAppBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)),
    ) {
        NavigationBar {
            NavigationBarItem(
                selected = selectedPage.value == "home",
                onClick = {
                    selectedPage.value = "home"
                    navHostController.navigate("home")
                    onClick()
                },
                icon = {
                    val scale by animateFloatAsState(
                        targetValue = if (selectedPage.value == "home") 1.3f else 1f,
                        animationSpec = tween(durationMillis = 300), label = ""
                    )

                    FontAwesomeIcon(
                        unicode = "\uf015",
                        fontSize = (14 * scale).sp
                    )
                },
                label = {
                    Text(
                        text = "Home",
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            )
            val user = currentUser
            if(!isAdmin  && user != null) {

                Log.d("BottomBar", "User roles: ${user.roles}")
                if((user.roles.contains("ROLE_SELLER") && user.roles.contains("ROLE_BUYER")) || (!user.roles.contains("ROLE_SELLER") && user.roles.contains("ROLE_BUYER"))) {
                    NavigationBarItem(
                        selected = selectedPage.value == "wishlist",
                        onClick = {
                            selectedPage.value = "wishlist"
                            navHostController.navigate("wishlist")
                            onClick()
                        },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selectedPage.value == "wishlist") 1.3f else 1f,
                                animationSpec = tween(durationMillis = 300), label = ""
                            )

                            FontAwesomeIcon(
                                unicode = "\uf004",
                                fontSize = (14 * scale).sp
                            )
                        },
                        label = {
                            Text(
                                text = "Wishlist",
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = selectedPage.value == "cart",
                        onClick = {
                            selectedPage.value = "cart"
                            navHostController.navigate("cart")
                            onClick()
                        },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selectedPage.value == "cart") 1.3f else 1f,
                                animationSpec = tween(durationMillis = 300), label = ""
                            )

                            FontAwesomeIcon(
                                unicode = "\uf07a",
                                fontSize = (14 * scale).sp
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.cart_label),
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
                else if (!user.roles.contains("ROLE_BUYER") && user.roles.contains("ROLE_SELLER")) {
                    NavigationBarItem(
                        selected = selectedPage.value == "sellProduct",
                        onClick = {
                            selectedPage.value = "sellProduct"
                            navHostController.navigate("sellProduct")
                            onClick()
                        },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selectedPage.value == "sellProduct") 1.3f else 1f,
                                animationSpec = tween(durationMillis = 300), label = ""
                            )

                            FontAwesomeIcon(
                                unicode = "\uf81d",
                                fontSize = (14 * scale).sp
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.user_seller_sell),
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    )

                    NavigationBarItem(
                        selected = selectedPage.value == "uploadedProducts",
                        onClick = {
                            selectedPage.value = "uploadedProducts"
                            navHostController.navigate("uploadedProducts")
                            onClick()
                        },
                        icon = {
                            val scale by animateFloatAsState(
                                targetValue = if (selectedPage.value == "uploadedProducts") 1.3f else 1f,
                                animationSpec = tween(durationMillis = 300), label = ""
                            )

                            FontAwesomeIcon(
                                unicode = "\uf466",
                                fontSize = (14 * scale).sp
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.user_seller_uploaded),
                                fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                    )
                }
            }
            else if (isAdmin) {
                NavigationBarItem(
                    selected = selectedPage.value == "adminOrders",
                    onClick = {
                        selectedPage.value = "adminOrders"
                        navHostController.navigate("adminOrders")
                        onClick()
                    },
                    icon = {
                        val scale by animateFloatAsState(
                            targetValue = if (selectedPage.value == "adminOrders") 1.3f else 1f,
                            animationSpec = tween(durationMillis = 300), label = ""
                        )

                        FontAwesomeIcon(
                            unicode = "\uf468",
                            fontSize = (14 * scale).sp
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.orders_label),
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                )

                NavigationBarItem(
                    selected = selectedPage.value == "adminUsers",
                    onClick = {
                        selectedPage.value = "adminUsers"
                        navHostController.navigate("adminUsers")
                        onClick()
                    },
                    icon = {
                        val scale by animateFloatAsState(
                            targetValue = if (selectedPage.value == "adminUsers") 1.3f else 1f,
                            animationSpec = tween(durationMillis = 300), label = ""
                        )

                        FontAwesomeIcon(
                            unicode = "\uf0c0",
                            fontSize = (14 * scale).sp
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.users_label),
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                )
            }

            NavigationBarItem(
                selected = selectedPage.value == "account",
                onClick = {
                    selectedPage.value = "account"
                    navHostController.navigate("account") {
                        popUpTo("home") { inclusive = true }
                    }
                    onClick() // Chiama la funzione di callback per eventuali azioni aggiuntive
                },
                icon = {
                    val scale by animateFloatAsState(
                        targetValue = if (selectedPage.value == "account") 1.3f else 1f,
                        animationSpec = tween(durationMillis = 300), label = ""
                    )

                    FontAwesomeIcon(
                        unicode = "\uf007",
                        fontSize = (14 * scale).sp
                    )
                },
                label = {
                    Text(
                        text = "Account",
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp
                    )
                }
            )
        }
    }
}
