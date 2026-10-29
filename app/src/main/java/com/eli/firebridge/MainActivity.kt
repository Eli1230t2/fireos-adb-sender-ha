package com.eli.firebridge

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private val keys = mapOf(
        R.id.btnUp to 19,
        R.id.btnDown to 20,
        R.id.btnLeft to 21,
        R.id.btnRight to 22,
        R.id.btnSelect to 23,
        R.id.btnBack to 4,
        R.id.btnHome to 3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val status = findViewById<TextView>(R.id.status)

        findViewById<Button>(R.id.btnConnect).setOnClickListener {
            status.text = "Connecting..."
            val executor = Executors.newSingleThreadExecutor()
            val future = executor.submit<Boolean> { AdbBridge.connect() }
            thread {
                val message = try {
                    if (future.get(15, TimeUnit.SECONDS)) {
                        "Connected"
                    } else {
                        "Failed: ${AdbBridge.lastError}"
                    }
                } catch (e: TimeoutException) {
                    future.cancel(true)
                    "Timed out after 15 seconds. Approve the ADB prompt on the TV, then press Connect again."
                } catch (e: Exception) {
                    "Error: ${e.javaClass.simpleName}"
                } finally {
                    executor.shutdownNow()
                }
                runOnUiThread { status.text = message }
            }
        }

        for ((id, code) in keys) {
            findViewById<Button>(id).setOnClickListener {
                thread {
                    val ok = AdbBridge.sendKey(code)
                    runOnUiThread { status.text = if (ok) "Sent key $code" else "Failed to send key $code" }
                }
            }
        }
    }
}
