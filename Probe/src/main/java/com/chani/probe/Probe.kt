package com.chani.probe

import android.util.Log
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
            instance?.log(Log.ERROR, Log.getStackTraceString(Exception(buildMsg(message))))
        }

        private fun buildMsg(message: String): String {
            val ste = Thread.currentThread().stackTrace[4]
            return "${getCallerInfo()} $message"
        }

        private fun getCallerInfo() : String {
            val stackTrace = Thread.currentThread().stackTrace[8]

            val fileName = stackTrace?.fileName ?: "UnknownFile"
            val methodName = stackTrace?.methodName ?: "UnknownMethod"
            val lineNumber = stackTrace?.lineNumber

            return "$fileName:$lineNumber - $methodName() ::"
        }
    }

    /**
     * Logger with custom tag
     */
    class ProbeLogger internal constructor(private val customTag: String) {

        fun e(message: String) {
            instance?.log(Log.ERROR, message, customTag)
        }

        fun w(message: String) {
            instance?.log(Log.WARN, message, customTag)
        }

        fun i(message: String) {
            instance?.log(Log.INFO, message, customTag)
        }

        fun d(message: String) {
            instance?.log(Log.DEBUG, message, customTag)
        }

        fun v(message: String) {
            instance?.log(Log.VERBOSE, message, customTag)
        }

        fun t(message: String) {
            val stackTrace = Log.getStackTraceString(Exception(buildMsg(message)))
            instance?.log(Log.ERROR, stackTrace, customTag)
        }

        private fun buildMsg(message: String): String {
            return "${getCallerInfo()} $message"
        }

        private fun getCallerInfo(): String {
            val stackTrace = Thread.currentThread().stackTrace[6]

            val fileName = stackTrace?.fileName ?: "UnknownFile"
            val methodName = stackTrace?.methodName ?: "UnknownMethod"
            val lineNumber = stackTrace?.lineNumber

            return "$fileName:$lineNumber - $methodName() ::"
        }
    }


    private fun log(level: Int, message: String, customTag: String? = null) {
        if (isLoggingEnabled) {
            val logMsg = buildMsg(message)
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
}