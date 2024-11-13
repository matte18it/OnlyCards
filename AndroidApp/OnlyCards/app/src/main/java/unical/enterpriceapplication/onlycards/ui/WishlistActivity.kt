package unical.enterpriceapplication.onlycards.ui

import android.app.Application
import android.util.Log
import android.widget.Spinner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.WishlistsViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishList(navHostController: NavHostController, currentUser:State<AuthUser?>, wishlistsViewModel: WishlistsViewModel, username : String?=null) {
    LaunchedEffect (navHostController){
        if(username==null) {
            if (currentUser.value == null || (currentUser.value?.roles?.contains("ROLE_BUYER") == false)) {
                navHostController.navigate("account")
            }
            wishlistsViewModel.getWishlists(0)
        }else{
            wishlistsViewModel.getPublicWishlists(0, username)
        }
    }
    val wishlistList by wishlistsViewModel.wishlistData.collectAsState(initial = emptyList())
    val publicWishlistList by wishlistsViewModel.wishlistDataOnline.collectAsState(initial = emptyList())
    val loading by wishlistsViewModel.isLoading.collectAsState()
    val error by wishlistsViewModel.error.collectAsState()
    var selectedSort  by remember { mutableIntStateOf(0) }
    var selectedFilter  by remember { mutableIntStateOf(0) }
    val sortOptions = mapOf(
        "new" to stringResource(R.string.wishlists_sort_date_desc),  // Decrescente = più nuovi prima
        "old" to stringResource(R.string.wishlists_sort_date_asc)     // Crescente = più vecchi prima
    )

    val filterOptions = mapOf(
        null to stringResource(R.string.wishlists_filter_all),            // Nessun filtro
        true to stringResource(R.string.wishlists_filter_created_by_me),      // Filtra le voci create dall'utente
        false to stringResource(R.string.wishlists_filter_created_by_others)  // Filtra le voci condivise con l'utente
    )
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val openAlertDialog = remember { mutableStateOf(false) }
    val gridState = rememberLazyListState()
    val currentPage by wishlistsViewModel.currentPage.collectAsState()
    val totalElements by wishlistsViewModel.totalElements.collectAsState()
    val coroutineScope = rememberCoroutineScope()


    Scaffold (floatingActionButton = { FloatingOrderButton(onClick = {scope.launch {
        showBottomSheet = true
        sheetState.show() // Show the bottom sheet
    }}) },   snackbarHost = { SnackbarHost(snackbarHostState){snackbarData -> Snackbar(snackbarData = snackbarData,
        containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)  } },
        topBar = {
            if(username.isNullOrBlank()){
            AddAWishlist(onClick = {
        openAlertDialog.value = true
            })}else{
                Column (modifier = Modifier.background(MaterialTheme.colorScheme.secondary)) {
                    Text(
                        text = "${stringResource(R.string.wishlists_of)} $username",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    HorizontalDivider(Modifier.fillMaxWidth())
                }




            }
        }) { it ->
        Box(modifier = Modifier.padding(it)) {

            LaunchedEffect(error) {
                error?.let {
                    launch {
                        snackbarHostState.showSnackbar(it.message)
                    }

                }
            }

            LaunchedEffect(gridState) {
                snapshotFlow { gridState.layoutInfo }
                    .collect { layoutInfo ->
                        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        val totalItemsCount = layoutInfo.totalItemsCount
                        if (lastVisibleItemIndex == totalItemsCount - 1 && currentPage < totalElements - 1) {
                            if(username.isNullOrBlank()){
                            wishlistsViewModel.getWishlists(currentPage + 1, sortOptions.keys.toList()[selectedSort], filterOptions.keys.toList()[selectedFilter]?.toString())
                        }else{
                                wishlistsViewModel.getPublicWishlists(currentPage+1, username, sortOptions.keys.toList()[selectedSort])
                            }
                        }
                    }
            }
            if(openAlertDialog.value){
                AddWishlistDialog(onDismiss = {openAlertDialog.value = false}, onConfirm ={
                    wishlistName, wishlistVisibility ->
                    run {
                        wishlistsViewModel.createWishlist(wishlistName, wishlistVisibility) }


                })
            }
            if(loading){
                CircularLoadingIndicator()
            }else {
                if ((wishlistList.isEmpty() && username.isNullOrBlank()) || (publicWishlistList.isEmpty() && !username.isNullOrBlank())) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FontAwesomeIcon("\uf5b4", fontSize = 48.sp)
                        if(username.isNullOrBlank()){

                        Text(
                            text = stringResource(R.string.wishlists_no_desire),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.wishlists_start_to_explore),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }else{
                            Text(
                                text = stringResource(R.string.wishlists_user_no_wishlist),
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.wishlists_dont_be_boring),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }

                    }
                }else{
                    if (username.isNullOrBlank()) {
                        WishlistPage(wishlistList, navHostController, username, gridState)}
                    else{
                        WishlistPageData(publicWishlistList, navHostController, username, gridState)
                    }
            }}
            if(showBottomSheet){
                FilterModalBottomSheet(sheetState = sheetState, onDismiss = {showBottomSheet = false}, selectedSort = selectedSort, 
                    selectedFilter = selectedFilter, 
                    sortOptions = sortOptions.values.toList(),
                    filterOptions = filterOptions.values.toList(),
                    onSortSelected = {selectedSort = it;
                        showBottomSheet = false
                        coroutineScope.launch { gridState.scrollToItem(0) }
                        if(username.isNullOrBlank()){
                            wishlistsViewModel.getWishlists(0, sortOptions.keys.toList()[selectedSort], filterOptions.keys.toList()[selectedFilter]?.toString())
                        }else{
                            wishlistsViewModel.getPublicWishlists(0, username, sortOptions.keys.toList()[selectedSort])
                        }
                       },
                    username = username,
                    onFilterSelected = {selectedFilter = it;
                        showBottomSheet = false
                        coroutineScope.launch { gridState.scrollToItem(0) }
                        if(username.isNullOrBlank()){
                        wishlistsViewModel.getWishlists(0, sortOptions.keys.toList()[selectedSort], filterOptions.keys.toList()[selectedFilter]?.toString())
                    }else{
                            wishlistsViewModel.getPublicWishlists(0, username, sortOptions.keys.toList()[selectedSort])
                        }

                    })
            }
        }
    }

}

