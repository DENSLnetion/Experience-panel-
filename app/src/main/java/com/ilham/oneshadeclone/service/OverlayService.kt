package com.ilham.oneshadeclone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.ilham.oneshadeclone.MainActivity
import com.ilham.oneshadeclone.ui.QuickSettingsPanel
import com.ilham.oneshadeclone.ui.TriggerView

class OverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var triggerView: TriggerView
    private lateinit var panelView: QuickSettingsPanel
    
    companion object {
        const val CHANNEL_ID = "OneShadeSolid"
        const val ACTION_CLOSE_PANEL = "ACTION_CLOSE_PANEL"
        var isPanelExpanded = false
        var isAccessibilityEnabled = false
    }

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_CLOSE_PANEL) panelView.collapsePanel()
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // 1. WAJIB START FOREGROUND DULUAN BIAR GAK CRASH DI ANDROID 14
        startForeground(1, createNotification())
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        try {
            setupTriggerView()
            setupPanelView()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                registerReceiver(commandReceiver, IntentFilter(ACTION_CLOSE_PANEL), RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(commandReceiver, IntentFilter(ACTION_CLOSE_PANEL))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTriggerView() {
        triggerView = TriggerView(this) { deltaY -> panelView.onExternalDrag(deltaY) }
        
        // DEBUG MODE: TRIGGER WARNA MERAH BIAR KELIATAN
        triggerView.setBackgroundColor(Color.parseColor("#44FF0000")) // Merah Transparan

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, 
            80, // Tinggiin dikit biar gampang kena
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP
        windowManager.addView(triggerView, params)
    }

    private fun setupPanelView() {
        panelView = QuickSettingsPanel(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or 
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or 
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or 
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(panelView, params)

        panelView.onStateChanged = { expanded ->
            isPanelExpanded = expanded
            if (expanded) {
                // Ambil alih touch saat expanded
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv() and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            } else {
                // Lepas touch saat collapse
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            windowManager.updateViewLayout(panelView, params)
        }
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "One Shade Service", NotificationManager.IMPORTANCE_LOW)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("One Shade Clone Aktif")
            .setContentText("Panel siap ditarik (Garis Merah)")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true) // Biar ga bisa diswipe user
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            windowManager.removeView(triggerView)
            windowManager.removeView(panelView)
        } catch (e: Exception) {}
        unregisterReceiver(commandReceiver)
    }
    override fun onBind(intent: Intent?): IBinder? = null
}
