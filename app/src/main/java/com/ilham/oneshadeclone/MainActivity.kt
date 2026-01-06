package com.ilham.oneshadeclone

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.ilham.oneshadeclone.service.OverlayService
import com.ilham.oneshadeclone.service.QSAccessibilityService

class MainActivity : Activity() {
    
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI GOD MODE (No XML)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "ONE SHADE CLONE"
            textSize = 24f
            setTextColor(Color.CYAN)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 20)
        }
        
        statusText = TextView(this).apply {
            text = "Status: Memeriksa Izin..."
            textSize = 14f
            setTextColor(Color.YELLOW)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }

        // TOMBOL 1: OVERLAY
        val btnOverlay = Button(this).apply {
            text = "1. IZIN OVERLAY (Wajib)"
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            setOnClickListener { requestOverlayPermission() }
        }

        // TOMBOL 2: ACCESSIBILITY (INI KUNCINYA HAM!)
        val btnAccess = Button(this).apply {
            text = "2. IZIN ACCESSIBILITY (Wajib)"
            setBackgroundColor(Color.DKGRAY)
            setTextColor(Color.WHITE)
            setOnClickListener { requestAccessibilityPermission() }
        }
        
        // TOMBOL 3: START
        val btnStart = Button(this).apply {
            text = "3. START SERVICE"
            setBackgroundColor(Color.parseColor("#006400")) // Dark Green
            setTextColor(Color.WHITE)
            setOnClickListener { startAppService() }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(btnOverlay)
        layout.addView(createSpacer())
        layout.addView(btnAccess)
        layout.addView(createSpacer())
        layout.addView(btnStart)

        setContentView(layout)
        
        checkPermissions()
        
        // Minta izin notifikasi buat Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
    
    private fun createSpacer(): TextView {
        return TextView(this).apply { height = 30 }
    }

    override fun onResume() {
        super.onResume()
        checkPermissions()
    }

    private fun checkPermissions() {
        val overlay = Settings.canDrawOverlays(this)
        val access = isAccessibilityEnabled()
        
        if (overlay && access) {
            statusText.text = "SEMUA IZIN LENGKAP!\nSilakan Klik Start Service"
            statusText.setTextColor(Color.GREEN)
        } else {
            statusText.text = "Izin Belum Lengkap:\nOverlay: $overlay\nAccessibility: $access"
            statusText.setTextColor(Color.RED)
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        } else {
            Toast.makeText(this, "Overlay Sudah Aktif!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestAccessibilityPermission() {
        if (!isAccessibilityEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(this, "Cari 'One Shade Clone' & Aktifkan", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Accessibility Sudah Aktif!", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Logic Murni buat cek Accessibility Service nyala atau gak
    private fun isAccessibilityEnabled(): Boolean {
        val expectedComponentName = ComponentName(this, QSAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val stringColonSplitter = TextUtils.SimpleStringSplitter(':')
        stringColonSplitter.setString(enabledServicesSetting)
        while (stringColonSplitter.hasNext()) {
            val componentNameString = stringColonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) return true
        }
        return false
    }

    private fun startAppService() {
        if (Settings.canDrawOverlays(this) && isAccessibilityEnabled()) {
            try {
                startService(Intent(this, OverlayService::class.java))
                Toast.makeText(this, "Service Started!", Toast.LENGTH_SHORT).show()
                finish() // Tutup activity biar ga ganggu
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Lengkapi Izin Dulu Bro!", Toast.LENGTH_SHORT).show()
        }
    }
}
