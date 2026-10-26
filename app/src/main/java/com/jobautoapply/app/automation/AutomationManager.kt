package com.jobautoapply.app.automation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.jobautoapply.app.data.model.JobApplication
import com.jobautoapply.app.data.model.PlatformType
import com.jobautoapply.app.data.repository.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class AutomationManager(
    private val context: Context,
    private val appPreferences: AppPreferences
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val gson = Gson()

    private val _uiState = MutableStateFlow(AutomationUiState())
    val uiState: StateFlow<AutomationUiState> = _uiState.asStateFlow()

    private var activeWebView: WebView? = null
    private var instahyreScript: String = ""
    private var linkedinScript: String = ""

    init {
        loadScripts()
    }

    private fun loadScripts() {
        try {
            instahyreScript = readAssetFile("instahyre_engine.js")
            linkedinScript = readAssetFile("linkedin_engine.js")
        } catch (e: Exception) {
            addLog("Error reading automation scripts: ${e.message}", "error")
        }
    }

    private fun readAssetFile(filename: String): String {
        val stream = context.assets.open(filename)
        val reader = BufferedReader(InputStreamReader(stream))
        val sb = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            sb.append(line).append("\n")
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun attachWebView(webView: WebView) {
        this.activeWebView = webView

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.allowFileAccess = false
        webSettings.setSupportMultipleWindows(false)
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.loadsImagesAutomatically = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        // Use a standard mobile Chrome User-Agent
        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        val bridge = JobApplyBridge(
            appPreferences = appPreferences,
            onLogReceived = { msg, level -> addLog(msg, level) },
            onStatusChanged = { status, detail -> updateStatus(status, detail) },
            onApplicationRecorded = { job -> onJobRecorded(job) }
        )
        webView.addJavascriptInterface(bridge, "AndroidBridge")

        webView.webChromeClient = object : WebChromeClient() {}

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                addLog("Navigating to: $url", "info")
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                addLog("Page loaded: $url", "info")
                injectAutomationScript(url.orEmpty())
            }
        }

        // Load the initial platform URL
        loadPlatformUrl(_uiState.value.currentPlatform)
    }

    fun switchPlatform(platform: PlatformType) {
        if (_uiState.value.currentPlatform == platform) return
        stop()
        _uiState.value = _uiState.value.copy(
            currentPlatform = platform,
            botStatus = BotStatus.IDLE,
            statusDetail = "Switched to ${platform.displayName}. Please login if needed."
        )
        loadPlatformUrl(platform)
    }

    private fun loadPlatformUrl(platform: PlatformType) {
        activeWebView?.post {
            activeWebView?.loadUrl(platform.homeUrl)
        }
    }

    private fun injectAutomationScript(url: String) {
        val platform = _uiState.value.currentPlatform
        val script = if (platform == PlatformType.LINKEDIN) linkedinScript else instahyreScript
        val engineName = if (platform == PlatformType.LINKEDIN) "window.__LinkedInEngine" else "window.__InstaHyreEngine"

        val configJson = gson.toJson(appPreferences.settings.value)
        val profileJson = gson.toJson(appPreferences.profile.value)

        val wrapper = """
            (function() {
                try {
                    $script
                    if ($engineName) {
                        $engineName.init('$configJson', '$profileJson');
                    }
                } catch(e) {
                    console.error("Injection error:", e);
                }
            })();
        """.trimIndent()

        activeWebView?.evaluateJavascript(wrapper) {
            addLog("Automation engine injected into ${platform.displayName}", "info")
        }
    }

    fun start() {
        val platform = _uiState.value.currentPlatform
        val engineName = if (platform == PlatformType.LINKEDIN) "window.__LinkedInEngine" else "window.__InstaHyreEngine"

        val configJson = gson.toJson(appPreferences.settings.value)
        val profileJson = gson.toJson(appPreferences.profile.value)

        val js = """
            if ($engineName) {
                $engineName.init('$configJson', '$profileJson');
                $engineName.start();
            } else {
                console.warn("Engine not ready yet.");
            }
        """.trimIndent()

        activeWebView?.post {
            activeWebView?.evaluateJavascript(js, null)
        }
        _uiState.value = _uiState.value.copy(
            botStatus = BotStatus.RUNNING,
            statusDetail = "Auto-applying on ${platform.displayName}..."
        )
        addLog("Sent START command to engine", "info")
    }

    fun pause() {
        val engineName = if (_uiState.value.currentPlatform == PlatformType.LINKEDIN) "window.__LinkedInEngine" else "window.__InstaHyreEngine"
        activeWebView?.post {
            activeWebView?.evaluateJavascript("if ($engineName) $engineName.pause();", null)
        }
        _uiState.value = _uiState.value.copy(
            botStatus = BotStatus.PAUSED,
            statusDetail = "Automation paused"
        )
        addLog("Sent PAUSE command", "warning")
    }

    fun resume() {
        val engineName = if (_uiState.value.currentPlatform == PlatformType.LINKEDIN) "window.__LinkedInEngine" else "window.__InstaHyreEngine"
        activeWebView?.post {
            activeWebView?.evaluateJavascript("if ($engineName) $engineName.resume();", null)
        }
        _uiState.value = _uiState.value.copy(
            botStatus = BotStatus.RUNNING,
            statusDetail = "Resuming automation..."
        )
        addLog("Sent RESUME command", "info")
    }

    fun stop() {
        val engineName = if (_uiState.value.currentPlatform == PlatformType.LINKEDIN) "window.__LinkedInEngine" else "window.__InstaHyreEngine"
        activeWebView?.post {
            activeWebView?.evaluateJavascript("if ($engineName) $engineName.stop();", null)
        }
        _uiState.value = _uiState.value.copy(
            botStatus = BotStatus.IDLE,
            statusDetail = "Automation stopped"
        )
        addLog("Sent STOP command", "info")
    }

    fun toggleWebViewVisibility() {
        _uiState.value = _uiState.value.copy(isWebViewVisible = !_uiState.value.isWebViewVisible)
    }

    private fun addLog(message: String, level: String) {
        scope.launch {
            val entry = LogEntry(message = message, level = level)
            val updated = (listOf(entry) + _uiState.value.logs).take(100)
            _uiState.value = _uiState.value.copy(logs = updated)
        }
    }

    private fun updateStatus(status: BotStatus, detail: String) {
        scope.launch {
            _uiState.value = _uiState.value.copy(botStatus = status, statusDetail = detail)
        }
    }

    private fun onJobRecorded(job: JobApplication) {
        scope.launch {
            val applied = if (job.status == com.jobautoapply.app.data.model.ApplicationStatus.APPLIED) {
                _uiState.value.appliedToday + 1
            } else {
                _uiState.value.appliedToday
            }
            val skipped = if (job.status != com.jobautoapply.app.data.model.ApplicationStatus.APPLIED) {
                _uiState.value.skippedToday + 1
            } else {
                _uiState.value.skippedToday
            }
            _uiState.value = _uiState.value.copy(
                appliedToday = applied,
                skippedToday = skipped,
                currentJobTitle = job.title,
                currentCompany = job.company
            )
        }
    }
}
