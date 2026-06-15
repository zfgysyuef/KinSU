package com.rekernel.manager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rekernel.manager.ui.component.bottombar.BottomBar
import com.rekernel.manager.ui.component.bottombar.SideRail
import com.rekernel.manager.ui.screen.HomeScreen
import com.rekernel.manager.ui.screen.ModuleScreen
import com.rekernel.manager.ui.screen.SettingsScreen
import com.rekernel.manager.ui.screen.SuperUserScreen
import com.rekernel.manager.ui.theme.AppSettings
import com.rekernel.manager.ui.theme.KernelSUTheme
import com.rekernel.manager.ui.theme.LocalColorMode
import com.rekernel.manager.ui.theme.LocalEnableBlur
import com.rekernel.manager.ui.theme.LocalEnableFloatingBottomBar
import com.rekernel.manager.ui.theme.LocalEnableFloatingBottomBarBlur
import com.rekernel.manager.ui.viewmodel.MainViewModel
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val selectedPage by viewModel.selectedPage.collectAsStateWithLifecycle()
            val appSettings = uiState
            val uiMode = UiMode.fromValue(appSettings.uiMode)
            val darkMode = appSettings.colorMode.isDark ||
                    (appSettings.colorMode.isSystem && isSystemInDarkTheme())

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { darkMode },
                )
                window.isNavigationBarContrastEnforced = false
                onDispose { }
            }

            CompositionLocalProvider(
                LocalColorMode provides appSettings.colorMode.value,
                LocalEnableBlur provides appSettings.enableBlur,
                LocalEnableFloatingBottomBar provides appSettings.enableFloatingBottomBar,
                LocalEnableFloatingBottomBarBlur provides appSettings.enableFloatingBottomBarBlur,
                LocalUiMode provides uiMode,
            ) {
                KernelSUTheme(appSettings = appSettings, uiMode = uiMode) {
                    MainScreen(
                        initialPage = selectedPage,
                        onPageChanged = viewModel::setSelectedPage,
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    initialPage: Int = 0,
    onPageChanged: (Int) -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { 4 })
    val uiMode = LocalUiMode.current
    val enableBlur = LocalEnableBlur.current
    val enableFloatingBottomBar = LocalEnableFloatingBottomBar.current
    val enableFloatingBottomBarBlur = LocalEnableFloatingBottomBarBlur.current

    val settledPage = pagerState.settledPage
    LaunchedEffect(settledPage) {
        onPageChanged(settledPage)
    }

    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val useNavigationRail = isLandscape && !(uiMode == UiMode.Miuix && enableFloatingBottomBar)

    val pagerContent = @Composable { bottomInnerPadding: Dp ->
        Box {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
            ) { page ->
                when (page) {
                    0 -> HomeScreen(bottomInnerPadding)
                    1 -> SuperUserScreen(bottomInnerPadding)
                    2 -> ModuleScreen(bottomInnerPadding)
                    3 -> SettingsScreen(bottomInnerPadding)
                }
            }
        }
    }

    if (useNavigationRail) {
        val startInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Start)
        val navBarBottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

        when (uiMode) {
            UiMode.Material -> androidx.compose.material3.Scaffold {
                Row {
                    SideRail(pagerState = pagerState)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .consumeWindowInsets(startInsets)
                    ) {
                        pagerContent(navBarBottomPadding)
                    }
                }
            }
            UiMode.Miuix -> Scaffold { _ ->
                Row {
                    SideRail(pagerState = pagerState)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .consumeWindowInsets(startInsets)
                    ) {
                        pagerContent(navBarBottomPadding)
                    }
                }
            }
        }
    } else {
        val bottomBar = @Composable {
            Box(modifier = Modifier.fillMaxWidth()) {
                BottomBar(pagerState = pagerState)
            }
        }
        when (uiMode) {
            UiMode.Material -> androidx.compose.material3.Scaffold(
                bottomBar = bottomBar
            ) { innerPadding ->
                pagerContent(innerPadding.calculateBottomPadding())
            }
            UiMode.Miuix -> Scaffold(
                bottomBar = bottomBar
            ) { _ ->
                pagerContent(Dp.Unspecified)
            }
        }
    }
}
