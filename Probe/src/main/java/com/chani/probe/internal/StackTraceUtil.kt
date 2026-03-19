package com.chani.probe.internal

import android.util.Log

internal object StackTraceUtil {

    fun getCallerInfo(): String {
        val stackTrace = Thread.currentThread().stackTrace
        
        for (element in stackTrace) {
            val className = element.className
            val methodName = element.methodName
            val fileName = element.fileName

            // 1. Probe 라이브러리 제외 (정확히 매칭)
            if (className.startsWith("com.chani.probe.")) {
                continue
            }

            // 2. 파일명이 없으면 제외
            if (fileName == null) {
                continue
            }

            // 3. 시스템/프레임워크 클래스 제외
            if (isSystemOrFrameworkClass(className, fileName)) {
                continue
            }


            val lineNumber = element.lineNumber

            // invoke 계열 메서드는 lambda로 표시
            val displayName = if (isInvokeMethod(methodName)) {
                "lambda"
            } else {
                "$methodName()"
            }

            return "📍 $fileName:$lineNumber → $displayName"
        }

        return "📍 UnknownFile:0 → UnknownMethod()"
    }

    /**
     * 시스템/프레임워크 클래스인지 확인
     */
    internal fun isSystemOrFrameworkClass(className: String, fileName: String): Boolean {
        // 패키지명 기반 필터
        if (className.startsWith("android.") ||
            className.startsWith("androidx.") ||
            className.startsWith("com.android.") ||
            className.startsWith("java.") ||
            className.startsWith("javax.") ||
            className.startsWith("kotlin.") ||
            className.startsWith("kotlinx.") ||
            className.startsWith("dalvik.")) {
            return true
        }

        // 파일명 기반 필터
        if (fileName.endsWith(".jvm.kt") ||
            fileName.contains("ActualJvm") ||
            fileName == "DecorView.java" ||
            fileName == "View.java" ||
            fileName == "ViewGroup.java") {
            return true
        }

        return false
    }

    private fun isInvokeMethod(methodName: String): Boolean {
        return methodName == "invoke" ||
                methodName == "invokeSuspend" ||
                methodName.startsWith("invoke\$")
    }

    fun buildMessage(message: String): String {
        return "${getCallerInfo()}\n   ↳ $message"
    }
}
