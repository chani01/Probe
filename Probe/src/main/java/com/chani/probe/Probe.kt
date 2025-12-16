package com.chani.probe

import android.util.Log
import com.chani.probe.internal.JsonFormatter
import com.chani.probe.internal.StackTraceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter



class Probe private constructor(
    private val tag: String,
    private val isLoggingEnabled: Boolean,
    private val logFile: File? = null
)  {

    companion object {
        @Volatile
        private var instance: Probe? = null

        fun init(
            tag: String = "Probe",
            isLoggingEnabled: Boolean = true,
            logFile: File? = null
        ) : Probe {
            return instance ?: synchronized(this) {
                instance ?: Probe(tag, isLoggingEnabled, logFile).also { instance = it }
            }
        }

        /**
         * Get current Probe instance (for internal use)
         */
        internal fun getInstance(): Probe? = instance

        /**
         * Create a logger with custom tag
         */
        fun tag(customTag: String): ProbeLogger {
            return ProbeLogger(customTag)
        }

        fun e(message: String) {
            instance?.log(Log.ERROR, message)
        }

        fun w(message: String) {
            instance?.log(Log.WARN, message)
        }

        fun i(message: String) {
            instance?.log(Log.INFO, message)
        }

        fun d(message: String) {
            instance?.log(Log.DEBUG, message)
        }

        fun v(message: String) {
            instance?.log(Log.VERBOSE, message)
        }

        fun t(message: String) {
            instance?.logTrace(message, null)
        }

        fun json(jsonString: String) {
            instance?.logJson(jsonString, null)
        }
    }


    internal fun log(level: Int, message: String, customTag: String? = null) {
        if (isLoggingEnabled) {
            val logMsg = StackTraceUtil.buildMessage(message = message, 6)
            val logTag = customTag ?: tag
            Log.println(level, logTag, logMsg)
            logFile?.let {
                writeLogToFile(logMsg, logTag)
            }
        }
    }

    private fun writeLogToFile(log: String, logTag: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                FileWriter(logFile, true).use { fw ->
                    PrintWriter(fw).use { pw ->
                        pw.println("[$logTag] $log")
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Log file write failed", e)
            }
        }
    }

    internal fun logJson(jsonString: String, customTag: String? = null, message: String? = null) {
        if (!isLoggingEnabled) return

        val logTag = customTag ?: tag
        val formattedJson = JsonFormatter.format(jsonString)

        if (formattedJson != null) {
            val callerInfo = StackTraceUtil.getCallerInfo(6)

            val prefix = if (message != null) {
                "$callerInfo $message - JSON ▼"
            } else {
                "$callerInfo JSON ▼"
            }

            // 첫 줄 (헤더)
            Log.d(logTag, prefix)

            // JSON 내용 (한 줄에 여러 줄 표시)
            Log.d(logTag, formattedJson)

            // 파일에도 기록
            logFile?.let {
                writeLogToFile("$prefix\n$formattedJson", logTag)
            }
        } else {
            // JSON 파싱 실패 시 원본 그대로 출력
            val callerInfo = StackTraceUtil.getCallerInfo(6)
            val errorMsg = "$callerInfo Invalid JSON (showing raw): $jsonString"
            Log.w(logTag, errorMsg)
            logFile?.let {
                writeLogToFile(errorMsg, logTag)
            }
        }
    }

    internal fun logTrace(message: String, customTag: String? = null) {
        if (!isLoggingEnabled) return

        val logTag = customTag ?: tag
        val stackTrace = Thread.currentThread().stackTrace
        val sb = StringBuilder()

        // 헤더
        sb.append("📍 Call Stack:\n")

        // 사용자 코드만 추출
        stackTrace.forEach { element ->
            val className = element.className
            val fileName = element.fileName

            // Probe 내부, 시스템, 프레임워크 제외
            if (!className.startsWith("com.chani.probe.") &&
                !className.startsWith("dalvik.") &&
                !className.startsWith("java.") &&  // java.* 전체 제외
                !className.startsWith("javax.") &&
                !className.startsWith("android.") &&
                !className.startsWith("androidx.") &&
                !className.startsWith("com.android.") &&  // com.android.* 추가
                !className.startsWith("kotlin.") &&
                !className.startsWith("kotlinx.") &&
                fileName != null &&
                !fileName.endsWith(".jvm.kt")) {
                sb.append("   → $fileName:${element.lineNumber} ${element.methodName}()\n")
            }
        }

        sb.append("   ↳ $message")

        // 로그 출력
        Log.d(logTag, sb.toString())

        // 파일에도 기록
        logFile?.let {
            writeLogToFile(sb.toString(), logTag)
        }
    }
}