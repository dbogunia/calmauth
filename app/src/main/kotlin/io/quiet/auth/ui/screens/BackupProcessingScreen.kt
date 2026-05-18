package io.quiet.auth.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.progress_indicator.LinearProgressIndicatorMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import io.quiet.auth.R
import io.quiet.auth.data.BackupIO
import io.quiet.auth.domain.BackupFormatException
import io.quiet.auth.domain.parseBackupCsv
import io.quiet.auth.domain.twoFAItemsToCsv
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.Paper
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BackupProcessingScreen(
    action: String,
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    backupIO: BackupIO,
    onFinished: () -> Unit,
    onLocked: () -> Unit,
) {
    val isRestore = action == "restore"
    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var status by remember {
        mutableStateOf(
            context.getString(
                if (isRestore) R.string.restoringBackupProgress else R.string.creatingBackupProgress,
            ),
        )
    }
    var progress by remember { mutableFloatStateOf(0.15f) }

    LaunchedEffect(Unit) {
        while (true) {
            progress = (progress + 0.08f).coerceAtMost(0.92f)
            delay(300)
        }
    }

    val createLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BackupIO.MIME_CSV),
    ) { uri ->
        scope.launch {
            try {
                if (uri == null) {
                    status = context.getString(R.string.backupErrorMessage)
                } else {
                    backupIO.writeCsv(uri, twoFAItemsToCsv(twoFA.items))
                    status = context.getString(R.string.backupCreatedTitle)
                }
            } catch (_: Throwable) {
                status = context.getString(R.string.backupErrorMessage)
            } finally {
                delay(1100)
                onFinished()
            }
        }
    }

    val openLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        scope.launch {
            try {
                if (uri == null) {
                    status = context.getString(R.string.restoreErrorMessage)
                } else {
                    val csv = backupIO.readCsv(uri)
                    val items = parseBackupCsv(csv)
                    twoFAViewModel.replaceAll(items)
                    status = context.getString(R.string.restoreSuccessTitle)
                }
            } catch (_: BackupFormatException) {
                status = context.getString(R.string.restoreErrorMessage)
            } catch (_: Throwable) {
                status = context.getString(R.string.restoreErrorMessage)
            } finally {
                delay(1100)
                onFinished()
            }
        }
    }

    LaunchedEffect(action, pin.sessionReadyForSensitiveActions) {
        if (!pin.sessionReadyForSensitiveActions) {
            status = context.getString(R.string.sessionExpiredMessage)
            delay(900)
            onLocked()
            return@LaunchedEffect
        }
        backupIO.beginInteractiveBackup()
        if (isRestore) {
            openLauncher.launch(arrayOf(BackupIO.MIME_CSV, "text/comma-separated-values", "text/plain"))
        } else {
            createLauncher.launch(BackupIO.suggestedBackupFileName())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding(),
    ) {
        TopAppBarMMD(
            title = {
                TextMMD(
                    text = stringResource(if (isRestore) R.string.restoreBackup else R.string.createBackup),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
            },
            showDivider = true,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            LinearProgressIndicatorMMD(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            TextMMD(status, fontSize = 16.sp, color = Ink)
        }
    }
}
