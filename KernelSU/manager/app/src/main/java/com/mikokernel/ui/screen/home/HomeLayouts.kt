package com.mikokernel.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikokernel.R
import com.mikokernel.ui.component.material.TonalCard
import com.mikokernel.ui.component.statustag.StatusTag

/* =========================================================
 * 多种首页布局变体（参考 FolkPatch 的 circle / stats / dashboard_ui）
 * ========================================================= */

@Composable
fun HomeCircleLayout(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior, onInstallClick = actions.onInstallClick) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircleStatusCard(state = state, actions = actions)
            AnimatedVisibility(
                visible = state.checkUpdateEnabled && state.hasUpdate,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                UpdateCard(state = state, actions = actions)
            }
            if (state.ksuVersion != null) {
                CountCardsRow(state = state, actions = actions)
            }
            InfoCard(systemInfo = state.systemInfo)
            DonateCard(onOpenUrl = actions.onOpenUrl)
            LearnMoreCard(onOpenUrl = actions.onOpenUrl)
            Spacer(Modifier.height(bottomInnerPadding))
        }
    }
}

@Composable
fun HomeStatsLayout(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior, onInstallClick = actions.onInstallClick) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsTopSection(state = state, actions = actions)
            if (state.ksuVersion != null) {
                CountCardsRow(state = state, actions = actions)
            }
            InfoCard(systemInfo = state.systemInfo)
            DonateCard(onOpenUrl = actions.onOpenUrl)
            LearnMoreCard(onOpenUrl = actions.onOpenUrl)
            Spacer(Modifier.height(bottomInnerPadding))
        }
    }
}

@Composable
fun HomeDashboardLayout(
    state: HomeUiState,
    actions: HomeActions,
    bottomInnerPadding: Dp,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = { TopBar(scrollBehavior = scrollBehavior, onInstallClick = actions.onInstallClick) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroStatusCard(state = state, actions = actions)
            if (state.ksuVersion != null) {
                CountCardsRow(state = state, actions = actions)
            }
            InfoCard(systemInfo = state.systemInfo)
            DonateCard(onOpenUrl = actions.onOpenUrl)
            LearnMoreCard(onOpenUrl = actions.onOpenUrl)
            Spacer(Modifier.height(bottomInnerPadding))
        }
    }
}

/* =========================================================
 * Circle 风格状态卡
 * ========================================================= */

