package pl.slownikjezykatrudnego.app.ui.lesson

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.slownikjezykatrudnego.app.domain.SessionManager
import pl.slownikjezykatrudnego.app.ui.components.SjtCard
import pl.slownikjezykatrudnego.app.ui.components.SjtSecondaryButton
import pl.slownikjezykatrudnego.app.ui.components.SjtTouchButton
import pl.slownikjezykatrudnego.app.ui.theme.SjtTheme

/**
 * Session completion summary screen with trophy, study metrics, and navigation actions.
 */
@Composable
fun SessionSummaryScreen(
    cardsReviewedCount: Int,
    streakDays: Int,
    completionMessage: SessionManager.CompletionMessage,
    onNavigateCatalog: () -> Unit,
    onNavigateStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SjtTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        SjtCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Trophy Icon Box
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(colors.brandPrimary, colors.brandPrimaryHover)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Title & Description
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = completionMessage.title,
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textSerifTitle,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = completionMessage.description,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                }

                // SRS Educational Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.badgeEmeraldBg.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, colors.badgeEmeraldBorder.copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.brandPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.brandPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "Algorytm powtórek dba o trwałe zapamiętywanie. Najlepsze efekty daje codzienna nauka.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Metrics Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgSurfaceElevated,
                    border = BorderStroke(1.dp, colors.borderDefault),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "PRZEJRZANE HASŁA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$cardsReviewedCount",
                                style = MaterialTheme.typography.displayLarge,
                                color = colors.textAmberBrand
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(width = 1.dp, height = 48.dp)
                                .background(colors.borderDefault)
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "SERIA NAUKI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textMuted,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$streakDays dni",
                                style = MaterialTheme.typography.displayLarge,
                                color = colors.textAmberBrand
                            )
                        }
                    }
                }

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SjtTouchButton(
                        text = "Przeglądaj Słowniczek",
                        onClick = onNavigateCatalog,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = colors.btnPrimaryText,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    SjtSecondaryButton(
                        text = "Zobacz Statystyki",
                        onClick = onNavigateStats,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}
