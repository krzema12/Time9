package it.krzeminski.time9.utils

import android.os.Handler

class PeriodicViewRefresher(private val functionToCall: () -> Unit) {
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun start(delayMillis: Long) {
        handler = Handler()

        runnable = object : Runnable {
            override fun run() {
                functionToCall()
                handler.postDelayed(this, delayMillis)
            }
        }

        handler.postDelayed(runnable, delayMillis)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }
}
