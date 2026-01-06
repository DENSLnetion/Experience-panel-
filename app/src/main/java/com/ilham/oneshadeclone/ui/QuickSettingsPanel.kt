package com.ilham.oneshadeclone.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.ilham.oneshadeclone.service.OverlayService

class QuickSettingsPanel @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var panelHeight = 0
    
    // LOGIC SOLID PAINT: Hitam Elegan (#FF141414)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF141414") 
        style = Paint.Style.FILL
        setShadowLayer(24f, 0f, 12f, Color.BLACK)
    }
    
    private val containerView: LinearLayout
    private lateinit var springAnim: SpringAnimation
    var onStateChanged: ((Boolean) -> Unit)? = null

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, backgroundPaint)
        
        containerView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP
            }
            setPadding(40, 120, 40, 60)
            
            // Dummy Tiles buat visualisasi
            for (i in 1..6) {
                val tile = View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 160
                    ).apply { setMargins(0, 24, 0, 0) }
                    setBackgroundColor(Color.parseColor("#FF2B2B2B")) // Dark Grey Tile
                }
                addView(tile)
            }
        }
        addView(containerView)
        translationY = -2000f 
        setupPhysics()
    }

    private fun setupPhysics() {
        springAnim = SpringAnimation(this, DynamicAnimation.TRANSLATION_Y, 0f).apply {
            spring.stiffness = SpringForce.STIFFNESS_LOW
            spring.dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        panelHeight = h
        if (!OverlayService.isPanelExpanded) translationY = -height.toFloat()
    }

    fun onExternalDrag(deltaY: Float) {
        if (!OverlayService.isPanelExpanded) {
            onStateChanged?.invoke(true)
            visibility = View.VISIBLE
        }
        translationY = (deltaY - height).coerceAtMost(0f)
    }

    fun collapsePanel() {
        springAnim.animateToFinalPosition(-height.toFloat())
        springAnim.addEndListener { _, _, _, _ -> onStateChanged?.invoke(false) }
    }
    
    fun expandPanel() {
        visibility = View.VISIBLE
        onStateChanged?.invoke(true)
        springAnim.animateToFinalPosition(0f)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val radius = 56f
        val rect = RectF(0f, -100f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, radius, radius, backgroundPaint)
        super.dispatchDraw(canvas)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (translationY < -height / 4) collapsePanel() else expandPanel()
        }
        return true
    }
}
