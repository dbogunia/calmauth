package io.quiet.auth.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text_field.TextFieldMMD
import io.quiet.auth.R
import io.quiet.auth.domain.AddTwoFAInput
import io.quiet.auth.domain.normalizeSecretInput
import io.quiet.auth.domain.parseOtpAuthUri
import io.quiet.auth.ui.BottomAction
import io.quiet.auth.ui.FieldLabel
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.components.ConfirmActionSheet
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

@Composable
fun AddTwoFAScreen(
    twoFAViewModel: TwoFAViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
) {
    var serviceName by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    var showMissing by remember { mutableStateOf(false) }
    var showInvalidSecret by remember { mutableStateOf(false) }

    fun save() {
        val trimmedSecret = secret.trim()
        if (trimmedSecret.startsWith("otpauth://", ignoreCase = true)) {
            val parsed = parseOtpAuthUri(trimmedSecret)
            if (parsed == null || normalizeSecretInput(trimmedSecret).isEmpty()) {
                showInvalidSecret = true
                return
            }
            val name = serviceName.trim().ifEmpty { parsed.name }
            val accountName = account.trim().ifEmpty { parsed.account }
            if (name.isBlank() || accountName.isBlank()) {
                showMissing = true
                return
            }
            twoFAViewModel.addTwoFA(
                AddTwoFAInput(
                    name = name,
                    account = accountName,
                    secret = parsed.secret,
                    digits = parsed.digits,
                    period = parsed.period,
                    algorithm = parsed.algorithm,
                ),
            )
            onSaved()
            return
        }

        if (serviceName.isBlank() || account.isBlank() || secret.isBlank()) {
            showMissing = true
            return
        }
        if (normalizeSecretInput(secret).isEmpty()) {
            showInvalidSecret = true
            return
        }
        twoFAViewModel.addTwoFA(
            AddTwoFAInput(
                name = serviceName,
                account = account,
                secret = secret,
            ),
        )
        onSaved()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumnMMD(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            item {
                FieldLabel(stringResource(R.string.serviceName))
                TextFieldMMD(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    singleLine = true,
                )
                Spacer(Modifier.height(24.dp))
                FieldLabel(stringResource(R.string.account))
                TextFieldMMD(
                    value = account,
                    onValueChange = { account = it },
                    singleLine = true,
                )
                Spacer(Modifier.height(24.dp))
                FieldLabel(stringResource(R.string.secretKey))
                TextFieldMMD(
                    value = secret,
                    onValueChange = { secret = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrect = false,
                    ),
                    supportingText = { Text(stringResource(R.string.secretKeyHint)) },
                )
                Spacer(Modifier.height(24.dp))
                SectionHeader(stringResource(R.string.token_details_field_type))
            }
        }
        BottomAction(text = stringResource(R.string.save2fa), onClick = ::save)
    }

    if (showMissing) {
        ConfirmActionSheet(
            title = stringResource(R.string.missingFieldsTitle),
            message = stringResource(R.string.missingFieldsMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { showMissing = false },
            onDismiss = { showMissing = false },
        )
    }

    if (showInvalidSecret) {
        ConfirmActionSheet(
            title = stringResource(R.string.invalidSecretTitle),
            message = stringResource(R.string.invalidSecretMessage),
            confirmLabel = "OK",
            dismissLabel = stringResource(R.string.cancel),
            onConfirm = { showInvalidSecret = false },
            onDismiss = { showInvalidSecret = false },
        )
    }
}
