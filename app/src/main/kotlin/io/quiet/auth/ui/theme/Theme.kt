package io.quiet.auth.ui.theme

import androidx.compose.runtime.Composable
import com.mudita.mmd.ThemeMMD

@Composable
fun QuietAuthTheme(content: @Composable () -> Unit) {
    ThemeMMD(content = content)
}
