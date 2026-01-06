package com.ilham.oneshadeclone.ui

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View

class TriggerView(context: Context, private val onDrag: (Float) -> Unit) : View(context) {
    init { setBackgroundColor(Color.TRANSPARENT) }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE) {
            onDrag(event.rawY)
        }
        return true // Intercept touch di status bar
    }
}
