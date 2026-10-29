package com.eli.firebridge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.concurrent.thread

class KeyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val keyCode = intent.getIntExtra("keycode", -1)
        if (keyCode == -1) return
        // Run off the main thread, since the ADB call does network I/O
        thread { AdbBridge.sendKey(keyCode) }
    }
}
