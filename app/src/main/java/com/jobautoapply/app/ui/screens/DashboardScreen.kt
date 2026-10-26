package com.jobautoapply.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobautoapply.app.automation.AutomationManager
import com.jobautoapply.app.automation.AutomationUiState
import com.jobautoapply.app.automation.BotStatus
import com.jobautoapply.app.data.model.PlatformType
import com.jobautoapply.app.ui.components.LogListView
import com.jobautoapply.app.ui.components.StatCard
import com.jobautoapply.app.ui.theme.AmberWarning
import com.jobautoapply.app.ui.theme.DarkBackground
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.DarkSurfaceVariant
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.InstahyreTeal
import com.jobautoapply.app.ui.theme.LinkedInBlue
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.RoseError
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary
import com.jobautoapply.app.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    state: AutomationUiState,
    automationManager: AutomationManager,
    onNavigateToBot: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "JobAutoApply",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Autonomous Application Engine",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // Status Indicator Badge
            StatusBadge(status = state.botStatus)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Platform Switcher
        Text(
            text = "TARGET PLATFORM",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlatformTab(
                modifier = Modifier.weight(1f),
                title = "Instahyre",
                isSelected = state.currentPlatform == PlatformType.INSTAHYRE,
                selectedColor = InstahyreTeal,
                onClick = { automationManager.switchPlatform(PlatformType.INSTAHYRE) }
            )
            PlatformTab(
                modifier = Modifier.weight(1f),
                title = "LinkedIn Easy Apply",
                isSelected = state.currentPlatform == PlatformType.LINKEDIN,
                selectedColor = LinkedInBlue,
                onClick = { automationManager.switchPlatform(PlatformType.LINKEDIN) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stat Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Applied Today",
                value = "${state.appliedToday}",
                valueColor = EmeraldSuccess,
                subtitle = "Active submissions"
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Skipped / Filtered",
                value = "${state.skippedToday}",
                valueColor = AmberWarning,
                subtitle = "Low match or external"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Current Action Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(14.dp))
                .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "CURRENT TARGET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.currentJobTitle,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (state.currentCompany.isNotBlank()) "at ${state.currentCompany}" else "Ready to scan",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.statusDetail,
                    fontSize = 12.sp,
                    color = when (state.botStatus) {
                        BotStatus.RUNNING -> EmeraldSuccess
                        BotStatus.WAITING -> AmberWarning
                        BotStatus.PAUSED -> AmberWarning
                        BotStatus.ERROR -> RoseError
                        else -> TextMuted
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Bot Control Action Buttons
        Text(
            text = "AUTOMATION CONTROLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.botStatus == BotStatus.RUNNING) {
                Button(
                    onClick = { automationManager.pause() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pause")
                }
            } else if (state.botStatus == BotStatus.PAUSED) {
                Button(
                    onClick = { automationManager.resume() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume")
                }
            } else {
                Button(
                    onClick = { automationManager.start() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Bot")
                }
            }

            OutlinedButton(
                onClick = { automationManager.stop() },
                modifier = Modifier.weight(0.8f),
                enabled = state.botStatus != BotStatus.IDLE
            ) {
                Icon(Icons.Default.Stop, contentDescription = "Stop", tint = RoseError, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Stop", color = RoseError)
            }

            OutlinedButton(
                onClick = onNavigateToBot,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = "Live View", tint = TextPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Live View", color = TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Activity Logs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE EVENT LOGS",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Text(
                text = "${state.logs.size} events",
                fontSize = 11.sp,
                color = TextMuted
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LogListView(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            logs = state.logs
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatusBadge(status: BotStatus) {
    val (bgColor, dotColor) = when (status) {
        BotStatus.RUNNING -> EmeraldSuccess.copy(alpha = 0.15f) to EmeraldSuccess
        BotStatus.WAITING -> AmberWarning.copy(alpha = 0.15f) to AmberWarning
        BotStatus.PAUSED -> AmberWarning.copy(alpha = 0.15f) to AmberWarning
        BotStatus.COMPLETED -> PrimaryBlue.copy(alpha = 0.15f) to PrimaryBlue
        BotStatus.ERROR -> RoseError.copy(alpha = 0.15f) to RoseError
        BotStatus.IDLE -> DarkSurfaceVariant to TextMuted
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, dotColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = status.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = dotColor
        )
    }
}

@Composable
fun PlatformTab(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) selectedColor else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) TextPrimary else TextMuted
        )
    }
}
