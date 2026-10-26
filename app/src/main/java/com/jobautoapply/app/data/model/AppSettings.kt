package com.jobautoapply.app.data.model

data class AppSettings(
    val minDelaySeconds: Float = 2.5f,
    val maxDelaySeconds: Float = 4.5f,
    val maxApplicationsPerRun: Int = 30,
    val skipLowMatchInstaMatch: Boolean = true,
    val autoBulkApplySimilar: Boolean = true,
    val skipExternalRedirects: Boolean = true,
    val autoAnswerScreening: Boolean = true,
    val soundAlertsEnabled: Boolean = true
)
