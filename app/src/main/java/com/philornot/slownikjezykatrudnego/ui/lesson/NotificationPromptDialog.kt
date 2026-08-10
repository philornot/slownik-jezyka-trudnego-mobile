package com.philornot.slownikjezykatrudnego.ui.lesson

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.philornot.slownikjezykatrudnego.ui.components.SjtTouchButton
import com.philornot.slownikjezykatrudnego.ui.theme.SjtTheme

/**
 * Dialog asking the user to enable daily study notifications after completing their first lesson.
 *
 * @param onEnable  Callback when user agrees to enable notifications.
 * @param onDismiss Callback when user declines or dismisses the prompt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPromptDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SjtTheme.colors

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onEnable()
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = colors.bgSurfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bell icon in styled circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = colors.brandPrimary.copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = colors.brandPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Title
                Text(
                    text = "Włączyć codzienne powiadomienia?",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textSerifTitle,
                    textAlign = TextAlign.Center
                )

                // Description
                Text(
                    text = "Świetna robota z ukończeniem sesji! Codzienna, krótka powtórka pozwala najszybciej utrwalić trudne słówka w pamięci długotrwałej. Czy chcesz otrzymywać jedno krótkie przypomnienie dziennie?",
                    fontSize = 13.5.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                SjtTouchButton(
                    text = "Włącz powiadomienia",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onEnable()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Może później",
                        color = colors.textMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
