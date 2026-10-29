package com.eli.firebridge

import android.util.Log
import dadb.Dadb

object AdbBridge {
    private const val TAG = "FireBridge"
    private var dadb: Dadb? = null
    var lastError: String = ""

    @Synchronized
    fun connect(): Boolean {
        Log.i(TAG, "connect: starting")
        return try {
            dadb?.close()
            dadb = Dadb.create("localhost", 5555)
            Log.i(TAG, "connect: success")
            lastError = ""
            true
        } catch (e: Exception) {
            Log.e(TAG, "connect failed", e)
            lastError = "${e.javaClass.simpleName}: ${e.message ?: "no message"}"
            dadb = null
            false
        }
    }

    @Synchronized
    fun sendKey(keyCode: Int): Boolean {
        Log.i(TAG, "sendKey: $keyCode")
        if (dadb == null && !connect()) return false
        return try {
            dadb!!.shell("input keyevent $keyCode")
            true
        } catch (e: Exception) {
            Log.e(TAG, "sendKey failed, reconnecting", e)
            if (connect()) {
                try {
                    dadb!!.shell("input keyevent $keyCode")
                    return true
                } catch (e2: Exception) {
                    Log.e(TAG, "retry failed", e2)
                    lastError = "${e2.javaClass.simpleName}: ${e2.message ?: "no message"}"
                }
            }
            false
        }
    }
}
