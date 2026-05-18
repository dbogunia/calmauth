package io.quiet.auth.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.mudita.mmd.components.lazy.LazyColumnMMD
import io.quiet.auth.R
import io.quiet.auth.domain.parseOtpAuthUri
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun AddTwoFAQrScreen(
    twoFAViewModel: TwoFAViewModel,
    onAddManually: () -> Unit,
    onCancel: () -> Unit,
    onAdded: () -> Unit,
) {
    val context = LocalContext.current
    var dialogMessage by remember { mutableStateOf<Int?>(null) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val raw = result.contents ?: return@rememberLauncherForActivityResult
        val parsed = parseOtpAuthUri(raw)
        if (parsed == null) {
            dialogMessage = R.string.invalidQrMessage
            return@rememberLauncherForActivityResult
        }
        twoFAViewModel.addParsedOtpAuth(parsed)
        dialogMessage = R.string.qrAddedMessage
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        if (granted) launchScan(scanLauncher, context)
    }

    LaunchedEffect(Unit) {
        if (permissionGranted) launchScan(scanLauncher, context)
    }

    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item { SectionHeader(stringResource(R.string.qrTitle)) }
        item {
            ActionRow(stringResource(R.string.qrTitle)) {
                if (permissionGranted) {
                    launchScan(scanLauncher, context)
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
        item { ActionRow(stringResource(R.string.addManually), onAddManually) }
        item { ActionRow(stringResource(R.string.cancel), onCancel) }
    }

    if (dialogMessage != null) {
        ConfirmActionSheet(
            title = stringResource(
                if (dialogMessage == R.string.qrAddedMessage) R.string.qrAddedTitle else R.string.invalidQrTitle,
            ),
            message = stringResource(dialogMessage!!),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                val msg = dialogMessage
                dialogMessage = null
                if (msg == R.string.qrAddedMessage) onAdded()
            },
            onDismiss = {
                val msg = dialogMessage
                dialogMessage = null
                if (msg == R.string.qrAddedMessage) onAdded()
            },
        )
    }
}

private fun launchScan(
    launcher: androidx.activity.result.ActivityResultLauncher<ScanOptions>,
    context: android.content.Context,
) {
    val options = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt(context.getString(R.string.qrSubtitle))
        setBeepEnabled(false)
        setOrientationLocked(false)
    }
    launcher.launch(options)
}
