package com.mikokernel.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.FixedScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikokernel.R
import com.mikokernel.ui.component.material.SegmentedColumn
import com.mikokernel.ui.component.material.SegmentedListItem
import com.mikokernel.ui.component.material.TonalCard
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

@Composable
fun AboutScreenMaterial(
    state: AboutUiState,
    actions: AboutScreenActions,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(
                        onClick = actions.onBack
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(vertical = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            contentScale = FixedScale(1f)
                        )
                    }
                    Text(
                        modifier = Modifier.padding(top = 12.dp),
                        text = state.appName,
                        fontWeight = FontWeight.Medium,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize
                    )
                    Text(
                        text = state.versionName,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
            }
            item {
                SegmentedColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    content = state.links.map { linkInfo ->
                        {
                            SegmentedListItem(
                                onClick = { actions.onOpenLink(linkInfo.url) },
                                headlineContent = { Text(linkInfo.fullText) }
                            )
                        }
                    }
                )
            }
            item {
                TonalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = parseHtml(state.copyrightNotice, actions.onOpenLink),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(
                    Modifier.height(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                    )
                )
            }
        }
    }
}

/**
 * Minimal HTML parser for the copyright notice card.
 * Supports: <b>...</b>, <a href="...">...</a>, <br/>
 *
 * Original author of the underlying KernelSU project: weishu.
 * This KinSU derivative is released under GPLv3, preserving the upstream
 * commit history in accordance with the license.
 */
private fun parseHtml(
    html: String,
    onOpenLink: (String) -> Unit
): AnnotatedString = buildAnnotatedString {
    val tagRegex = Regex("""<(?:b>|a\s+href="([^"]*)">|/a>|/b>|br\s*/?)>""")
    var idx = 0
    var bold = false
    var linkUrl: String? = null
    for (match in tagRegex.findAll(html)) {
        if (match.range.first > idx) {
            val segment = html.substring(idx, match.range.first)
            if (linkUrl != null) {
                val url = linkUrl
                withLink(
                    LinkAnnotation.Clickable(
                        tag = url,
                        linkInteractionListener = LinkInteractionListener { onOpenLink(url) }
                    )
                ) {
                    if (bold) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(segment) }
                    else append(segment)
                }
            } else if (bold) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(segment) }
            } else {
                append(segment)
            }
        }
        when {
            match.value == "<b>" -> bold = true
            match.value == "</b>" -> bold = false
            match.value.startsWith("<a ") -> linkUrl = match.groupValues[1]
            match.value == "</a>" -> linkUrl = null
            match.value.startsWith("<br") -> append("\n")
        }
        idx = match.range.last + 1
    }
    if (idx < html.length) {
        val tail = html.substring(idx)
        if (bold) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(tail) }
        else append(tail)
    }
}
