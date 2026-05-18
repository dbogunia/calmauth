package io.quiet.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.bottom_sheet.ModalBottomSheetMMD
import com.mudita.mmd.components.bottom_sheet.rememberModalBottomSheetMMDState
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.ui.theme.Ink

@Composable
fun ConfirmActionSheet(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetMMDState(skipPartiallyExpanded = true)
    ModalBottomSheetMMD(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TextMMD(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
            TextMMD(message, fontSize = 14.sp, color = Ink)
            ButtonMMD(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                TextMMD(confirmLabel, color = Color.White, fontSize = 16.sp)
            }
            OutlinedButtonMMD(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                TextMMD(dismissLabel, fontSize = 16.sp, color = Ink)
            }
        }
    }
}
