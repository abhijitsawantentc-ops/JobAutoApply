package com.jobautoapply.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobautoapply.app.data.model.AppSettings
import com.jobautoapply.app.data.repository.AppPreferences
import com.jobautoapply.app.ui.theme.DarkBackground
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    appPreferences: AppPreferences
) {
    val currentSettings by appPreferences.settings.collectAsState()

    var minDelay by remember(currentSettings) { mutableFloatStateOf(currentSettings.minDelaySeconds) }
    var maxDelay by remember(currentSettings) { mutableFloatStateOf(currentSettings.maxDelaySeconds) }
    var maxApps by remember(currentSettings) { mutableFloatStateOf(currentSettings.maxApplicationsPerRun.toFloat()) }
    var skipLowMatch by remember(currentSettings) { mutableStateOf(currentSettings.skipLowMatchInstaMatch) }
    var autoBulkApply by remember(currentSettings) { mutableStateOf(currentSettings.autoBulkApplySimilar) }
    var skipExternal by remember(currentSettings) { mutableStateOf(currentSettings.skipExternalRedirects) }
    var autoAnswer by remember(currentSettings) { mutableStateOf(currentSettings.autoAnswerScreening) }
    var savedMessage by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bot Rules & Safety Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Configure delays, anti-detection pacing, and application limits.",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pacing & Delays
        SectionBox(title = "HUMAN JITTER & PACING") {
            Text(
                text = "Minimum Delay: ${String.format(java.util.Locale.US, "%.1f", minDelay)} seconds",
                fontSize = 13.sp,
                color = TextPrimary
            )
            Slider(
                value = minDelay,
                onValueChange = { minDelay = it; savedMessage = false },
                valueRange = 1.0f..8.0f,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Maximum Delay: ${String.format(java.util.Locale.US, "%.1f", maxDelay)} seconds",
                fontSize = 13.sp,
                color = TextPrimary
            )
            Slider(
                value = maxDelay,
                onValueChange = { maxDelay = it; savedMessage = false },
                valueRange = 2.0f..12.0f,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Max Applications Per Session: ${maxApps.roundToInt()}",
                fontSize = 13.sp,
                color = TextPrimary
            )
            Slider(
                value = maxApps,
                onValueChange = { maxApps = it; savedMessage = false },
                valueRange = 5f..100f,
                steps = 19,
                colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Rules
        SectionBox(title = "AUTO-FILTERING RULES") {
            SettingToggle(
                title = "Skip Low InstaMatch",
                subtitle = "Skip jobs where Instahyre matches candidate profile poorly",
                checked = skipLowMatch,
                onCheckedChange = { skipLowMatch = it; savedMessage = false }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingToggle(
                title = "Auto Bulk-Apply to Similar Jobs",
                subtitle = "Select recommended matching jobs on Instahyre popup",
                checked = autoBulkApply,
                onCheckedChange = { autoBulkApply = it; savedMessage = false }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingToggle(
                title = "Skip External Redirects",
                subtitle = "Skip jobs requiring 3rd-party career portal forms",
                checked = skipExternal,
                onCheckedChange = { skipExternal = it; savedMessage = false }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SettingToggle(
                title = "Auto-Answer Screening Questions",
                subtitle = "Use profile answers for Easy Apply wizard",
                checked = autoAnswer,
                onCheckedChange = { autoAnswer = it; savedMessage = false }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (savedMessage) {
            Text(
                text = "✓ Settings saved and active!",
                fontSize = 13.sp,
                color = EmeraldSuccess,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val updated = AppSettings(
                    minDelaySeconds = minDelay,
                    maxDelaySeconds = maxDelay,
                    maxApplicationsPerRun = maxApps.roundToInt(),
                    skipLowMatchInstaMatch = skipLowMatch,
                    autoBulkApplySimilar = autoBulkApply,
                    skipExternalRedirects = skipExternal,
                    autoAnswerScreening = autoAnswer
                )
                appPreferences.saveSettings(updated)
                savedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Save Safety Settings")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SettingToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
        )
    }
}
