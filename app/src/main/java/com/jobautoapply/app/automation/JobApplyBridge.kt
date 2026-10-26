package com.jobautoapply.app.automation

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import com.jobautoapply.app.data.model.ApplicationStatus
import com.jobautoapply.app.data.model.JobApplication
import com.jobautoapply.app.data.model.PlatformType
import com.jobautoapply.app.data.repository.AppPreferences

class JobApplyBridge(
    private val appPreferences: AppPreferences,
    private val onLogReceived: (String, String) -> Unit,
    private val onStatusChanged: (BotStatus, String) -> Unit,
    private val onApplicationRecorded: (JobApplication) -> Unit
) {
    private val gson = Gson()

    @JavascriptInterface
    fun onLog(message: String, level: String) {
        onLogReceived(message, level)
    }

    @JavascriptInterface
    fun onStatusUpdate(status: String, details: String) {
        val botStatus = try {
            BotStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            BotStatus.RUNNING
        }
        onStatusChanged(botStatus, details)
    }

    @JavascriptInterface
    fun onJobApplied(title: String, company: String, jobId: String, platformKey: String) {
        val platform = if (platformKey.contains("linkedin", ignoreCase = true)) {
            PlatformType.LINKEDIN
        } else {
            PlatformType.INSTAHYRE
        }

        val job = JobApplication(
            id = jobId.ifBlank { "job_${System.currentTimeMillis()}" },
            title = title,
            company = company,
            platform = platform,
            status = ApplicationStatus.APPLIED,
            appliedAt = System.currentTimeMillis()
        )
        appPreferences.addJobApplication(job)
        onApplicationRecorded(job)
    }

    @JavascriptInterface
    fun onJobSkipped(title: String, company: String, reason: String, platformKey: String) {
        val platform = if (platformKey.contains("linkedin", ignoreCase = true)) {
            PlatformType.LINKEDIN
        } else {
            PlatformType.INSTAHYRE
        }

        val status = if (reason.contains("external", ignoreCase = true)) {
            ApplicationStatus.EXTERNAL
        } else {
            ApplicationStatus.SKIPPED
        }

        val job = JobApplication(
            id = "skip_${System.currentTimeMillis()}",
            title = title,
            company = company,
            platform = platform,
            status = status,
            notes = reason
        )
        appPreferences.addJobApplication(job)
        onApplicationRecorded(job)
    }

    @JavascriptInterface
    fun getProfileJson(): String {
        return gson.toJson(appPreferences.profile.value)
    }

    @JavascriptInterface
    fun getConfigJson(): String {
        return gson.toJson(appPreferences.settings.value)
    }
}
