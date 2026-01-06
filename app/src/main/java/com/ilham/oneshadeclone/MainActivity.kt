package com.ilham.oneshadeclone

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.ilham.oneshadeclone.service.OverlayService

class MainActivity : Activity() { // Pake Activity biasa biar ringann
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BIKIN UI TANPA XML (ANTI ERROR RESOURCE)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "One Shade Clone"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val subtitle = TextView(this).apply {
            text = "By Ilham Danial Saputra\nSMAN 78 Jakarta"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val btnStart = Button(this).apply {
            text = "START SERVICE"
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            setOnClickListener {
                checkPermissionAndStart()
            }
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(btnStart)

        setContentView(layout)
        
        // Auto start kalo udah ada izin
        if (Settings.canDrawOverlays(this)) {
            startService(Intent(this, OverlayService::class.java))
        }
    }

    private fun checkPermissionAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivityForResult(intent, 123)
        } else {
            startService(Intent(this, OverlayService::class.java))
        }
    }
}
