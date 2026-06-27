package com.mikokernel.ui.component.bottombar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mikokernel.Natives
import com.mikokernel.R
import com.mikokernel.ui.LocalMainPagerState
import com.mikokernel.ui.util.rootAvailable

@Composable
fun BottomBarMaterial(showSusfs: Boolean = false) {
    val isManager = Natives.isManager
    val fullFeatured = isManager && !Natives.requireNewKernel() && rootAvailable()
    val mainPagerState = LocalMainPagerState.current

    if (!fullFeatured) return

    val items = buildList {
        add(Triple(R.string.home, Icons.Filled.Home, Icons.Outlined.Home))
        if (showSusfs) {
            add(Triple(R.string.susfs_nav_title, Icons.Filled.Build, Icons.Outlined.Build))
        }
        add(Triple(R.string.superuser, Icons.Filled.Apps, Icons.Outlined.Apps))
        add(Triple(R.string.module, Icons.Filled.Apps, Icons.Outlined.Apps))
    }

    FlexibleBottomAppBar(
        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout).only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
        )
    ) {
        items.forEachIndexed { index, (label, selectedIcon, unselectedIcon) ->
            val selected = mainPagerState.selectedPage == index
            val isSuperuser = label == R.string.superuser
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        mainPagerState.animateToPage(index)
                    }
                },
                icon = {
                    if (isSuperuser) {
                        Icon(
                            painter = painterResource(R.drawable.ic_superuser),
                            contentDescription = stringResource(label),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            if (selected) selectedIcon else unselectedIcon,
                            stringResource(label)
                        )
                    }
                },
                label = {
                    Text(
                        stringResource(label),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}
