package io.quiet.auth.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.FloatingActionButtonMMD
import com.mudita.mmd.components.nav_bar.NavigationBarItemMMD
import com.mudita.mmd.components.nav_bar.NavigationBarMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import io.quiet.auth.R
import io.quiet.auth.ui.screens.AddTwoFAQrScreen
import io.quiet.auth.ui.screens.AddTwoFAScreen
import io.quiet.auth.ui.screens.DangerZoneScreen
import io.quiet.auth.ui.screens.SettingsBackupScreen
import io.quiet.auth.ui.screens.SettingsSecurityScreen
import io.quiet.auth.ui.screens.TokenDetailsScreen
import io.quiet.auth.ui.screens.TwoFAsScreen
import io.quiet.auth.ui.theme.Ink
import io.quiet.auth.ui.theme.Paper
import io.quiet.auth.ui.viewmodel.PinViewModel
import io.quiet.auth.ui.viewmodel.TwoFAViewModel

enum class MainTab {
    Tokens,
    Backup,
    Security,
}

private sealed interface TokensStack {
    data object List : TokensStack
    data class Details(val id: String) : TokensStack
    data object AddMethod : TokensStack
    data object ManualEntry : TokensStack
}

private sealed interface SecurityStack {
    data object Main : SecurityStack
    data object DangerZone : SecurityStack
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabHost(
    pinViewModel: PinViewModel,
    twoFAViewModel: TwoFAViewModel,
    pinRepository: io.quiet.auth.data.PinRepository,
    onNavigateToPin: (String) -> Unit,
    onNavigateToBackupProcessing: (String) -> Unit,
    onLocked: () -> Unit,
    onAfterReset: () -> Unit,
) {
    var selectedTab by remember { mutableStateOf(MainTab.Tokens) }
    var tokensStack by remember { mutableStateOf<TokensStack>(TokensStack.List) }
    var securityStack by remember { mutableStateOf<SecurityStack>(SecurityStack.Main) }

    val pin by pinViewModel.state.collectAsState()
    val twoFA by twoFAViewModel.state.collectAsState()

    LaunchedEffect(pin.isLoading, pin.isPinEnabled, pin.isUnlocked) {
        if (!pin.isLoading && pin.isPinEnabled && !pin.isUnlocked) onLocked()
    }

    val showBottomBar = when (selectedTab) {
        MainTab.Tokens -> tokensStack is TokensStack.List
        MainTab.Backup -> true
        MainTab.Security -> securityStack is SecurityStack.Main
    }

    val canGoBack = when (selectedTab) {
        MainTab.Tokens -> tokensStack !is TokensStack.List
        MainTab.Security -> securityStack !is SecurityStack.Main
        MainTab.Backup -> false
    }

    val title = when (selectedTab) {
        MainTab.Tokens -> when (val stack = tokensStack) {
            is TokensStack.List -> stringResource(R.string.navTokens)
            is TokensStack.Details -> twoFA.items
                .firstOrNull { it.id == stack.id }?.name ?: stringResource(R.string.appName)
            TokensStack.AddMethod -> stringResource(R.string.add2faTitle)
            TokensStack.ManualEntry -> stringResource(R.string.add2faTitle)
        }
        MainTab.Backup -> stringResource(R.string.settingsBackupTitle)
        MainTab.Security -> when (securityStack) {
            SecurityStack.Main -> stringResource(R.string.navSettings)
            SecurityStack.DangerZone -> stringResource(R.string.dangerZoneTitle)
        }
    }

    fun onBack() {
        when (selectedTab) {
            MainTab.Tokens -> tokensStack = TokensStack.List
            MainTab.Security -> securityStack = SecurityStack.Main
            MainTab.Backup -> Unit
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
                    text = title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink,
                )
            },
            navigationIcon = {
                if (canGoBack) {
                    TextMMD(
                        text = "‹",
                        modifier = Modifier
                            .clickable(onClick = ::onBack)
                            .padding(horizontal = 16.dp),
                        fontSize = 32.sp,
                        color = Ink,
                    )
                }
            },
            actions = {
                if (selectedTab == MainTab.Tokens && tokensStack is TokensStack.List) {
                    FloatingActionButtonMMD(
                        onClick = { tokensStack = TokensStack.AddMethod },
                        containerColor = Ink,
                        contentColor = Paper,
                    ) {
                        TextMMD(
                            text = "+",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Paper,
                        )
                    }
                }
            },
            showDivider = true,
        )

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                MainTab.Tokens -> when (val stack = tokensStack) {
                    TokensStack.List -> TwoFAsScreen(
                        pinViewModel = pinViewModel,
                        twoFAViewModel = twoFAViewModel,
                        onTokenClick = { tokensStack = TokensStack.Details(it) },
                    )
                    is TokensStack.Details -> TokenDetailsScreen(
                        tokenId = stack.id,
                        pinViewModel = pinViewModel,
                        twoFAViewModel = twoFAViewModel,
                        onBack = { tokensStack = TokensStack.List },
                        onLocked = onLocked,
                    )
                    TokensStack.AddMethod -> AddTwoFAQrScreen(
                        twoFAViewModel = twoFAViewModel,
                        onAddManually = { tokensStack = TokensStack.ManualEntry },
                        onCancel = { tokensStack = TokensStack.List },
                        onAdded = { tokensStack = TokensStack.List },
                    )
                    TokensStack.ManualEntry -> AddTwoFAScreen(
                        twoFAViewModel = twoFAViewModel,
                        onSaved = { tokensStack = TokensStack.List },
                        onCancel = { tokensStack = TokensStack.AddMethod },
                    )
                }
                MainTab.Backup -> SettingsBackupScreen(
                    pinViewModel = pinViewModel,
                    onCreateBackup = { onNavigateToBackupProcessing("create") },
                    onRestoreBackup = { onNavigateToBackupProcessing("restore") },
                )
                MainTab.Security -> when (securityStack) {
                    SecurityStack.Main -> SettingsSecurityScreen(
                        pinViewModel = pinViewModel,
                        onEnablePinProtection = { onNavigateToPin(PinRouteMode.SETUP) },
                        onDisablePinProtection = { onNavigateToPin(PinRouteMode.VERIFY_DISABLE) },
                        onDangerZone = { securityStack = SecurityStack.DangerZone },
                    )
                    SecurityStack.DangerZone -> DangerZoneScreen(
                        pinViewModel = pinViewModel,
                        twoFAViewModel = twoFAViewModel,
                        pinRepository = pinRepository,
                        onBack = { securityStack = SecurityStack.Main },
                        onAfterReset = onAfterReset,
                    )
                }
            }
        }

        if (showBottomBar) {
            NavigationBarMMD(containerColor = Paper) {
                BottomNavItem(
                    label = stringResource(R.string.navTokens),
                    selected = selectedTab == MainTab.Tokens,
                    onClick = {
                        selectedTab = MainTab.Tokens
                        tokensStack = TokensStack.List
                    },
                )
                BottomNavItem(
                    label = stringResource(R.string.navBackup),
                    selected = selectedTab == MainTab.Backup,
                    onClick = { selectedTab = MainTab.Backup },
                )
                BottomNavItem(
                    label = stringResource(R.string.navSettings),
                    selected = selectedTab == MainTab.Security,
                    onClick = {
                        selectedTab = MainTab.Security
                        securityStack = SecurityStack.Main
                    },
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(label: String, selected: Boolean, onClick: () -> Unit) {
    NavigationBarItemMMD(
        selected = selected,
        onClick = onClick,
        icon = { Spacer(modifier = Modifier.size(0.dp)) },
        label = {
            TextMMD(
                text = label,
                color = Ink,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                textDecoration = if (selected) TextDecoration.Underline else null,
            )
        },
        alwaysShowLabel = true,
    )
}
