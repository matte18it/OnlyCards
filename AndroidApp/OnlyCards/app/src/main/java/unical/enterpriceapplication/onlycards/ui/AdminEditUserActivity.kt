package unical.enterpriceapplication.onlycards.ui

import android.provider.Settings.Global.getString
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import android.widget.Toast
import androidx.compose.material.MaterialTheme.colors
import unical.enterpriceapplication.onlycards.viewmodels.UserViewModel
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.R

@Composable
fun EditUserScreen(
    userViewModel: UserViewModel,
    navHostController: NavHostController,
    userId: String
) {
    val context = LocalContext.current

    val isLoading by userViewModel.isLoading.collectAsState()
    val error by userViewModel.error.collectAsState()

    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var operationType by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        userViewModel.loadUserDetails(userId)
        userViewModel.loadUserRoles(userId)
    }

    val selectedUser by userViewModel.selectedUser.collectAsState()
    val selectedUserRoles by userViewModel.selectedUserRoles.collectAsState()

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cellphoneNumber by remember { mutableStateOf("") }
    var blocked by remember { mutableStateOf(false) }

    var isAdmin by remember { mutableStateOf(false) }
    var isBuyer by remember { mutableStateOf(false) }
    var isSeller by remember { mutableStateOf(false) }

    var usernameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var rolesError by remember { mutableStateOf(false) }



    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$"
        return email.matches(emailRegex.toRegex())
    }

    fun validateFields() {
        usernameError = username.length < 3
        emailError = !isValidEmail(email)
        rolesError = !(isAdmin || isBuyer || isSeller)
    }

    LaunchedEffect(selectedUser) {
        selectedUser?.let {
            username = it.username
            email = it.email
            cellphoneNumber = it.cellphoneNumber ?: ""
            blocked = it.blocked

            isAdmin = selectedUserRoles.any { role -> role.name == "ROLE_ADMIN" }
            isBuyer = selectedUserRoles.any { role -> role.name == "ROLE_BUYER" }
            isSeller = selectedUserRoles.any { role -> role.name == "ROLE_SELLER" }

            validateFields()
        }
    }

    val isFormValid = !usernameError && !emailError && !rolesError && username.isNotBlank() && email.isNotBlank()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && !showSuccessMessage && !showErrorMessage) {
                Text(
                    text = stringResource(id = R.string.error, error?.message ?: ""),
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                selectedUser?.let { user ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = username,
                                onValueChange = {
                                    username = it
                                    validateFields()
                                },
                                label = { Text(stringResource(id = R.string.username_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = usernameError
                            )
                            if (usernameError) {
                                Text(
                                    text = stringResource(id = R.string.username_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    validateFields()
                                },
                                label = { Text(stringResource(id = R.string.email_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                isError = emailError
                            )
                            if (emailError) {
                                Text(
                                    text = stringResource(id = R.string.email_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = cellphoneNumber,
                                onValueChange = { cellphoneNumber = it },
                                label = { Text(stringResource(id = R.string.phone_label)) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = stringResource(id = R.string.user_status))
                            Row {
                                RadioButton(
                                    selected = !blocked,
                                    onClick = { blocked = false }
                                )
                                Text(stringResource(id = R.string.unblocked_status), modifier = Modifier.align(Alignment.CenterVertically))

                                Spacer(modifier = Modifier.width(16.dp))

                                RadioButton(
                                    selected = blocked,
                                    onClick = { blocked = true }
                                )
                                Text(stringResource(id = R.string.blocked_status), modifier = Modifier.align(Alignment.CenterVertically))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = stringResource(id = R.string.user_roles), fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isAdmin,
                                    onClick = {
                                        isAdmin = !isAdmin
                                        validateFields()
                                    },
                                    enabled = false
                                )
                                Text(stringResource(id = R.string.admin_role))

                                Spacer(modifier = Modifier.width(16.dp))

                                RadioButton(
                                    selected = isBuyer,
                                    onClick = {
                                        if (!isAdmin) {
                                            isBuyer = !isBuyer
                                            validateFields()
                                        }
                                    },
                                    enabled = !isAdmin
                                )
                                Text(stringResource(id = R.string.buyer_role))

                                Spacer(modifier = Modifier.width(16.dp))

                                RadioButton(
                                    selected = isSeller,
                                    onClick = {
                                        if (!isAdmin) {
                                            isSeller = !isSeller
                                            validateFields()
                                        }
                                    },
                                    enabled = !isAdmin
                                )
                                Text(stringResource(id = R.string.seller_role))
                            }


                            if (rolesError) {
                                Text(
                                    text = stringResource(id = R.string.roles_error),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(text = stringResource(id = R.string.addresses_label), fontWeight = FontWeight.Bold)
                            user.addresses.forEach { address ->
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text("- ${stringResource(id = R.string.name)}: ${address.name} ${address.surname}")
                                    Text("- ${stringResource(id = R.string.phone_label)}: ${address.telephoneNumber ?: "N/A"}")
                                    Text("- ${stringResource(id = R.string.address_label_street)}: ${address.street}, ${address.city}, ${address.state}")
                                    Text("- ${stringResource(id = R.string.address_label_cap)}: ${address.zip}")
                                    Text("- ${stringResource(id = R.string.default_address)}: ${if (address.defaultAddress) "Sì" else "No"}")
                                    Text("- ${stringResource(id = R.string.weekend_delivery)}: ${if (address.weekendDelivery) "Sì" else "No"}")
                                    Divider()
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Lancia una coroutine per chiamare la funzione sospesa deleteUser
                                        coroutineScope.launch {
                                            operationType = "delete"
                                            showSuccessMessage = false
                                            showErrorMessage = false

                                            val result = userViewModel.deleteUser(userId)
                                            result.fold(
                                                onSuccess = {
                                                    Toast.makeText(context, context.getString(R.string.delete_user_success), Toast.LENGTH_LONG).show()
                                                    navHostController.popBackStack()
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, context.getString(R.string.operation_failed, it.message ?: ""), Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text(stringResource(id = R.string.delete_user), color = MaterialTheme.colorScheme.onError)
                                }

                                Button(
                                    onClick = { navHostController.popBackStack() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.onSecondary)
                                }

                                Button(
                                    onClick = {
                                        operationType = "save"
                                        showSuccessMessage = false
                                        showErrorMessage = false

                                        val updatedUser = user.copy(
                                            username = username,
                                            email = email,
                                            cellphoneNumber = cellphoneNumber,
                                            blocked = blocked,
                                            roles = mutableSetOf<UserData.Role>().apply {
                                                if (isAdmin) add(UserData.Role("ROLE_ADMIN"))
                                                if (isBuyer) add(UserData.Role("ROLE_BUYER"))
                                                if (isSeller) add(UserData.Role("ROLE_SELLER"))
                                            }
                                        )

                                        coroutineScope.launch {
                                            val result = userViewModel.updateUser(updatedUser)
                                            result.fold(
                                                onSuccess = {
                                                    Toast.makeText(context, context.getString(R.string.save_user_success), Toast.LENGTH_LONG).show()
                                                    navHostController.popBackStack()
                                                },
                                                onFailure = {
                                                    Toast.makeText(context, context.getString(R.string.operation_failed, it.message ?: ""), Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        }
                                    },
                                    enabled = isFormValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(stringResource(id = R.string.save_user), color = MaterialTheme.colorScheme.onPrimary)
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
