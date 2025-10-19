package com.example.autostradaauctions.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.autostradaauctions.data.repository.AdminUser
import com.example.autostradaauctions.data.repository.UpdateUserRequest
import com.example.autostradaauctions.data.repository.CreateUserRequest

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun UserFormDialog(
    user: AdminUser?,
    formMode: UserFormMode,
    onDismiss: () -> Unit,
    onSave: (UpdateUserRequest?, CreateUserRequest?) -> Unit
) {
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var username by remember { mutableStateOf(user?.username ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "Buyer") }
    var isEmailVerified by remember { mutableStateOf(user?.isEmailVerified ?: false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    var firstNameError by remember { mutableStateOf("") }
    var lastNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var usernameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    
    val roles = listOf("Admin", "Seller", "Buyer")
    
    fun validateForm(): Boolean {
        firstNameError = if (firstName.isBlank()) "First name is required" else ""
        lastNameError = if (lastName.isBlank()) "Last name is required" else ""
        emailError = if (email.isBlank()) "Email is required" 
                     else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email format"
                     else ""
        usernameError = if (username.isBlank()) "Username is required" else ""
        
        passwordError = when {
            formMode == UserFormMode.CREATE && newPassword.isBlank() -> "Password is required"
            formMode == UserFormMode.EDIT && newPassword.isNotEmpty() && newPassword.length < 6 -> "Password must be at least 6 characters"
            newPassword.isNotEmpty() && newPassword != confirmPassword -> "Passwords don't match"
            else -> ""
        }
        
        return firstNameError.isEmpty() && lastNameError.isEmpty() && 
               emailError.isEmpty() && usernameError.isEmpty() && passwordError.isEmpty()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (formMode == UserFormMode.CREATE) "Create User" else "Edit User",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // First Name
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { 
                        firstName = it
                        firstNameError = ""
                    },
                    label = { Text("First Name") },
                    isError = firstNameError.isNotEmpty(),
                    supportingText = if (firstNameError.isNotEmpty()) {{ Text(firstNameError) }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Last Name
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { 
                        lastName = it
                        lastNameError = ""
                    },
                    label = { Text("Last Name") },
                    isError = lastNameError.isNotEmpty(),
                    supportingText = if (lastNameError.isNotEmpty()) {{ Text(lastNameError) }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = ""
                    },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = emailError.isNotEmpty(),
                    supportingText = if (emailError.isNotEmpty()) {{ Text(emailError) }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        usernameError = ""
                    },
                    label = { Text("Username") },
                    isError = usernameError.isNotEmpty(),
                    supportingText = if (usernameError.isNotEmpty()) {{ Text(usernameError) }} else null,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Role Dropdown
                var roleExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        roles.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    role = selectionOption
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Email Verified Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Email Verified")
                    Switch(
                        checked = isEmailVerified,
                        onCheckedChange = { isEmailVerified = it }
                    )
                }
                
                // Password fields
                if (formMode == UserFormMode.CREATE || formMode == UserFormMode.EDIT) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { 
                            newPassword = it
                            passwordError = ""
                        },
                        label = { Text(if (formMode == UserFormMode.CREATE) "Password" else "New Password (leave blank to keep current)") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        },
                        isError = passwordError.isNotEmpty(),
                        supportingText = if (passwordError.isNotEmpty()) {{ Text(passwordError) }} else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (newPassword.isNotEmpty()) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                passwordError = ""
                            },
                            label = { Text("Confirm Password") },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (validateForm()) {
                                when (formMode) {
                                    UserFormMode.CREATE -> {
                                        val createRequest = CreateUserRequest(
                                            firstName = firstName,
                                            lastName = lastName,
                                            email = email,
                                            username = username,
                                            password = newPassword,
                                            role = role,
                                            isEmailVerified = isEmailVerified
                                        )
                                        onSave(null, createRequest)
                                    }
                                    UserFormMode.EDIT -> {
                                        val updateRequest = UpdateUserRequest(
                                            firstName = firstName,
                                            lastName = lastName,
                                            email = email,
                                            username = username,
                                            role = role,
                                            isEmailVerified = isEmailVerified,
                                            newPassword = newPassword
                                        )
                                        onSave(updateRequest, null)
                                    }
                                }
                            }
                        }
                    ) {
                        Text(if (formMode == UserFormMode.CREATE) "Create" else "Save")
                    }
                }
            }
        }
    }
}