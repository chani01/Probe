package com.chani.probe

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.chani.probe.internal.JsonFormatter
import com.chani.probe.internal.StackTraceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter



class Probe private constructor(
    private val tag: String,
    private val isLoggingEnabled: Boolean,
    private val logFile: File? = null
)  {

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fileMutex = Mutex()

    companion object {
        @Volatile
        private var instance: Probe? = null

        /**
         * Initializes Probe with the given configuration and returns the singleton instance.
         *
         * If Probe is already initialized, the existing instance is returned as-is.
         * Re-initialization is not supported once Probe has been set up.
         * Call [shutdown] first if reconfiguration is needed.
         *
         * @param tag Default tag used in log output.
         * @param isLoggingEnabled Set to false to suppress all logs (e.g. release builds).
         * @param logFile Optional file to persist logs. If null, file logging is disabled.
         */
        fun init(
            tag: String = "Probe",
            isLoggingEnabled: Boolean = true,
            logFile: File? = null
        ) : Probe {
            return instance?.also {
                Log.w("Probe", "Probe is already initialized. Subsequent init() call is ignored.")
            } ?: synchronized(this) {
                instance ?: Probe(tag, isLoggingEnabled, logFile).also { instance = it }
            }
        }

        /**
         * Reset instance (for testing purposes only)
         */
        @VisibleForTesting
        internal fun reset() {
            synchronized(this) {
                instance = null
            }
        }

        /**
         * Cancels all pending IO coroutines and clears the singleton instance.
         * Call from Application.onTerminate() or test @After teardown.
         */
        fun shutdown() {
            synchronized(this) {
                instance?.ioScope?.cancel()  // IO 코루틴 전부 취소
                instance = null              // 싱글톤 초기화
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

        fun json(jsonString: String, level: Int = Log.DEBUG) {
            instance?.logJson(
                jsonString = jsonString,
                customTag =  null,
                level = level
            )
        }
    }


    internal fun log(level: Int, message: String, customTag: String? = null) {
        if (isLoggingEnabled) {
            val logMsg = StackTraceUtil.buildMessage(message = message)
            val logTag = customTag ?: tag
            Log.println(level, logTag, logMsg)
            logFile?.let { writeLogToFile(logMsg, logTag, it) }
        }
    }

    private fun writeLogToFile(log: String, logTag: String, file: File) {
        ioScope.launch {
            fileMutex.withLock {
                try {
                    FileWriter(file, true).use { fw ->
                        PrintWriter(fw).use { pw ->
                            pw.println("[$logTag] $log")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Log file write failed", e)
                }
            }
        }
    }

    internal fun logJson(
        jsonString: String,
        customTag: String? = null,
        message: String? = null,
        level: Int = Log.DEBUG
    ) {
        if (!isLoggingEnabled) return

        val logTag = customTag ?: tag
        val formattedJson = JsonFormatter.format(jsonString)

        if (formattedJson != null) {
            val callerInfo = StackTraceUtil.getCallerInfo()

            val prefix = if (message != null) {
                "$callerInfo $message - JSON ▼"
            } else {
                "$callerInfo JSON ▼"
            }

            // 첫 줄 (헤더)
            Log.println(level, logTag, prefix)

            // JSON 내용 (한 줄에 여러 줄 표시)
            Log.println(level, logTag, formattedJson)

            // 파일에도 기록
            logFile?.let { writeLogToFile("$prefix\n$formattedJson", logTag, it) }
        } else {
            // JSON 파싱 실패 시 원본 그대로 출력
            val callerInfo = StackTraceUtil.getCallerInfo()
            val errorMsg = "$callerInfo Invalid JSON (showing raw): $jsonString"
            Log.w(logTag, errorMsg)
            logFile?.let { writeLogToFile(errorMsg, logTag, it) }
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
                fileName != null &&
                !StackTraceUtil.isSystemOrFrameworkClass(className, fileName)) {
                sb.append("   → $fileName:${element.lineNumber} ${element.methodName}()\n")
            }
        }

        sb.append("   ↳ $message")

        // 로그 출력
        Log.d(logTag, sb.toString())

        // 파일에도 기록
        logFile?.let { writeLogToFile(sb.toString(), logTag, it) }
    }
}