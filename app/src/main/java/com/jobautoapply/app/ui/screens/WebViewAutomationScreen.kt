package com.jobautoapply.app.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.jobautoapply.app.automation.AutomationManager
import com.jobautoapply.app.automation.AutomationUiState
import com.jobautoapply.app.automation.BotStatus
import com.jobautoapply.app.ui.theme.AmberWarning
import com.jobautoapply.app.ui.theme.DarkBackground
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.RoseError
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary

@Composable
fun WebViewAutomationScreen(
    state: AutomationUiState,
    automationManager: AutomationManager
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Intercept hardware back button when WebView can go back
    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mini Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(0.5.dp, DarkCardBorder)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { webViewInstance?.goBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = state.currentPlatform.displayName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (state.botStatus == BotStatus.RUNNING) "Autonomous Engine Active" else "Interactive Login & Navigation",
                            fontSize = 10.sp,
                            color = if (state.botStatus == BotStatus.RUNNING) EmeraldSuccess else TextMuted
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { webViewInstance?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = TextPrimary)
                    }
                    StatusBadge(status = state.botStatus)
                }
            }

            // Embedded Android WebView
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        automationManager.attachWebView(this)
                        webViewInstance = this
                    }
                },
                update = {
                    webViewInstance = it
                }
            )
        }

        // Floating Bot HUD at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.botStatus == BotStatus.RUNNING) "APPLYING: ${state.currentJobTitle}" else "BOT STATUS: ${state.botStatus.label}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = state.statusDetail,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Control Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.botStatus == BotStatus.RUNNING) {
                        FloatingActionButton(
                            onClick = { automationManager.pause() },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            containerColor = AmberWarning
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = TextPrimary)
                        }
                    } else if (state.botStatus == BotStatus.PAUSED) {
                        FloatingActionButton(
                            onClick = { automationManager.resume() },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            containerColor = EmeraldSuccess
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = TextPrimary)
                        }
                    } else {
                        FloatingActionButton(
                            onClick = { automationManager.start() },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            containerColor = PrimaryBlue
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = TextPrimary)
                        }
                    }

                    if (state.botStatus != BotStatus.IDLE) {
                        FloatingActionButton(
                            onClick = { automationManager.stop() },
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            containerColor = RoseError
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}