@Composable
fun CircularLoadingIndicator() {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) { // Center the CircularProgressIndicator
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun WishlistPageData(wishlistList:List<WishlistData>, navHostController: NavHostController, username: String?, gridListState: LazyListState) {
    LazyColumn ( state = gridListState
    ) {
        items(items = wishlistList, key={it.id}) { wishlistItem ->
            WishlistItemData(wishlistItem, onItemClick = {
                Log.d("WishlistPage", "Clicked on ${wishlistItem.name}")
                if(username.isNullOrBlank()) {
                    navHostController.navigate("wishlist/${wishlistItem.id}")
                }else{
                    navHostController.navigate("users/${username}/wishlists/${wishlistItem.name}")
                }
            },
                last = wishlistList.indexOf(wishlistItem) == wishlistList.size - 1
            )

        }


    }
}
@Composable
fun WishlistPage(wishlistList:List<Wishlist>, navHostController: NavHostController, username: String?, listState: LazyListState) {
    LazyColumn (state = listState
    ) {
        items(items = wishlistList, key={it.id}) { wishlistItem ->
            WishlistItem(wishlistItem, onItemClick = {
                Log.d("WishlistPage", "Clicked on ${wishlistItem.name}")
                if(username.isNullOrBlank()) {
                    navHostController.navigate("wishlist/${wishlistItem.id}")
                }else{
                    navHostController.navigate("users/${username}/wishlists/${wishlistItem.name}")
                }
            },
                last = wishlistList.indexOf(wishlistItem) == wishlistList.size - 1
            )

        }


    }
}

@Composable
fun WishlistItem(wishlistItem:Wishlist, onItemClick: () -> Unit, last: Boolean = false) {

    Box(modifier = Modifier.height( 64.dp).fillMaxWidth().clickable(onClick = {
        onItemClick()

    })){
        Text(
            text = wishlistItem.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
        )
        if(!last){
            HorizontalDivider(Modifier.fillMaxWidth().align(Alignment.BottomCenter))
        }
    }
}
@Composable
fun WishlistItemData(wishlistItem:WishlistData, onItemClick: () -> Unit, last: Boolean = false) {

    Box(modifier = Modifier.height( 64.dp).fillMaxWidth().clickable(onClick = {
        onItemClick()

    })){
        Text(
            text = wishlistItem.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
        )
        if(!last){
            HorizontalDivider(Modifier.fillMaxWidth().align(Alignment.BottomCenter))
        }
    }
}
@Composable
fun FloatingOrderButton(onClick:()->Unit) {
   FloatingActionButton(onClick=onClick) {
       FontAwesomeIcon("\uf0b0", fontSize = 24.sp)
   }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterModalBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    selectedSort: Int,
    selectedFilter: Int,
    sortOptions: List<String>,  // Opzioni di ordinamento
    filterOptions: List<String>,  // Opzioni di filtro
    onSortSelected: (Int) -> Unit,  // Callback per selezione ordinamento
    onFilterSelected: (Int) -> Unit,  // Callback per selezione filtro
    username: String?=null
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyVerticalGrid (
            columns = GridCells.Adaptive(minSize = 150.dp), // Adatta la griglia per min size
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)  // Spazio interno per gli elementi
        ) {
            // Sezione "Ordina per"
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(text =  stringResource(R.string.wishlists_sorting_by), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            items(items=sortOptions, key={it}) { option ->
                FilterChip(
                    label = { Text(option) },
                    onClick = { onSortSelected(sortOptions.indexOf(option)) },  // Seleziona ordinamento
                    selected = selectedSort == sortOptions.indexOf(option),
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)  // Spazio tra le chip
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.height(16.dp))
            }

                if(username.isNullOrBlank()){
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(text = stringResource(R.string.wishlists_filter_by), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(items = filterOptions, key={it}) { option ->
                        FilterChip(
                            label = { Text(option) },
                            onClick = { onFilterSelected(filterOptions.indexOf(option));  },  // Seleziona filtro
                            selected = selectedFilter == filterOptions.indexOf(option),
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)  // Spazio tra le chip
                        )
                    }}

        }
    }
}

