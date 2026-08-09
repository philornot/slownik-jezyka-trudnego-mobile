package pl.slownikjezykatrudnego.app.ui.account

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slownikjezykatrudnego.app.ui.components.SjtTouchButton
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

/**
 * Account & Sync bottom sheet for offline-first state and cloud synchronization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountBottomSheet(
    deviceId: String,
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
                .padding(bottom = 24.dp)
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
                        text = "Profil i Synchronizacja",
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

            // Local Offline Mode Status Card
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
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = colors.badgeEmeraldText,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Działa w 100% lokalnie (Offline-First)",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.badgeEmeraldText
                        )
                        Text(
                            text = "Twój postęp jest bezpiecznie zapisywany w pamięci Twojego urządzenia.",
                            fontSize = 11.5.sp,
                            color = colors.textPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Current Device Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "URZĄDZENIE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.textMuted,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = colors.brandPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Natywna aplikacja Android",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "ID urządzenia: ${deviceId.take(8)}...",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }
                }
            }

            SjtTouchButton(
                text = "Zamknij",
                onClick = onDismiss
            )
        }
    }
}
