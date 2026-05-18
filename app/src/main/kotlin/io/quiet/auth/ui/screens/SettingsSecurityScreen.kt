package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.switcher.SwitchMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.R
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.MetadataRow
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.QuietDivider
import io.quiet.auth.ui.theme.SoftInk
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.sessionReadyForSensitiveActions
import kotlinx.coroutines.launch

@Composable
fun SettingsSecurityScreen(
    pinViewModel: PinViewModel,
    onEnablePinProtection: () -> Unit,
    onDisablePinProtection: () -> Unit,
    onDangerZone: () -> Unit,
) {
    val pin by pinViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var dialogMessage by remember { mutableStateOf<Int?>(null) }
    val biometricsTitle = stringResource(R.string.biometricsSettingTitle)
    val cancelLabel = stringResource(R.string.cancel)

    fun ensureSessionReady(): Boolean {
        if (!pin.sessionReadyForSensitiveActions) {
            dialogMessage = R.string.sessionExpiredMessage
            return false
        }
        return true
    }

    LazyColumnMMD(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item { SectionHeader(stringResource(R.string.settingsSecurityTitle)) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    TextMMD(
                        stringResource(R.string.pinProtectionSettingTitle),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Ink,
                    )
                    TextMMD(
                        text = if (pin.isPinEnabled) {
                            stringResource(R.string.pinProtectionSettingDescription)
                        } else {
                            stringResource(R.string.pinProtectionSettingDescription)
                        },
                        fontSize = 13.sp,
                        color = SoftInk,
                    )
                }
                SwitchMMD(
                    checked = pin.isPinEnabled,
                    onCheckedChange = { next ->
                        if (next) onEnablePinProtection() else if (pin.isPinEnabled) onDisablePinProtection()
                    },
                )
            }
            HorizontalDividerMMD(color = QuietDivider)
        }
        if (pin.isBiometricSupported) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TextMMD(
                            stringResource(R.string.biometricsSettingTitle),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Ink,
                        )
                        TextMMD(
                            text = if (pin.isBiometricSupported) {
                                stringResource(R.string.biometricsSettingDescription)
                            } else {
                                stringResource(R.string.biometricsUnavailableMessage)
                            },
                            fontSize = 13.sp,
                            color = SoftInk,
                        )
                    }
                    SwitchMMD(
                        checked = pin.hasBiometricUnlock,
                        enabled = pin.sessionReadyForSensitiveActions,
                        onCheckedChange = { next ->
                            if (ensureSessionReady()) {
                                scope.launch {
                                    val ok = pinViewModel.setBiometricUnlockEnabled(
                                        next,
                                        title = biometricsTitle,
                                        cancelLabel = cancelLabel,
                                    )
                                    if (!ok) dialogMessage = R.string.biometricSettingsErrorMessage
                                }
                            }
                        },
                    )
                }
                HorizontalDividerMMD(color = QuietDivider)
            }
        }
        item { MetadataRow(stringResource(R.string.settingsSecurityTitle), "30s") }
        item { SectionHeader(stringResource(R.string.dangerZoneTitle)) }
        item { ActionRow(stringResource(R.string.dangerZoneTitle), onDangerZone) }
    }

    if (dialogMessage != null) {
        ConfirmActionSheet(
            title = stringResource(R.string.biometricSettingsErrorTitle),
            message = stringResource(dialogMessage!!),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { dialogMessage = null },
            onDismiss = { dialogMessage = null },
        )
    }
}
