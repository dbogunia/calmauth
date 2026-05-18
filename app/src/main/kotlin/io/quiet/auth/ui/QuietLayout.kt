package io.quiet.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.Paper
import io.quiet.auth.ui.theme.QuietDivider
import io.quiet.auth.ui.theme.SoftInk

@Composable
fun SectionHeader(text: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextMMD(text, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(8.dp))
        HorizontalDividerMMD(color = Ink)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun FieldLabel(text: String) {
    TextMMD(text, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Ink)
    Spacer(Modifier.height(8.dp))
}

@Composable
fun MetadataRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        TextMMD(label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(4.dp))
        TextMMD(value, fontSize = 14.sp, color = SoftInk)
        Spacer(Modifier.height(12.dp))
        HorizontalDividerMMD(color = QuietDivider)
    }
}

@Composable
fun ActionRow(label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
    ) {
        TextMMD(label, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
        Spacer(Modifier.height(12.dp))
        HorizontalDividerMMD(color = QuietDivider)
    }
}

@Composable
fun BottomAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paper)
            .padding(16.dp),
    ) {
        HorizontalDividerMMD(color = QuietDivider)
        Spacer(Modifier.height(12.dp))
        ButtonMMD(onClick = onClick, modifier = Modifier.fillMaxWidth(), enabled = enabled) {
            TextMMD(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun EmptyStateText(title: String, description: String = "") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
    ) {
        TextMMD(title, fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Ink)
        if (description.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            TextMMD(description, fontSize = 14.sp, color = SoftInk)
        }
    }
}
