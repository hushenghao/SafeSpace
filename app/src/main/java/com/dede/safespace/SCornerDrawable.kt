package com.dede.safespace

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.RoundedCorner
import android.view.WindowInsets
import androidx.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.S)
class SCornerDrawable : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var privacyIndicatorBounds: Rect? = null
    private var roundedCorners: List<RoundedCorner?>? = null

    fun setWindowInsets(showPrivacyIndicator: Boolean, insets: WindowInsets?) {
        privacyIndicatorBounds = if (showPrivacyIndicator) {
            insets?.privacyIndicatorBounds
        } else {
            null
        }
        roundedCorners = arrayOf(
            RoundedCorner.POSITION_TOP_LEFT,
            RoundedCorner.POSITION_TOP_RIGHT,
            RoundedCorner.POSITION_BOTTOM_RIGHT,
            RoundedCorner.POSITION_BOTTOM_LEFT
        ).map { insets?.getRoundedCorner(it) }
        invalidateSelf()
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
    }

    override fun draw(canvas: Canvas) {
        val roundedCorners = this.roundedCorners
        if (roundedCorners != null) {
            for (roundedCorner in roundedCorners) {
                if (roundedCorner == null || roundedCorner.radius <= 0f) {
                    continue
                }
                val x = roundedCorner.center.x.toFloat()
                val y = roundedCorner.center.y.toFloat()
                val radius = roundedCorner.radius.toFloat()
//                val offset = (radius * sin(Math.toRadians(45.0))).toFloat()
                paint.color = Color.GREEN
                canvas.drawCircle(x, y, radius, paint)

                paint.textSize = radius * 0.5f
                paint.color = Color.RED
                val fontMetrics = paint.fontMetrics
                canvas.drawText(
                    roundedCorner.position.toString(),
                    x,
                    y - (fontMetrics.top + fontMetrics.bottom) / 2f,
                    paint
                )
            }
        }

        val rect = this.privacyIndicatorBounds
        if (rect != null && !rect.isEmpty) {
            paint.color = Color.RED
            canvas.drawRect(rect, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }
}