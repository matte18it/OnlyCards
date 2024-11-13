package unical.enterpriceapplication.onlycards.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import unical.enterpriceapplication.onlycards.viewmodels.UserViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserRegistrationData
import java.util.*

@Composable
fun RegisterScreen(navHostController: NavHostController, userViewModel: UserViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val error = userViewModel.error.collectAsState()
    val registrationSuccess = userViewModel.registrationSuccess.collectAsState()

    Scaffold(
        topBar = { RegisterLoadingIndicator(userViewModel.isLoading.collectAsState()) },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            RegisterPage(
                navHostController,
                onRegister = { name, email, phone, password ->
                    val userRegistrationData = UserRegistrationData(
                        username = name,
                        email = email,
                        phone = phone,
                        password = password
                    )

                    coroutineScope.launch {
                        val success = userViewModel.registerUser(userRegistrationData)

                        if (success) {
                            navHostController.navigate("account")
                            Toast.makeText(context, R.string.registration_success, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, R.string.registration_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun RegisterPage(navHostController: NavHostController, onRegister: (String, String, String, String) -> Unit) {
    val nameState = remember { mutableStateOf("") }
    val emailState = remember { mutableStateOf("") }
    val emailErrorState = remember { mutableStateOf(false) }
    val phoneState = remember { mutableStateOf("") }
    val phoneErrorState = remember { mutableStateOf(false) } // Stato di errore per il numero di telefono
    val passwordState = remember { mutableStateOf("") }
    val confirmPasswordState = remember { mutableStateOf("") }
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmPasswordVisible = remember { mutableStateOf(false) }
    val passwordFocused = remember { mutableStateOf(false) }
    val nameFocused = remember { mutableStateOf(false) }

    val hasMinLength = remember { mutableStateOf(false) }
    val hasLowercase = remember { mutableStateOf(false) }
    val hasUppercase = remember { mutableStateOf(false) }
    val hasNumber = remember { mutableStateOf(false) }
    val hasSpecialChar = remember { mutableStateOf(false) }
    val hasValidLength = remember { mutableStateOf(false) }

    val isPasswordRequirementsVisible = remember { mutableStateOf(false) }
    val isNameRequirementsVisible = remember { mutableStateOf(false) }

    val isFormValid = nameState.value.isNotBlank() &&
            emailState.value.isNotBlank() &&
            !emailErrorState.value &&
            !phoneErrorState.value && // Controlla che non ci sia un errore nel numero di telefono
            phoneState.value.isNotBlank() &&
            passwordState.value.isNotBlank() &&
            passwordState.value == confirmPasswordState.value &&
            hasMinLength.value && hasLowercase.value && hasUppercase.value && hasNumber.value && hasSpecialChar.value &&
            hasValidLength.value

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> stringResource(id = R.string.good_morning)
        in 12..17 -> stringResource(id = R.string.good_afternoon)
        else -> stringResource(id = R.string.good_evening)
    }

    // Funzione per validare il numero di telefono
    fun isValidPhoneNumber(phone: String): Boolean {
        val phoneRegex = "^\\+?[0-9]*\$" // Accetta solo numeri e un simbolo "+"
        return phone.matches(phoneRegex.toRegex())
    }

    fun updatePasswordRequirements(password: String) {
        hasMinLength.value = password.length >= 8
        hasLowercase.value = password.any { it.isLowerCase() }
        hasUppercase.value = password.any { it.isUpperCase() }
        hasNumber.value = password.any { it.isDigit() }
        hasSpecialChar.value = password.any { !it.isLetterOrDigit() }

        isPasswordRequirementsVisible.value = passwordFocused.value && !(hasMinLength.value && hasLowercase.value && hasUppercase.value && hasNumber.value && hasSpecialChar.value)
    }

    fun updateNameRequirements(name: String) {
        hasValidLength.value = name.length in 3..20
        isNameRequirementsVisible.value = nameFocused.value && !hasValidLength.value
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
        return email.matches(emailRegex.toRegex())
    }

    fun validateForm() {
        updateNameRequirements(nameState.value)
        updatePasswordRequirements(passwordState.value)

        phoneErrorState.value = !isValidPhoneNumber(phoneState.value) // Verifica il numero di telefono

        if (!hasValidLength.value) {
            isNameRequirementsVisible.value = true
        }
        if (!(hasMinLength.value && hasLowercase.value && hasUppercase.value && hasNumber.value && hasSpecialChar.value)) {
            isPasswordRequirementsVisible.value = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "$greeting, ${stringResource(id = R.string.welcome)}!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = nameState.value,
                onValueChange = {
                    nameState.value = it
                    updateNameRequirements(it)
                },
                label = { Text(stringResource(id = R.string.name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        nameFocused.value = focusState.isFocused
                        if (!focusState.isFocused && hasValidLength.value) {
                            isNameRequirementsVisible.value = false
                        }
                    },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = stringResource(id = R.string.name)) }
            )

            if (isNameRequirementsVisible.value) {
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PasswordRequirement(text = stringResource(id = R.string.name_length_requirement), satisfied = hasValidLength.value)
                    }
                }
            }

            OutlinedTextField(
                value = emailState.value,
                onValueChange = {
                    emailState.value = it
                    emailErrorState.value = !isValidEmail(it)
                },
                label = { Text(stringResource(id = R.string.email)) },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = stringResource(id = R.string.email)) },
                modifier = Modifier
                    .fillMaxWidth()
            )

            if (emailErrorState.value) {
                Text(
                    text = stringResource(id = R.string.invalid_email),
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            OutlinedTextField(
                value = phoneState.value,
                onValueChange = {
                    phoneState.value = it
                    phoneErrorState.value = !isValidPhoneNumber(it)
                },
                label = { Text(stringResource(id = R.string.phone)) },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = stringResource(id = R.string.phone)) },
                modifier = Modifier.fillMaxWidth(),
                isError = phoneErrorState.value
            )

            if (phoneErrorState.value) {
                Text(
                    text = stringResource(id = R.string.invalid_phone),
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            OutlinedTextField(
                value = passwordState.value,
                onValueChange = {
                    passwordState.value = it
                    updatePasswordRequirements(it)
                },
                label = { Text(stringResource(id = R.string.password)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = stringResource(id = R.string.password)) },
                trailingIcon = {
                    val image = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                        Icon(image, contentDescription = if (passwordVisible.value) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password))
                    }
                },
                visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { passwordFocused.value = it.isFocused }
            )

            if (isPasswordRequirementsVisible.value) {
                Card(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        PasswordRequirement(text = stringResource(id = R.string.password_min_length), satisfied = hasMinLength.value)
                        PasswordRequirement(text = stringResource(id = R.string.password_lowercase), satisfied = hasLowercase.value)
                        PasswordRequirement(text = stringResource(id = R.string.password_uppercase), satisfied = hasUppercase.value)
                        PasswordRequirement(text = stringResource(id = R.string.password_number), satisfied = hasNumber.value)
                        PasswordRequirement(text = stringResource(id = R.string.password_special_char), satisfied = hasSpecialChar.value)
                    }
                }
            }

            OutlinedTextField(
                value = confirmPasswordState.value,
                onValueChange = { confirmPasswordState.value = it },
                label = { Text(stringResource(id = R.string.confirm_password)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = stringResource(id = R.string.confirm_password)) },
                trailingIcon = {
                    val image = if (confirmPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible.value = !confirmPasswordVisible.value }) {
                        Icon(image, contentDescription = if (confirmPasswordVisible.value) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password))
                    }
                },
                visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    validateForm()
                    if (isFormValid) {
                        onRegister(nameState.value, emailState.value, phoneState.value, passwordState.value)
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(id = R.string.register))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Link per il login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .clickable { navHostController.navigate("login") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(stringResource(id = R.string.already_have_account), color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.login_now),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BFFF)
                )
            }
        }
    }
}

@Composable
fun PasswordRequirement(text: String, satisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (satisfied) Icons.Filled.Check else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (satisfied) Color.Green else Color.Red,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun RegisterLoadingIndicator(isLoadingState: State<Boolean>) {
    if (isLoadingState.value) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
    }
}
