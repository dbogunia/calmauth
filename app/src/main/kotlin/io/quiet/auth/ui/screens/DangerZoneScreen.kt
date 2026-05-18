package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.lazy.LazyColumnMMD
import io.quiet.auth.R
import io.quiet.auth.data.PinRepository
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

private sealed class PendingDangerAction {
    data object ResetStorage : PendingDangerAction()
    data object RemovePin : PendingDangerAction()
    data object RemoveBiometrics : PendingDangerAction()
}

@Composable
fun DangerZoneScreen(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    pinRepository: PinRepository,
    onBack: () -> Unit,
    onAfterReset: () -> Unit,
) {
    var pending by remember { mutableStateOf<PendingDangerAction?>(null) }
    var successNavigate by remember { mutableStateOf(false) }

    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item { SectionHeader(stringResource(R.string.dangerZoneTitle)) }
        item {
            ActionRow(stringResource(R.string.developerResetStorage)) {
                pending = PendingDangerAction.ResetStorage
            }
        }
        item {
            ActionRow(stringResource(R.string.developerRemovePin)) {
                pending = PendingDangerAction.RemovePin
            }
        }
        item {
            ActionRow(stringResource(R.string.developerRemoveBiometrics)) {
                pending = PendingDangerAction.RemoveBiometrics
            }
        }
        item { ActionRow(stringResource(R.string.settingsBack), onBack) }
    }

    when (val action = pending) {
        PendingDangerAction.ResetStorage -> ConfirmActionSheet(
            title = stringResource(R.string.developerResetStorageTitle),
            message = stringResource(R.string.developerResetStorageConfirmMessage),
            confirmLabel = stringResource(R.string.developerConfirmAction),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                pinRepository.clearOnboardingCompleted()
                pinViewModel.removePin()
                twoFAViewModel.replaceAll(emptyList())
                pending = null
                successNavigate = true
            },
            onDismiss = { pending = null },
        )
        PendingDangerAction.RemovePin -> ConfirmActionSheet(
            title = stringResource(R.string.developerRemovePinTitle),
            message = stringResource(R.string.developerRemovePinConfirmMessage),
            confirmLabel = stringResource(R.string.developerConfirmAction),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                pinRepository.clearOnboardingCompleted()
                pinViewModel.removePin()
                pending = null
                successNavigate = true
            },
            onDismiss = { pending = null },
        )
        PendingDangerAction.RemoveBiometrics -> ConfirmActionSheet(
            title = stringResource(R.string.developerRemoveBiometricsTitle),
            message = stringResource(R.string.developerRemoveBiometricsConfirmMessage),
            confirmLabel = stringResource(R.string.developerConfirmAction),
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                pinViewModel.removeBiometrics()
                pending = null
            },
            onDismiss = { pending = null },
        )
        null -> Unit
    }

    if (successNavigate) {
        ConfirmActionSheet(
            title = stringResource(R.string.developerActionSuccessTitle),
            message = stringResource(R.string.developerResetStorageSuccessMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = {
                successNavigate = false
                onAfterReset()
            },
            onDismiss = {
                successNavigate = false
                onAfterReset()
            },
        )
    }
}
