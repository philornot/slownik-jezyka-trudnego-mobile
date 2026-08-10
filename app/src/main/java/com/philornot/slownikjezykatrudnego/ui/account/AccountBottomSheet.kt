package com.philornot.slownikjezykatrudnego.ui.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.philornot.slownikjezykatrudnego.data.model.AuthState
import com.philornot.slownikjezykatrudnego.data.model.DeviceSession
import com.philornot.slownikjezykatrudnego.data.model.UserProfile
import com.philornot.slownikjezykatrudnego.ui.components.SjtTouchButton
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Account & Sync bottom sheet with three states:
 * 1. Unauthenticated — prompt to log in / register
 * 2. Loading — sync in progress indicator
 * 3. Authenticated — full profile view with devices, settings, danger zone
 *
 * @param authState       Current Firebase authentication state.
 * @param userProfile     Loaded user profile (may be null while loading).
 * @param isSyncing       True while a cloud sync operation is in progress.
 * @param currentDeviceId The device ID of this device (for highlighting in device list).
 * @param onOpenAuth      Callback to open the authentication bottom sheet.
 * @param onSignOut       Callback to sign out the current user.
 * @param onSaveUsername  Callback to save a new display username.
 * @param onLogoutAllDevices Callback to revoke all device sessions.
 * @param onDeleteAccount Callback to permanently delete the account.
 * @param onDismiss       Callback when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBottomSheet(
    authState: AuthState,
    userProfile: UserProfile?,
    isSyncing: Boolean,
    currentDeviceId: String,
    onOpenAuth: () -> Unit,
    onSignOut: () -> Unit,
    onSaveUsername: (String, () -> Unit, (String) -> Unit) -> Unit,
    onLogoutAllDevices: (() -> Unit, (String) -> Unit) -> Unit,
    onDeleteAccount: (() -> Unit, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SjtTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.bgSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 28.dp)
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
                    Icon(Icons.Default.Person, contentDescription = null, tint = colors.brandPrimary)
                    Text(
                        text = "Profil i Synchronizacja",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textSerifTitle
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Zamknij", tint = colors.textMuted)
                }
            }

            AnimatedContent(
                targetState = authState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "accountState"
            ) { state ->
                when (state) {
                    AuthState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.brandPrimary)
                        }
                    }

                    AuthState.Unauthenticated -> {
                        UnauthenticatedContent(
                            onOpenAuth = onOpenAuth,
                            currentDeviceId = currentDeviceId
                        )
                    }

                    is AuthState.Authenticated -> {
                        AuthenticatedContent(
                            user = state,
                            userProfile = userProfile,
                            isSyncing = isSyncing,
                            currentDeviceId = currentDeviceId,
                            onSignOut = onSignOut,
                            onSaveUsername = onSaveUsername,
                            onLogoutAllDevices = onLogoutAllDevices,
                            onDeleteAccount = onDeleteAccount
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UnauthenticatedContent(
    onOpenAuth: () -> Unit,
    currentDeviceId: String
) {
    val colors = SjtTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.bgSurfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Konto użytkownika",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSerifTitle
                )
                Text(
                    text = "Zaloguj się lub utwórz konto, aby synchronizować postępy w nauce między urządzeniami i bezpiecznie przechowywać kopię zapasową w chmurze.",
                    fontSize = 13.sp,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        SjtTouchButton(
            text = "Zaloguj się / Zarejestruj",
            onClick = onOpenAuth,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "ID urządzenia: ${currentDeviceId.take(8)}…",
            fontSize = 11.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun AuthenticatedContent(
    user: AuthState.Authenticated,
    userProfile: UserProfile?,
    isSyncing: Boolean,
    currentDeviceId: String,
    onSignOut: () -> Unit,
    onSaveUsername: (String, () -> Unit, (String) -> Unit) -> Unit,
    onLogoutAllDevices: (() -> Unit, (String) -> Unit) -> Unit,
    onDeleteAccount: (() -> Unit, (String) -> Unit) -> Unit
) {
    val colors = SjtTheme.colors
    val focusManager = LocalFocusManager.current

    var editingUsername by remember { mutableStateOf(false) }
    var usernameInput by remember { mutableStateOf(userProfile?.username ?: "") }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var usernameSuccess by remember { mutableStateOf<String?>(null) }

    var showLogoutAllConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteConfirmText by remember { mutableStateOf("") }
    var actionError by remember { mutableStateOf<String?>(null) }
    var isActionLoading by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Sync status
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.badgeEmeraldBg.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, colors.badgeEmeraldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.badgeEmeraldText
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = colors.badgeEmeraldText,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = if (isSyncing) "Synchronizacja w toku…" else "Synchronizacja włączona",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.badgeEmeraldText
                    )
                    Text(
                        text = user.user.email ?: "Konto Google",
                        fontSize = 11.5.sp,
                        color = colors.textPrimary
                    )
                }
            }
        }

        // Username section
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.bgSurfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NAZWA UŻYTKOWNIKA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textMuted,
                    letterSpacing = 0.8.sp
                )

                if (editingUsername) {
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it; usernameError = null },
                        label = { Text("Nazwa użytkownika") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                        shape = RoundedCornerShape(10.dp)
                    )

                    usernameError?.let {
                        Text(it, fontSize = 11.sp, color = colors.badgeRoseText)
                    }
                    usernameSuccess?.let {
                        Text(it, fontSize = 11.sp, color = colors.badgeEmeraldText)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SjtTouchButton(
                            text = "Zapisz",
                            onClick = {
                                val trimmed = usernameInput.trim()
                                if (trimmed.length < 2) {
                                    usernameError = "Minimum 2 znaki."
                                    return@SjtTouchButton
                                }
                                onSaveUsername(trimmed, {
                                    usernameSuccess = "Zapisano!"
                                    editingUsername = false
                                }, { err ->
                                    usernameError = err
                                })
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            onClick = { editingUsername = false; usernameError = null },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurfaceMuted,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                Text("Anuluj", fontSize = 13.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userProfile?.username ?: user.user.displayName ?: "Brak nazwy",
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Surface(
                            onClick = {
                                usernameInput = userProfile?.username ?: user.user.displayName ?: ""
                                editingUsername = true
                            },
                            color = androidx.compose.ui.graphics.Color.Transparent
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edytuj nazwę",
                                tint = colors.brandPrimary,
                                modifier = Modifier.padding(4.dp).size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Devices
        val devices = userProfile?.devices?.values?.sortedByDescending { it.isCurrent } ?: emptyList()
        if (devices.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "URZĄDZENIA (${devices.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.textMuted,
                            letterSpacing = 0.8.sp
                        )
                    }

                    devices.forEachIndexed { i, device ->
                        if (i > 0) HorizontalDivider(color = colors.borderMuted, thickness = 0.5.dp)
                        DeviceRow(device = device, isCurrentDevice = device.id == currentDeviceId)
                    }
                }
            }
        }

        // Action error
        actionError?.let { err ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = colors.badgeRoseBg,
                border = BorderStroke(1.dp, colors.badgeRoseBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = err,
                    fontSize = 12.sp,
                    color = colors.badgeRoseText,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        HorizontalDivider(color = colors.borderMuted)

        // Sign out
        Surface(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.bgSurfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
                Text("Wyloguj się z tego urządzenia", fontSize = 13.5.sp, color = colors.textPrimary)
            }
        }

        // Logout all devices
        if (!showLogoutAllConfirm) {
            Surface(
                onClick = { showLogoutAllConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.badgeAmberBg.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, colors.badgeAmberBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = colors.badgeAmberText, modifier = Modifier.size(20.dp))
                    Text("Wyloguj wszystkie urządzenia", fontSize = 13.5.sp, color = colors.badgeAmberText)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.badgeAmberBg.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, colors.badgeAmberBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Czy na pewno wylogować wszystkie urządzenia?", fontSize = 13.sp, color = colors.badgeAmberText, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SjtTouchButton(
                            text = if (isActionLoading) "…" else "Wyloguj wszystkie",
                            onClick = {
                                isActionLoading = true
                                onLogoutAllDevices({
                                    isActionLoading = false
                                    showLogoutAllConfirm = false
                                }, { err ->
                                    actionError = err
                                    isActionLoading = false
                                    showLogoutAllConfirm = false
                                })
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isActionLoading
                        )
                        Surface(
                            onClick = { showLogoutAllConfirm = false },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurfaceMuted,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                Text("Anuluj", fontSize = 13.sp, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Delete account
        if (!showDeleteConfirm) {
            Surface(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = colors.badgeRoseBg.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, colors.badgeRoseBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = colors.badgeRoseText, modifier = Modifier.size(20.dp))
                    Text("Usuń konto i dane", fontSize = 13.5.sp, color = colors.badgeRoseText)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.badgeRoseBg.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, colors.badgeRoseBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = colors.badgeRoseText, modifier = Modifier.size(18.dp))
                        Text("Ta operacja jest nieodwracalna!", fontSize = 13.sp, color = colors.badgeRoseText, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "Wpisz \"USUŃ\" aby potwierdzić usunięcie konta i wszystkich danych.",
                        fontSize = 12.sp, color = colors.textPrimary, lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = deleteConfirmText,
                        onValueChange = { deleteConfirmText = it },
                        label = { Text("Wpisz USUŃ") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.badgeRoseText,
                            unfocusedBorderColor = colors.badgeRoseBorder,
                            focusedLabelColor = colors.badgeRoseText,
                            unfocusedLabelColor = colors.textMuted,
                            cursorColor = colors.badgeRoseText,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedContainerColor = colors.bgSurface,
                            unfocusedContainerColor = colors.bgSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = {
                                if (deleteConfirmText == "USUŃ") {
                                    isActionLoading = true
                                    onDeleteAccount({
                                        isActionLoading = false
                                        showDeleteConfirm = false
                                    }, { err ->
                                        actionError = err
                                        isActionLoading = false
                                        showDeleteConfirm = false
                                    })
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (deleteConfirmText == "USUŃ") colors.badgeRoseBg else colors.bgSurfaceMuted,
                            border = BorderStroke(1.dp, colors.badgeRoseBorder),
                            modifier = Modifier.weight(1f),
                            enabled = !isActionLoading
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                Text(
                                    if (isActionLoading) "Usuwanie…" else "Usuń konto",
                                    fontSize = 13.sp,
                                    color = colors.badgeRoseText,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Surface(
                            onClick = { showDeleteConfirm = false; deleteConfirmText = "" },
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurfaceMuted,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(12.dp)) {
                                Text("Anuluj", fontSize = 13.sp, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceSession, isCurrentDevice: Boolean) {
    val colors = SjtTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isCurrentDevice) Icons.Default.Smartphone else Icons.Default.Devices,
            contentDescription = null,
            tint = if (isCurrentDevice) colors.brandPrimary else colors.textMuted,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = device.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                if (isCurrentDevice) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colors.badgeEmeraldBg,
                        border = BorderStroke(1.dp, colors.badgeEmeraldBorder)
                    ) {
                        Text(
                            "To urządzenie",
                            fontSize = 10.sp,
                            color = colors.badgeEmeraldText,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = "Ostatnia aktywność: ${device.lastActive.take(10)}",
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }
    }
}
