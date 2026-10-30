package com.eli.firebridge

import android.content.Context
import android.util.Log
import dadb.AdbKeyPair
import dadb.Dadb
import java.io.File

object AdbBridge {
    private const val TAG = "FireBridge"
    private const val HOST = "10.0.0.76"
    private var dadb: Dadb? = null
    private var appContext: Context? = null
    var lastError: String = ""

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Creates the key pair once and stores it in the app's private folder,
    // so the TV only needs to approve it once.
    private fun keyPair(): AdbKeyPair {
        val dir = appContext?.filesDir ?: throw IllegalStateException("AdbBridge.init() was not called")
        val priv = File(dir, "adbkey")
        val pub = File(dir, "adbkey.pub")
        if (!priv.exists() || !pub.exists()) {
            AdbKeyPair.generate(priv, pub)
        }
        return AdbKeyPair.read(priv, pub)
    }

    @Synchronized
    fun connect(): Boolean {
        Log.i(TAG, "connect: starting")
        return try {
            dadb?.close()
            val candidate = Dadb.create(HOST, 5555, keyPair())
            // Dadb connects lazily, so run a real command to force the handshake.
            candidate.shell("echo ok")
            dadb = candidate
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