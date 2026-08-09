package pl.slownikjezykatrudnego.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
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
 * Privacy policy and RODO information bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyBottomSheet(
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = colors.brandPrimary
                    )
                    Text(
                        text = "Polityka Prywatności",
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

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.bgSurfaceElevated,
                border = BorderStroke(1.dp, colors.borderDefault),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. Zbieranie i przechowywanie danych",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Aplikacja Słownik Języka Trudnego przechowuje postęp nauki (odpowiedzi w quizach, stopnie zapamiętania słów, seria dni) lokalnie na Twoim urządzeniu. Aplikacja nie przekazuje Twoich danych osobowych podmiotom trzecim bez Twojej wyraźnej zgody.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "2. Prawa użytkownika (RODO)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "W każdej chwili masz prawo do zresetowania lub usunięcia swoich danych w menu Ustawienia za pomocą przycisku 'Resetuj postępy nauki'.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "3. Prawa autorskie i źródła",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Definicje i przykłady użycia zostały opracowane na podstawie ogólnodostępnych źródeł leksykograficznych, m.in. Słownika Języka Polskiego PWN, do którego odsyłają bezpośrednie linki w kartach słów.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        lineHeight = 17.sp
                    )
                }
            }

            SjtTouchButton(
                text = "Rozumiem",
                onClick = onDismiss
            )
        }
    }
}
