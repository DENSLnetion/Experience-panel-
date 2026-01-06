package com.ilham.oneshadeclone.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class QSAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { OverlayService.isAccessibilityEnabled = true }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { OverlayService.isAccessibilityEnabled = false }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (OverlayService.isPanelExpanded) {
                sendBroadcast(Intent(OverlayService.ACTION_CLOSE_PANEL))
                return true
            }
        }
        return super.onKeyEvent(event)
    }
}
