package com.jobautoapply.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobautoapply.app.automation.LogEntry
import com.jobautoapply.app.ui.theme.AmberWarning
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.RoseError
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogListView(
    modifier: Modifier = Modifier,
    logs: List<LogEntry>
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    Box(
        modifier = modifier
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No logs yet. Start the bot to begin tracking.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs) { entry ->
                    val dotColor = when (entry.level) {
                        "success" -> EmeraldSuccess
                        "warning" -> AmberWarning
                        "error" -> RoseError
                        else -> PrimaryBlue
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(6.dp)
                                .background(dotColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = timeFormat.format(Date(entry.timestamp)),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.message,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
