package com.chani.probesample

import android.app.Application
import com.chani.probe.Probe


class ProbeApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        Probe.init("Probe", true)
    }
}