@Composable
fun FilterChip(
    label: @Composable () -> Unit,
    onClick: () -> Unit,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    ElevatedFilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier
    )
}
@Composable
fun AddAWishlist(onClick: () -> Unit){
    Button(onClick = {onClick()}, modifier = Modifier.fillMaxWidth() )
    { Text(stringResource(R.string.wishlists_create)) }
}
@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistDialog(onDismiss: () -> Unit, onConfirm: (String, Boolean) -> Unit){
    var wishlistName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val public = stringResource(R.string.wishlists_public)
    val private = stringResource(R.string.wishlists_private)
    var selectedOption by remember { mutableStateOf(private) }
    val itemsMap = mapOf(
        public to true,
        private to false
    )
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val validNameRegex = "^[a-zA-Z0-9 ]{3,30}$".toRegex()
    val nameValidationMessage = stringResource(R.string.wishlists_name_validation)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wishlists_create)) },
        text = {
            Column {
                Text(stringResource(R.string.wishlists_ask_name))
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = wishlistName,
                    onValueChange = {
                        wishlistName = it; errorMessage = null
                    },
                    label = { Text(stringResource(R.string.wishlists_name)) },
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(stringResource(R.string.wishlists_ask_visibility))
                Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                            OutlinedTextField(
                                value = selectedOption,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.wishlists_select_visibility)) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            )


                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier =  Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            itemsMap.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.key) },

                                    onClick = {
                                        selectedOption = item.key
                                        expanded = false
                                    }
                                )
                            }

                    }
                }



            }},
        confirmButton = {
            Button(onClick = {
                if (!validNameRegex.matches(wishlistName)) {
                    errorMessage = nameValidationMessage
                } else {
                    itemsMap[selectedOption]?.let { onConfirm(wishlistName, it) }  // Pass the input text to the onConfirm lambda
                    onDismiss()  // Dismiss the dialog
                }
            }) {
                Text(stringResource(R.string.wishlists_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.wishlists_cancel))
            }
        }
    )

}


