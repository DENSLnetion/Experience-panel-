package com.ilham.oneshadeclone.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
// import com.ilham.oneshadeclone.R  <-- INI PEMBUNUHNYA, KITA HAPUS
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
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupTriggerView()
        setupPanelView()
        startForeground(1, createNotification())
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(commandReceiver, IntentFilter(ACTION_CLOSE_PANEL), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(commandReceiver, IntentFilter(ACTION_CLOSE_PANEL))
        }
    }

    private fun setupTriggerView() {
        triggerView = TriggerView(this) { deltaY -> panelView.onExternalDrag(deltaY) }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, 60,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
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
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(panelView, params)

        panelView.onStateChanged = { expanded ->
            isPanelExpanded = expanded
            if (expanded) {
                params.flags = params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv() and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
            } else {
                params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            }
            windowManager.updateViewLayout(panelView, params)
        }
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(CHANNEL_ID, "One Shade", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("One Shade Active")
            .setSmallIcon(android.R.drawable.sym_def_app_icon) 
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
