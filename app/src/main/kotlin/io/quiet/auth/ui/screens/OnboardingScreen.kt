package io.quiet.auth.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import io.quiet.auth.R
import io.quiet.auth.ui.ActionRow
import io.quiet.auth.ui.BottomAction
import io.quiet.auth.ui.SectionHeader
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.Paper
import io.quiet.auth.ui.theme.SoftInk

@Composable
fun OnboardingScreen(
    onContinueWithoutPin: () -> Unit,
    onProtectWithPin: () -> Unit,
    onDeveloperMode: () -> Unit,
) {
    val tapCounter = remember { intArrayOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .systemBarsPadding(),
    ) {
        TopAppBarMMD(
            title = {
                TextMMD(
                    text = stringResource(R.string.appName),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
            },
            showDivider = true,
        )

        LazyColumnMMD(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        ) {
            item {
                SectionHeader(stringResource(R.string.onboardingTitle))
                TextMMD(
                    stringResource(R.string.onboardingSubtitle),
                    fontSize = 16.sp,
                    color = SoftInk,
                )
                Spacer(Modifier.height(24.dp))
                TextMMD(
                    text = "\uD83C\uDF3F",
                    fontSize = 48.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            tapCounter[0] += 1
                            if (tapCounter[0] >= 5) {
                                tapCounter[0] = 0
                                onDeveloperMode()
                            }
                        }
                        .padding(vertical = 24.dp),
                )
                TextMMD(
                    stringResource(R.string.onboardingPinHint),
                    fontSize = 14.sp,
                    color = SoftInk,
                )
            }
        }

        BottomAction(
            text = stringResource(R.string.continueWithoutPin),
            onClick = onContinueWithoutPin,
        )
        ActionRow(
            label = stringResource(R.string.protectWithPin),
            onClick = onProtectWithPin,
        )
    }
}
