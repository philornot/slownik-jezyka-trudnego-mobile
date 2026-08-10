package com.philornot.slownikjezykatrudnego.ui.account

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.R
import com.philornot.slownikjezykatrudnego.ui.components.SjtTouchButton
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Authentication Bottom Sheet providing email/password login, registration,
 * and Google Sign-In via Credential Manager.
 *
 * @param onSignInWithEmail   Callback for email+password sign-in attempt.
 * @param onRegisterWithEmail Callback for email+password registration attempt.
 * @param onSignInWithGoogle  Callback for Google Sign-In attempt.
 * @param onOpenPrivacy       Callback to open the Privacy Policy bottom sheet.
 * @param onDismiss           Callback when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onSignInWithEmail: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onRegisterWithEmail: (email: String, password: String, onError: (String) -> Unit) -> Unit,
    onSignInWithGoogle: (onError: (String) -> Unit) -> Unit,
    onOpenPrivacy: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SjtTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val focusManager = LocalFocusManager.current

    var mode by remember { mutableStateOf<AuthMode>(AuthMode.Login) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.brandPrimary
                    )
                    Text(
                        text = if (mode == AuthMode.Login) "Zaloguj się" else "Utwórz konto",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSerifTitle
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Zamknij",
                        tint = colors.textMuted
                    )
                }
            }

            Text(
                text = if (mode == AuthMode.Login)
                    "Zaloguj się, by synchronizować postępy między urządzeniami."
                else
                    "Utwórz bezpłatne konto, by synchronizować postępy między urządzeniami.",
                fontSize = 13.sp,
                color = colors.textMuted,
                lineHeight = 18.sp
            )

            // Google Sign-In Button
            Surface(
                onClick = {
                    isGoogleLoading = true
                    errorMessage = null
                    onSignInWithGoogle { err ->
                        errorMessage = err
                        isGoogleLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                enabled = !isLoading && !isGoogleLoading
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGoogleLoading) {
                        Text(
                            "Łączenie z Google…",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    } else {
                        // Google "G" logo text representation
                        Text(
                            text = "G",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.brandPrimary,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = "Kontynuuj z Google",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderDefault)
                Text("lub", fontSize = 12.sp, color = colors.textMuted)
                HorizontalDivider(modifier = Modifier.weight(1f), color = colors.borderDefault)
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Adres e-mail") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = colors.textMuted)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.brandPrimary,
                    unfocusedBorderColor = colors.borderDefault,
                    focusedLabelColor = colors.brandPrimary,
                    unfocusedLabelColor = colors.textMuted,
                    cursorColor = colors.brandPrimary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.bgSurface,
                    unfocusedContainerColor = colors.bgSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Hasło") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = colors.textMuted)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Ukryj hasło" else "Pokaż hasło",
                            tint = colors.textMuted
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (mode == AuthMode.Register) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.clearFocus() }
                ),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.brandPrimary,
                    unfocusedBorderColor = colors.borderDefault,
                    focusedLabelColor = colors.brandPrimary,
                    unfocusedLabelColor = colors.textMuted,
                    cursorColor = colors.brandPrimary,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.bgSurface,
                    unfocusedContainerColor = colors.bgSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Confirm Password (register mode only)
            AnimatedVisibility(visible = mode == AuthMode.Register) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = null },
                    label = { Text("Powtórz hasło") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = colors.textMuted)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.brandPrimary,
                        unfocusedBorderColor = colors.borderDefault,
                        focusedLabelColor = colors.brandPrimary,
                        unfocusedLabelColor = colors.textMuted,
                        cursorColor = colors.brandPrimary,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.bgSurface,
                        unfocusedContainerColor = colors.bgSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Error message
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.badgeRoseBg,
                    border = BorderStroke(1.dp, colors.badgeRoseBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 12.5.sp,
                        color = colors.badgeRoseText,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // Success message
            AnimatedVisibility(visible = successMessage != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.badgeEmeraldBg,
                    border = BorderStroke(1.dp, colors.badgeEmeraldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage ?: "",
                        fontSize = 12.5.sp,
                        color = colors.badgeEmeraldText,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // Primary action button
            SjtTouchButton(
                text = if (isLoading) "Przetwarzanie…"
                       else if (mode == AuthMode.Login) "Zaloguj się"
                       else "Utwórz konto",
                onClick = {
                    errorMessage = null
                    focusManager.clearFocus()

                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Wypełnij wszystkie pola."
                        return@SjtTouchButton
                    }
                    if (mode == AuthMode.Register && password != confirmPassword) {
                        errorMessage = "Hasła nie są identyczne."
                        return@SjtTouchButton
                    }

                    isLoading = true
                    if (mode == AuthMode.Login) {
                        onSignInWithEmail(email, password) { err ->
                            errorMessage = err
                            isLoading = false
                        }
                    } else {
                        onRegisterWithEmail(email, password) { err ->
                            errorMessage = err
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isGoogleLoading
            )

            // Mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == AuthMode.Login) "Nie masz konta? " else "Masz już konto? ",
                    fontSize = 13.sp,
                    color = colors.textMuted
                )
                Surface(
                    onClick = {
                        mode = if (mode == AuthMode.Login) AuthMode.Register else AuthMode.Login
                        errorMessage = null
                        confirmPassword = ""
                    },
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    Text(
                        text = if (mode == AuthMode.Login) "Zarejestruj się" else "Zaloguj się",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.brandPrimary
                    )
                }
            }

            // Privacy policy link
            Text(
                text = "Rejestrując się, akceptujesz Politykę Prywatności",
                fontSize = 11.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private enum class AuthMode { Login, Register }
