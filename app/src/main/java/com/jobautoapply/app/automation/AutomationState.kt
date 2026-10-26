package com.jobautoapply.app.automation

import com.jobautoapply.app.data.model.PlatformType

enum class BotStatus(val label: String) {
    IDLE("Ready"),
    RUNNING("Running"),
    WAITING("Pacing / Delay"),
    PAUSED("Paused"),
    COMPLETED("Completed"),
    ERROR("Error")
}

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val level: String = "info" // info, success, warning, error
)

data class AutomationUiState(
    val currentPlatform: PlatformType = PlatformType.INSTAHYRE,
    val botStatus: BotStatus = BotStatus.IDLE,
    val statusDetail: String = "Waiting for you to log in & start",
    val appliedToday: Int = 0,
    val skippedToday: Int = 0,
    val currentJobTitle: String = "None",
    val currentCompany: String = "None",
    val logs: List<LogEntry> = emptyList(),
    val isWebViewVisible: Boolean = true
)
