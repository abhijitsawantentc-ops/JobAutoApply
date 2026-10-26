package com.jobautoapply.app.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobautoapply.app.data.model.ApplicationStatus
import com.jobautoapply.app.data.model.PlatformType
import com.jobautoapply.app.data.repository.AppPreferences
import com.jobautoapply.app.ui.theme.AmberWarning
import com.jobautoapply.app.ui.theme.DarkBackground
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.DarkSurfaceVariant
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.InstahyreTeal
import com.jobautoapply.app.ui.theme.LinkedInBlue
import com.jobautoapply.app.ui.theme.RoseError
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary
import com.jobautoapply.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    appPreferences: AppPreferences
) {
    val history by appPreferences.history.collectAsState()
    val timeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Application History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${history.size} total entries recorded",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            if (history.isNotEmpty()) {
                IconButton(onClick = { appPreferences.clearHistory() }) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Clear History",
                        tint = RoseError
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No applications logged yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Run the auto-apply bot on Instahyre or LinkedIn to record jobs here.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { job ->
                    val statusColor = when (job.status) {
                        ApplicationStatus.APPLIED -> EmeraldSuccess
                        ApplicationStatus.SKIPPED -> AmberWarning
                        ApplicationStatus.EXTERNAL -> TextMuted
                        ApplicationStatus.FAILED -> RoseError
                    }
                    val platformColor = when (job.platform) {
                        PlatformType.INSTAHYRE -> InstahyreTeal
                        PlatformType.LINKEDIN -> LinkedInBlue
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Platform pill
                                Box(
                                    modifier = Modifier
                                        .background(platformColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = job.platform.displayName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = platformColor
                                    )
                                }

                                // Status pill
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(statusColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = job.status.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = statusColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = job.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = job.company,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )

                            if (job.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Note: ${job.notes}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = timeFormat.format(Date(job.appliedAt)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
