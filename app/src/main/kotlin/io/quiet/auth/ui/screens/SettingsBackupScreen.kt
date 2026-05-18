package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.R
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.QuietDivider
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions

@Composable
fun SettingsBackupScreen(
    pinViewModel: PinViewModel,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    var showSessionDialog by remember { mutableStateOf(false) }

    fun ensureSessionReady(): Boolean {
        if (!pin.sessionReadyForSensitiveActions) {
            showSessionDialog = true
            return false
        }
        return true
    }

    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item { SectionHeader(stringResource(R.string.settingsBackupTitle)) }
        item {
            ActionRow(stringResource(R.string.createBackup)) {
                if (ensureSessionReady()) onCreateBackup()
            }
        }
        item {
            ActionRow(stringResource(R.string.restoreBackup)) {
                if (ensureSessionReady()) onRestoreBackup()
            }
        }
        item { SectionHeader(stringResource(R.string.settingsBackupSubtitle)) }
        item {
            TextMMD(
                stringResource(R.string.backupProcessingHint),
                fontSize = 16.sp,
                color = Ink,
            )
            HorizontalDividerMMD(color = QuietDivider)
        }
    }

    if (showSessionDialog) {
        ConfirmActionSheet(
            title = stringResource(R.string.backupErrorTitle),
            message = stringResource(R.string.sessionExpiredMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { showSessionDialog = false },
            onDismiss = { showSessionDialog = false },
        )
    }
}
