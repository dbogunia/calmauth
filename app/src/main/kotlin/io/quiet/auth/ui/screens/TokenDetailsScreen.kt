package io.quiet.auth.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.R
import io.quiet.auth.domain.formatLiveTotpCode
import io.quiet.auth.domain.secondsLeftForPeriod
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.EmptyStateText
import io.quiet.auth.ui.MetadataRow
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.SoftInk
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel
import kotlinx.coroutines.delay

private fun formatTotpCodeWithGap(code: String): String {
    val digits = code.filter { it.isDigit() }
    return if (digits.length == 6) {
        "${digits.take(3)} ${digits.drop(3)}"
    } else {
        code
    }
}

@Composable
fun TokenDetailsScreen(
    tokenId: String,
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    onBack: () -> Unit,
    onLocked: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val nowMsState = remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000L)
            nowMsState.longValue = System.currentTimeMillis()
        }
    }
    val nowMs = nowMsState.longValue

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pin.isLoading, pin.isPinEnabled, pin.isUnlocked) {
        if (!pin.isLoading && pin.isPinEnabled && !pin.isUnlocked) onLocked()
    }

    val item by remember(tokenId, twoFA.items) {
        derivedStateOf { twoFA.items.firstOrNull { it.id == tokenId } }
    }

    val context = LocalContext.current
    val copyDone = stringResource(R.string.token_details_copy_toast)
    val notAvailable = stringResource(R.string.token_details_not_available)

    when {
        pin.isLoading || (pin.isPinEnabled && !pin.isUnlocked) -> {
            EmptyStateText(title = stringResource(R.string.unlockingApp))
        }
        item == null -> {
            EmptyStateText(title = stringResource(R.string.tokenNotFound))
        }
        else -> {
            val token = item!!
            val code = formatTotpCodeWithGap(formatLiveTotpCode(token, nowMs))
            val secondsLeft = secondsLeftForPeriod(token.period, nowMs)

            LazyColumnMMD(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            ) {
                item { SectionHeader(stringResource(R.string.token_details_current_code)) }
                item {
                    TextMMD(code, fontSize = 36.sp, fontWeight = FontWeight.Medium, color = Ink)
                    Spacer(Modifier.height(8.dp))
                    TextMMD(
                        stringResource(R.string.tokenExpiresSeconds, secondsLeft),
                        fontSize = 16.sp,
                        color = SoftInk,
                    )
                    Spacer(Modifier.height(24.dp))
                }
                item { SectionHeader(stringResource(R.string.settingsSecurityTitle)) }
                item { MetadataRow(stringResource(R.string.token_details_field_type), stringResource(R.string.token_details_type_totp)) }
                item { MetadataRow(stringResource(R.string.token_details_field_account), token.account) }
                item { MetadataRow(stringResource(R.string.token_details_field_added), notAvailable) }
                item { MetadataRow(stringResource(R.string.token_details_field_in_backup), notAvailable) }
                item { SectionHeader(stringResource(R.string.token_details_copy_code)) }
                item {
                    ActionRow(stringResource(R.string.token_details_copy_code)) {
                        val plain = formatLiveTotpCode(token, nowMs).filter { it.isDigit() }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("totp", plain))
                        Toast.makeText(context, copyDone, Toast.LENGTH_SHORT).show()
                    }
                }
                item {
                    ActionRow(stringResource(R.string.confirmDelete)) {
                        showDeleteDialog = true
                    }
                }
            }
        }
    }

    if (showDeleteDialog && item != null) {
        ConfirmActionSheet(
            title = stringResource(R.string.deleteTokenTitle),
            message = stringResource(R.string.deleteTokenMessage),
            confirmLabel = stringResource(R.string.confirmDelete),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                twoFAViewModel.removeTwoFA(item!!.id)
                showDeleteDialog = false
                onBack()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
}
