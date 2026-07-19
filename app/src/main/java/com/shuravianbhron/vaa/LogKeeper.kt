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
     * Callers are responsible for never passing credentials, passwords, cookies, or API keys to this function.
     * Do not add keyword-based filtering here — it is not a reliable substitute for callers being correct.
     */
    fun logError(tag: String, message: String, exception: Throwable? = null) {
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
