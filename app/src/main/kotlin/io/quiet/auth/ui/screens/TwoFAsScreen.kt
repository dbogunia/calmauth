package io.quiet.auth.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.R
import io.quiet.auth.domain.formatLiveTotpCode
import io.quiet.auth.domain.secondsLeftForPeriod
import io.quiet.auth.ui.EmptyStateText
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.QuietDivider
import io.quiet.auth.ui.theme.SoftInk
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

private fun formatTotpCodeWithGap(code: String): String {
    val digits = code.filter { it.isDigit() }
    return if (digits.length == 6) {
        "${digits.take(3)} ${digits.drop(3)}"
    } else {
        code
    }
}

@Composable
fun TwoFAsScreen(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    onTokenClick: (String) -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val nowMs by useCurrentTime()

    LaunchedEffect(pin.isLoading, pin.isPinEnabled, pin.isUnlocked) {
        if (!pin.isLoading && pin.isPinEnabled && !pin.isUnlocked) {
            // MainTabHost handles onLocked
        }
    }

    val emptyMessage = when {
        pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked) -> stringResource(R.string.unlockingApp)
        twoFA.isLoading -> stringResource(R.string.loadingAccounts)
        else -> stringResource(R.string.emptyAccounts)
    }

    val showList = !pin.isLoading && (!pin.isPinEnabled || pin.isUnlocked) && !twoFA.isLoading

    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        if (!showList) {
            item { EmptyStateText(title = emptyMessage) }
        } else if (twoFA.items.isEmpty()) {
            item { EmptyStateText(title = emptyMessage) }
        } else {
            twoFA.items.forEach { item ->
                val sec = secondsLeftForPeriod(item.period, nowMs)
                val code = formatTotpCodeWithGap(formatLiveTotpCode(item, nowMs))
                item {
                    TokenListRow(
                        service = item.name,
                        code = code,
                        expiresIn = stringResource(R.string.tokenExpiresSeconds, sec),
                        onClick = { onTokenClick(item.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenListRow(
    service: String,
    code: String,
    expiresIn: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        TextMMD(service, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(4.dp))
        TextMMD(code, fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(4.dp))
        TextMMD(expiresIn, fontSize = 13.sp, color = SoftInk)
        Spacer(Modifier.height(12.dp))
        HorizontalDividerMMD(color = QuietDivider)
    }
}
