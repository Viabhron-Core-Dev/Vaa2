package com.shuravianbhron.vaa

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LogKeeper {
    private const val MAX_FILE_SIZE = 5 * 1024 * 1024L // 5MB
    private var logFile: File? = null

    fun init(context: Context) {
        logFile = File(context.filesDir, "vaa_app_logs.txt")
    }

    /**
     * Dedicated logging function to prevent ad-hoc exception dumps.
     * Hard rule: NEVER pass sensitive data (passwords, cookies, API keys, PII) to this function.
     */
    fun logError(tag: String, message: String, exception: Throwable? = null) {
        // Enforce hard exclusion: If any sensitive keywords are detected, drop the log entirely.
        // We do NOT redact after the fact.
        val lowerMessage = message.lowercase(Locale.US)
        if (lowerMessage.contains("password") || 
            lowerMessage.contains("cookie") || 
            lowerMessage.contains("api_key") ||
            lowerMessage.contains("apikey") ||
            lowerMessage.contains("token") || 
            lowerMessage.contains("credential")) {
            return
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
        val exceptionStr = exception?.stackTraceToString()?.let { "\n$it" } ?: ""
        val logEntry = "[$timestamp] [$tag]: $message$exceptionStr\n\n"

        try {
            logFile?.let { file ->
                if (file.exists() && file.length() > MAX_FILE_SIZE) {
                    file.writeText("") // Reset file if it exceeds 5MB
                }
                file.appendText(logEntry)
            }
        } catch (e: Throwable) {
            // Failsafe: DO NOT crash the app if logging fails
        }
    }

    fun getLogs(): String {
        return try {
            if (logFile?.exists() == true) {
                logFile?.readText() ?: "No logs available."
            } else {
                "No logs available."
            }
        } catch (e: Throwable) {
            "Error reading logs: ${e.message}"
        }
    }
    
    fun clearLogs() {
        try {
            logFile?.writeText("")
        } catch (e: Throwable) {
            // Ignore
        }
    }
}
