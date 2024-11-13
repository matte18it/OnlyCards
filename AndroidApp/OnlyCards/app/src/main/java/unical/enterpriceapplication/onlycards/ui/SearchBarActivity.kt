package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.SearchHistoryViewModel
import android.view.KeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.res.stringResource
import unical.enterpriceapplication.onlycards.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchHistoryViewModel: SearchHistoryViewModel,
    navHostController: NavHostController,
    searchQuery: MutableState<String>,
    onSearch: (String) -> Unit,
    onValueChange: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val list = searchHistoryViewModel.searchHistory.collectAsState(initial = emptyList())
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    SearchBar(
        inputField = {
            OutlinedTextField(
                shape = RoundedCornerShape(15.dp),
                value = searchQuery.value,
                onValueChange = { newText ->
                    onValueChange(newText)
                },
                placeholder = { Text(text = stringResource(id = R.string.search_text)) },
                trailingIcon = {
                    FontAwesomeIcon(
                        unicode = "\uf002",
                        fontSize = 18.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedBorderColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp, end = 5.dp)
                    .onPreviewKeyEvent { event ->
                        if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                            isExpanded = false
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            true
                        } else {
                            false
                        }
                    }
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        if (isFocused) {
                            onValueChange("")
                        }
                    },
                singleLine = true,
                keyboardActions = KeyboardActions(
                    onDone = {
                        isExpanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        if (searchQuery.value.isNotBlank()) {
                            searchHistoryViewModel.insert(searchQuery.value)
                        }
                        onSearch(searchQuery.value)
                    }
                ),
            )
        },
        content = {
            LazyColumn {
                items(items = list.value) { item ->
                    key(item) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable {
                                    onSearch(item.search)
                                    isExpanded = false
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FontAwesomeIcon(
                                unicode = "\uf1da", // Clock icon
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Text(text = item.search)
                            Spacer(modifier = Modifier.weight(1f))
                            FontAwesomeIcon(
                                unicode = "\uf00d", // Delete icon
                                modifier = Modifier.clickable {
                                    searchHistoryViewModel.delete(item)
                                },
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        },
        expanded = isExpanded,
        onExpandedChange = { expanded ->
            if (!expanded) {
                onValueChange("")
            }
        },
        modifier = Modifier.onFocusChanged { focusState ->
            isExpanded = focusState.isFocused
        },
        colors = SearchBarDefaults.colors(
            containerColor = if (isFocused) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.background,
            dividerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}
