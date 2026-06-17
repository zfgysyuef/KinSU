package com.mikokernel.ui.screen.home

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mikokernel.R
import com.mikokernel.magica.MagicaService
import com.mikokernel.ui.LocalMainPagerState
import com.mikokernel.ui.component.dialog.rememberLoadingDialog
import com.mikokernel.ui.navigation3.Navigator
import com.mikokernel.ui.navigation3.Route
import com.mikokernel.ui.util.loadBundledLKM
import com.mikokernel.ui.viewmodel.HomeViewModel

@Composable
fun HomePager(
    navigator: Navigator,
    bottomInnerPadding: Dp,
    isCurrentPage: Boolean = true
) {
    val viewModel = viewModel<HomeViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mainState = LocalMainPagerState.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val loadingDialog = rememberLoadingDialog()
    val scope = rememberCoroutineScope()

    var hasActivated by remember { mutableStateOf(false) }
    if (isCurrentPage) hasActivated = true

    if (hasActivated) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
        }
    }

    val actions = HomeActions(
        onInstallClick = { navigator.push(Route.Install) },
        onSuperuserClick = { mainState.animateToPage(1) },
        onModuleClick = { mainState.animateToPage(2) },
        onOpenUrl = uriHandler::openUri,
        onJailbreakClick = {
            loadingDialog.showLoading()
            context.startService(Intent(context, MagicaService::class.java))
            // Manager will be force-stopped and restarted by late-load on success.
            // If that doesn't happen within timeout, jailbreak likely failed.
            scope.launch(Dispatchers.IO) {
                delay(30_000)
                withContext(Dispatchers.Main) {
                    loadingDialog.hide()
                    Toast.makeText(context, R.string.jailbreak_timeout, Toast.LENGTH_LONG).show()
                }
            }
        },
        onLoadLkmClick = {
            loadingDialog.showLoading()
            scope.launch(Dispatchers.IO) {
                val result = loadBundledLKM().trim()
                withContext(Dispatchers.Main) {
                    loadingDialog.hide()
                    val msg = if (result.isNotBlank()) result else "LKM loaded, refreshing..."
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    delay(2000)
                    viewModel.refresh()
                }
            }
        },
    )

    when (uiState.homeLayoutStyle) {
        "circle" -> HomeCircleLayout(
            state = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
        )
        "stats" -> HomeStatsLayout(
            state = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
        )
        "dashboard_ui" -> HomeDashboardLayout(
            state = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
        )
        else -> HomePagerMaterial(
            state = uiState,
            actions = actions,
            bottomInnerPadding = bottomInnerPadding,
        )
    }
}
