package com.eli.firebridge

import dadb.Dadb

// Keeps one ADB connection open to this device and reuses it for every key press.
object AdbBridge {
    private var dadb: Dadb? = null

    @Synchronized
    fun connect(): Boolean {
        return try {
            dadb?.close()
            dadb = Dadb.create("localhost", 5555)
            true
        } catch (e: Exception) {
            dadb = null
            false
        }
    }

    @Synchronized
    fun sendKey(keyCode: Int): Boolean {
        if (dadb == null && !connect()) return false
        return try {
            dadb!!.shell("input keyevent $keyCode")
            true
        } catch (e: Exception) {
            // Connection dropped: reconnect once and retry
            if (connect()) {
                try {
                    dadb!!.shell("input keyevent $keyCode")
                    return true
                } catch (_: Exception) {}
            }
            false
        }
    }
}
