package unical.enterpriceapplication.onlycards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.ui.icon.FontAwesomeBrandIcon
import unical.enterpriceapplication.onlycards.viewmodels.AuthViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import java.util.*

@Composable
fun LoginScreen(navHostController: NavHostController, authViewModel: AuthViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorState = authViewModel.error.collectAsState()
    val currentUser = authViewModel.currentUser.collectAsState(null)
    val context = LocalContext.current

    LaunchedEffect(currentUser.value) {
        if (currentUser.value!=null) {
            navHostController.navigate("home")
        }
    }

    Scaffold(
        topBar = { LoadingIndicator(authViewModel.isLoading.collectAsState()) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState, snackbar = { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LoginPage(
                navHostController,
                onLogin = { email, password ->
                    coroutineScope.launch {
                        authViewModel.basicLogin(email, password)

                        if (errorState.value == null) {
                            navHostController.navigate("account") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val message = errorState.value?.message ?: context.getString(R.string.generic_login_error)
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                },
                error = errorState,
                onOauthLogin = { provider ->
                    coroutineScope.launch {
                        authViewModel.oauthLogin(provider)
                        if (errorState.value == null) {
                            navHostController.navigate("account") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val message = errorState.value?.message ?: context.getString(R.string.generic_login_error)
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LoginPage(
    navHostController: NavHostController,
    onLogin: (String, String) -> Unit,
    error: State<ErrorData?>,
    onOauthLogin: (String) -> Unit
) {
    val emailState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        else -> stringResource(R.string.good_evening)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$greeting, ${stringResource(R.string.welcome_back)}!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            isError = error.value != null,
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text(stringResource(R.string.username_or_email)) },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = stringResource(R.string.email)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface
            )
        )

        OutlinedTextField(
            isError = error.value != null,
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text(stringResource(R.string.password)) },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = stringResource(R.string.password)) },
            trailingIcon = {
                val image = if (passwordVisible.value) {
                    Icons.Filled.Visibility
                } else {
                    Icons.Filled.VisibilityOff
                }
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(imageVector = image, contentDescription = stringResource(R.string.toggle_password_visibility))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.onSurface
            ),
            visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation()
        )

        Button(
            onClick = {
                onLogin(emailState.value, passwordState.value)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(stringResource(R.string.login), color = MaterialTheme.colorScheme.onSurface)
        }

        Text(stringResource(R.string.or), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 8.dp))

        Button(
            onClick = { onOauthLogin("google") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            FontAwesomeBrandIcon(unicode = "\uf1a0", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.login_with_google), color = MaterialTheme.colorScheme.onSurface)
        }

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .clickable { navHostController.navigate("register") },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.dont_have_account), color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.register_now), fontWeight = FontWeight.Bold, color = Color(0xFF00BFFF))
        }
    }
}

@Composable
fun LoadingIndicator(loading: State<Boolean>) {
    if (loading.value)
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}
