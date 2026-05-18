package io.quiet.auth.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import io.quiet.auth.R
import io.quiet.auth.ui.BottomAction
import io.quiet.auth.ui.nav.PinRouteMode
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.Paper
import io.quiet.auth.ui.viewmodel.PinViewModel
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

@Composable
fun PinScreen(
    viewModel: PinViewModel,
    mode: String,
    onFinished: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf<String?>(null) }
    var biometricAttempted by remember { mutableStateOf(false) }
    var biometricBusy by remember { mutableStateOf(false) }

    val setupLike = mode == PinRouteMode.SETUP
    val verifyDisable = mode == PinRouteMode.VERIFY_DISABLE
    val unlockLike = mode == PinRouteMode.UNLOCK || verifyDisable

    LaunchedEffect(state.hasPin, state.isPinEnabled, state.isUnlocked, state.isLoading, mode) {
        val shouldAutoNavigate =
            !state.isLoading &&
                state.isPinEnabled &&
                state.hasPin &&
                state.isUnlocked &&
                !verifyDisable
        if (shouldAutoNavigate) onFinished()
    }

    LaunchedEffect(
        mode,
        state.hasPin,
        state.hasBiometricUnlock,
        state.isPinEnabled,
        state.isUnlocked,
        state.isLoading,
    ) {
        if (
            mode != PinRouteMode.UNLOCK ||
            state.isLoading ||
            !state.isPinEnabled ||
            !state.hasPin ||
            !state.hasBiometricUnlock ||
            state.isUnlocked ||
            biometricAttempted
        ) {
            return@LaunchedEffect
        }
        biometricAttempted = true
        biometricBusy = true
        val ok = viewModel.unlockWithBiometrics(
            title = context.getString(R.string.pinUnlockTitle),
            cancelLabel = context.getString(R.string.cancel),
        )
        biometricBusy = false
        if (!ok) {
            Toast.makeText(
                context,
                context.getString(R.string.biometricUnlockFailedMessage),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun toast(titleRes: Int, msgRes: Int) {
        Toast.makeText(
            context,
            "${context.getString(titleRes)}: ${context.getString(msgRes)}",
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun onContinue() {
        val trimmed = pin.trim()
        if (trimmed.length < PIN_LENGTH) {
            toast(R.string.pinInvalidTitle, R.string.pinInvalidMessage)
            return
        }
        when {
            verifyDisable -> {
                if (!viewModel.disablePinProtectionAfterVerification(trimmed)) {
                    toast(R.string.pinIncorrectTitle, R.string.pinIncorrectMessage)
                    pin = ""
                } else {
                    Toast.makeText(
                        context,
                        context.getString(R.string.pinProtectionDisabledMessage),
                        Toast.LENGTH_SHORT,
                    ).show()
                    onFinished()
                }
            }
            setupLike -> {
                if (firstPin == null) {
                    if (!viewModel.validatePinFormat(trimmed)) {
                        toast(R.string.pinInvalidTitle, R.string.pinInvalidMessage)
                        return
                    }
                    firstPin = trimmed
                    pin = ""
                    return
                }
                if (trimmed != firstPin) {
                    toast(R.string.pinMismatchTitle, R.string.pinMismatchMessage)
                    firstPin = null
                    pin = ""
                    return
                }
                if (!viewModel.setPin(trimmed)) {
                    toast(R.string.pinStorageErrorTitle, R.string.pinStorageErrorMessage)
                    return
                }
            }
            unlockLike && state.hasPin -> {
                if (!viewModel.unlockWithPin(trimmed)) {
                    toast(R.string.pinIncorrectTitle, R.string.pinIncorrectMessage)
                    pin = ""
                }
            }
            else -> Unit
        }
    }

    val pinDots = buildString {
        repeat(PIN_LENGTH) { i ->
            append(if (i < pin.length) "●" else "○")
            if (i < PIN_LENGTH - 1) append(' ')
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
                    text = stringResource(R.string.pinUnlockTitle),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
            },
            showDivider = true,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextMMD(pinDots, fontSize = 40.sp, fontWeight = FontWeight.Medium, color = Ink)
            Spacer(Modifier.height(32.dp))
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "⌫"),
            ).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    row.forEach { digit ->
                        if (digit.isBlank()) {
                            Spacer(Modifier.width(72.dp))
                        } else {
                            ButtonMMD(
                                onClick = {
                                    when (digit) {
                                        "⌫" -> pin = pin.dropLast(1)
                                        else -> if (pin.length < PIN_LENGTH) pin += digit
                                    }
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(64.dp),
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                TextMMD(digit, fontSize = 20.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            if (
                mode == PinRouteMode.UNLOCK &&
                state.isPinEnabled &&
                state.hasPin &&
                state.isBiometricSupported
            ) {
                Spacer(Modifier.height(16.dp))
                ButtonMMD(
                    onClick = {
                        if (!biometricBusy) {
                            biometricAttempted = true
                            biometricBusy = true
                            scope.launch {
                                val ok = viewModel.unlockWithBiometrics(
                                    title = context.getString(R.string.pinUnlockTitle),
                                    cancelLabel = context.getString(R.string.cancel),
                                )
                                biometricBusy = false
                                if (!ok) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.biometricUnlockFailedMessage),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = !biometricBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextMMD(
                        text = if (biometricBusy) {
                            stringResource(R.string.unlockingApp)
                        } else {
                            stringResource(R.string.useBiometrics)
                        },
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        BottomAction(
            text = stringResource(R.string.continueLabel),
            onClick = ::onContinue,
            enabled = pin.length >= PIN_LENGTH,
        )
    }
}
