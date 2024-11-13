package unical.enterpriceapplication.onlycards.ui.privateProfile

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeIcon
import unical.enterpriceapplication.onlycards.viewmodels.SupportViewModel

@Composable
fun Support(supportViewModel: SupportViewModel) {
    // Variabili
    val scrollState = rememberScrollState() // stato di scorrimento
    val errorSnackbar = remember { SnackbarHostState() }    // stato del messaggio di errore
    val error by supportViewModel.error.collectAsState()   // Errore
    var isLoading = remember { mutableStateOf(false) }

    LaunchedEffect(error) {
        error?.let {
            launch {
                errorSnackbar.showSnackbar(it.message)
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(errorSnackbar) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor =  MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError ,
                )
            }
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Column(Modifier.verticalScroll(state = scrollState, enabled = !isLoading.value)) {
                Text(
                    text = stringResource(id = R.string.support_title),
                    modifier = Modifier.padding(start = 20.dp, top = 15.dp),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 5.dp))

                // Form di contatto
                SupportForm(supportViewModel, isLoading)
            }
        }
    }
}

@Composable
fun SupportForm(supportViewModel: SupportViewModel, isLoading: MutableState<Boolean>) {
    var emailObject by remember { mutableStateOf("") }
    var emailMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val localFocus = LocalFocusManager.current
    val context = LocalContext.current
    val isDeleting = remember { mutableStateOf(false) }
    val isSending = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = emailObject,
        onValueChange = { emailObject = it },
        label = {
            Row {
                Text(text = stringResource(id = R.string.support_object))
                Text(text = "*", color = Color.Red)
            }
        },
        leadingIcon = {
            FontAwesomeIcon(
                unicode = "\uf1dc",
                fontSize = 20.sp
            )
        },
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp),
        enabled = !isLoading.value
    )

    OutlinedTextField(
        value = emailMessage,
        onValueChange = { emailMessage = it },
        label = {
            Row {
                Text(text = stringResource(id = R.string.support_message))
                Text(text = "*", color = Color.Red)
            }
        },
        leadingIcon = {
            Box(
                modifier = Modifier.fillMaxHeight(),
                contentAlignment = Alignment.TopStart
            ) {
                FontAwesomeIcon(
                    unicode = "\uf15c",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        },
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp).height(200.dp),
        enabled = !isLoading.value
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp, top = 5.dp),
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    localFocus.clearFocus()

                    isLoading.value = true
                    isSending.value = true
                    val success = supportViewModel.sendHelpRequest(emailObject, emailMessage)

                    if (success) {
                        emailObject = ""
                        emailMessage = ""
                        Toast.makeText(context, context.getString(R.string.support_sent_success), Toast.LENGTH_SHORT).show()
                    } else
                        Toast.makeText(context, context.getString(R.string.support_sent_error), Toast.LENGTH_SHORT).show()

                    isLoading.value = false
                    isSending.value = false
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondaryContainer),
            enabled = !isLoading.value && emailObject.isNotEmpty() && emailMessage.isNotEmpty()
        ) {
            if (isSending.value) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            } else {
                Text(text = stringResource(id = R.string.support_send), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }

        Button (
            onClick = {
                isDeleting.value = true
                emailObject = ""
                emailMessage = ""
                isDeleting.value = false
            },
            modifier = Modifier.weight(1f).padding(start = 5.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error),
            enabled = !isLoading.value
        ) {
            if(isDeleting.value) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onError, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    text = stringResource(id = R.string.support_cancel),
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}