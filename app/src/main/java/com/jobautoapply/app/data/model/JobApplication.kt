package com.jobautoapply.app.data.model

data class JobApplication(
    val id: String,
    val title: String,
    val company: String,
    val platform: PlatformType,
    val status: ApplicationStatus,
    val appliedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class PlatformType(val displayName: String, val homeUrl: String) {
    INSTAHYRE("Instahyre", "https://www.instahyre.com/candidate/opportunities/"),
    LINKEDIN("LinkedIn Easy Apply", "https://www.linkedin.com/jobs/")
}

enum class ApplicationStatus(val label: String) {
    APPLIED("Applied"),
    SKIPPED("Skipped"),
    EXTERNAL("External Redirect"),
    FAILED("Failed")
}
