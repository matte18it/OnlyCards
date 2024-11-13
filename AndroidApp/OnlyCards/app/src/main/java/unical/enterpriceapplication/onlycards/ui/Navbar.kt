package unical.enterpriceapplication.onlycards.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIconColor
import unical.enterpriceapplication.onlycards.viewmodels.GameViewModel

@Composable
fun Navbar(navController: NavController, gameViewModel: GameViewModel, selectedGame: MutableState<String>) {
    // Variabili
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val img = BitmapFactory.decodeResource(LocalContext.current.resources, R.drawable.logo).asImageBitmap() // Immagine del logo
    val games = gameViewModel.games.collectAsState(initial = null)  // Flusso di giochi
    var expanded by remember { mutableStateOf(false) }  // Variabile per il menu espanso
    val currentUrl = navBackStackEntry?.destination?.route ?: "home" // URL corrente

    LaunchedEffect(key1 = Unit) {
        gameViewModel.getGames()
    }
    
    NavigationBar(
        containerColor = Color(0xFF04223E),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            ) {
                Image(
                    bitmap = img,
                    contentDescription = "OnlyCards Logo",
                    modifier = Modifier.clickable {
                        navController.navigate("home")
                    }
                )
            }

            if(currentUrl == "home") {
                Box(
                    modifier = Modifier.padding(end = 16.dp).clickable(onClick = { expanded = true })
                ) {
                    games.value?.get(selectedGame.value)?.let {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = it,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            FontAwesomeIconColor(
                                unicode = "\uf0d7",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        games.value?.filter { (key, _) -> key != selectedGame.value }?.forEach { (key, value) ->
                            DropdownMenuItem(
                                text = { Text(value) },
                                onClick = {
                                    selectedGame.value = key
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}