@Composable
private fun CircleStatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val isWorking = state.ksuVersion != null
    val isUpdate = state.showVersionMismatchWarning || state.showRequireKernelWarning

    TonalCard(
        containerColor = when {
            isWorking -> MaterialTheme.colorScheme.primaryContainer
            isUpdate -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!isWorking) actions.onInstallClick()
                }
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                isWorking -> {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .aspectRatio(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(R.string.home_working),
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(Modifier.padding(start = 20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.home_working),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.home_working_version,
                                "${state.ksuVersion}/${state.kernelUAPIVersion}"
                            ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                state.kernelVersion.isGKI() -> {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(R.string.home_not_installed),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Column(
                        modifier = Modifier
                            .padding(start = 20.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.home_not_installed),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_click_to_install),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (state.showLkmPrompt) {
                        Button(onClick = actions.onLoadLkmClick) {
                            Text(stringResource(R.string.home_lkm_load))
                        }
                    }
                    if (state.isSELinuxPermissive) {
                        Button(
                            onClick = actions.onJailbreakClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.home_jailbreak))
                        }
                    }
                }

                else -> {
                    Icon(
                        imageVector = Icons.Outlined.Block,
                        contentDescription = stringResource(R.string.home_unsupported),
                        modifier = Modifier.size(32.dp)
                    )
                    Column(Modifier.padding(start = 20.dp)) {
                        Text(
                            text = stringResource(R.string.home_unsupported),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.home_unsupported_reason),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================
 * Stats 风格顶部区
 * ========================================================= */

@Composable
private fun StatsTopSection(
    state: HomeUiState,
    actions: HomeActions,
) {
    val isWorking = state.ksuVersion != null
    val isUpdate = state.showVersionMismatchWarning || state.showRequireKernelWarning

    Card(
        onClick = { if (!isWorking) actions.onInstallClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWorking -> MaterialTheme.colorScheme.primary
                isUpdate -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.errorContainer
            },
            contentColor = when {
                isWorking -> MaterialTheme.colorScheme.onPrimary
                isUpdate -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onErrorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                isWorking -> Icons.Filled.CheckCircle
                isUpdate -> Icons.Outlined.SystemUpdate
                state.kernelVersion.isGKI() -> Icons.Filled.Warning
                else -> Icons.Outlined.Block
            }
            val title = when {
                isWorking -> stringResource(R.string.home_working)
                isUpdate -> stringResource(R.string.home_kp_need_update)
                state.kernelVersion.isGKI() -> stringResource(R.string.home_not_installed)
                else -> stringResource(R.string.home_unsupported)
            }
            val summary = when {
                isWorking -> stringResource(
                    R.string.home_working_version,
                    "${state.ksuVersion}/${state.kernelUAPIVersion}"
                )
                state.kernelVersion.isGKI() -> stringResource(R.string.home_click_to_install)
                else -> stringResource(R.string.home_unsupported_reason)
            }

            Icon(icon, title, modifier = Modifier.size(40.dp))
            Column(Modifier.padding(start = 16.dp).weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/* =========================================================
 * Dashboard Pro 风格 Hero 状态卡
 * ========================================================= */

@Composable
private fun HeroStatusCard(
    state: HomeUiState,
    actions: HomeActions,
) {
    val isWorking = state.ksuVersion != null
    val isUpdate = state.showVersionMismatchWarning || state.showRequireKernelWarning

    Card(
        onClick = { if (!isWorking) actions.onInstallClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isWorking -> MaterialTheme.colorScheme.primary
                isUpdate -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.errorContainer
            },
            contentColor = when {
                isWorking -> MaterialTheme.colorScheme.onPrimary
                isUpdate -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onErrorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isWorking -> Icons.Outlined.CheckCircle
                        isUpdate -> Icons.Outlined.SystemUpdate
                        state.kernelVersion.isGKI() -> Icons.Filled.Warning
                        else -> Icons.Outlined.Block
                    },
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = when {
                    isWorking -> stringResource(R.string.home_working)
                    isUpdate -> stringResource(R.string.home_kp_need_update)
                    state.kernelVersion.isGKI() -> stringResource(R.string.home_not_installed)
                    else -> stringResource(R.string.home_unsupported)
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = when {
                    isWorking -> stringResource(
                        R.string.home_working_version,
                        "${state.ksuVersion}/${state.kernelUAPIVersion}"
                    )
                    state.kernelVersion.isGKI() -> stringResource(R.string.home_click_to_install)
                    else -> stringResource(R.string.home_unsupported_reason)
                },
                style = MaterialTheme.typography.bodyLarge
            )

            if (isWorking) {
                Spacer(Modifier.height(12.dp))
                val workingMode = when (state.lkmMode) {
                    null -> ""
                    true -> "LKM"
                    else -> "GKI"
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (workingMode.isNotEmpty()) {
                        StatusTag(
                            label = workingMode,
                            contentColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    if (state.isSafeMode) {
                        StatusTag(
                            label = stringResource(id = R.string.safe_mode),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                    if (state.isLateLoadMode) {
                        StatusTag(
                            label = stringResource(id = R.string.jailbreak_mode),
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                }
            }
        }
    }
}

/* =========================================================
 * 计数卡片行（超级用户 / 模块）
 * ========================================================= */

@Composable
private fun CountCardsRow(
    state: HomeUiState,
    actions: HomeActions,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Security,
            label = stringResource(R.string.superuser),
            count = state.superuserCount,
            onClick = actions.onSuperuserClick
        )
        CountCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Outlined.Widgets,
            label = stringResource(R.string.module),
            count = state.moduleCount,
            onClick = actions.onModuleClick
        )
    }
}

@Composable
private fun CountCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    onClick: () -> Unit,
) {
    TonalCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
