package com.chani.probe.internal

internal object StackTraceUtil {

    fun getCallerInfo(stackOffset: Int = 8): String {
        val stackTrace = Thread.currentThread().stackTrace

        if (stackOffset < stackTrace.size) {
            val element = stackTrace[stackOffset]
            val fileName = element?.fileName ?: "UnknownFile"
            val methodName = element?.methodName ?: "UnknownMethod"
            val lineNumber = element?.lineNumber ?: 0

            return "📍 $fileName:$lineNumber → $methodName()"
        }

        return "UnknownFile:0 - UnknownMethod() ::"
    }

    fun buildMessage(message: String, stackOffset: Int): String {
        return "${getCallerInfo(stackOffset)}\n   ↳ $message"
    }
}