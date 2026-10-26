package com.jobautoapply.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jobautoapply.app.data.model.AppSettings
import com.jobautoapply.app.data.model.JobApplication
import com.jobautoapply.app.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("job_auto_apply_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _profile = MutableStateFlow(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<JobApplication>> = _history.asStateFlow()

    private fun loadProfile(): UserProfile {
        val json = prefs.getString("user_profile", null) ?: return UserProfile()
        return try {
            gson.fromJson(json, UserProfile::class.java) ?: UserProfile()
        } catch (e: Exception) {
            UserProfile()
        }
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit().putString("user_profile", gson.toJson(profile)).apply()
        _profile.value = profile
    }

    private fun loadSettings(): AppSettings {
        val json = prefs.getString("app_settings", null) ?: return AppSettings()
        return try {
            gson.fromJson(json, AppSettings::class.java) ?: AppSettings()
        } catch (e: Exception) {
            AppSettings()
        }
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().putString("app_settings", gson.toJson(settings)).apply()
        _settings.value = settings
    }

    private fun loadHistory(): List<JobApplication> {
        val json = prefs.getString("job_history", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<JobApplication>>() {}.type
            gson.fromJson<List<JobApplication>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addJobApplication(job: JobApplication) {
        val updated = listOf(job) + _history.value
        prefs.edit().putString("job_history", gson.toJson(updated)).apply()
        _history.value = updated
    }

    fun clearHistory() {
        prefs.edit().remove("job_history").apply()
        _history.value = emptyList()
    }
}
