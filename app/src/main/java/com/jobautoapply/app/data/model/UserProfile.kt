package com.jobautoapply.app.data.model

data class UserProfile(
    val fullName: String = "Candidate",
    val email: String = "candidate@example.com",
    val phoneNumber: String = "+919876543210",
    val totalExperienceYears: Float = 3.0f,
    val currentCtcLakhs: Float = 12.0f,
    val expectedCtcLakhs: Float = 18.0f,
    val noticePeriodDays: Int = 30,
    val isAuthorizedToWork: Boolean = true,
    val requiresVisaSponsorship: Boolean = false,
    val linkedInUrl: String = "",
    val githubUrl: String = "",
    val portfolioUrl: String = "",
    val preferredLocations: String = "Bangalore, Remote, Pune, Hyderabad"
)
