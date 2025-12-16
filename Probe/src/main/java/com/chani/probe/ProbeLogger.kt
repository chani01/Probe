package com.chani.probe

import android.util.Log

class ProbeLogger internal constructor(private val customTag: String) {

    fun e(message: String) {
        Probe.getInstance()?.log(Log.ERROR, message, customTag)
    }

    fun w(message: String) {
        Probe.getInstance()?.log(Log.WARN, message, customTag)
    }

    fun i(message: String) {
        Probe.getInstance()?.log(Log.INFO, message, customTag)
    }

    fun d(message: String) {
        Probe.getInstance()?.log(Log.DEBUG, message, customTag)
    }

    fun v(message: String) {
        Probe.getInstance()?.log(Log.VERBOSE, message, customTag)
    }

    fun t(message: String) {
        Probe.getInstance()?.logTrace(message, customTag)
    }

    fun json(jsonString: String) {
        Probe.getInstance()?.logJson(jsonString, customTag)
    }
}