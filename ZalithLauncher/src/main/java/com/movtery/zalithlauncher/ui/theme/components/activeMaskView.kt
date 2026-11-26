package com.movtery.zalithlauncher.ui.theme.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.graphics.createBitmap
import kotlin.math.hypot

fun Context.activeMaskView(
    animTime: Long = 800,
    maskComplete: () -> Unit,
    maskAnimFinish: () -> Unit
) {
    // PixelCopy was added in API 26. Apply the theme immediately on Lollipop
    // instead of trying to load/call that class on older Android releases.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        maskComplete()
        maskAnimFinish()
        return
    }

    val activity = findActivity()
    val window = activity?.window
    val rootView = window?.decorView?.rootView as? ViewGroup
    if (activity == null || window == null || rootView == null) {
        maskComplete()
        maskAnimFinish()
        return
    }

    captureView(rootView, window) { bitmap ->
        // Follow-system theme changes must still be applied if PixelCopy is not
        // available (for example while the window is being recreated or hidden).
        if (bitmap == null || activity.isFinishing || activity.isDestroyed || !rootView.isAttachedToWindow) {
            bitmap?.recycle()
            maskComplete()
            maskAnimFinish()
            return@captureView
        }

        val maskView = MaskView(activity, bitmap)
        try {
            rootView.addView(maskView)
        } catch (_: RuntimeException) {
            maskView.releaseBitmap()
            maskComplete()
            maskAnimFinish()
            return@captureView
        }

        maskComplete()
        maskView.animActive(animTime) {
            rootView.removeView(maskView)
            maskView.releaseBitmap()
            maskAnimFinish()
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun captureView(view: View, window: Window, bitmapCallback: (Bitmap?) -> Unit) {
    val width = view.width
    val height = view.height
    if (width <= 0 || height <= 0) {
        bitmapCallback(null)
        return
    }

    val bitmap = try {
        createBitmap(width, height)
    } catch (_: IllegalArgumentException) {
        bitmapCallback(null)
        return
    }
    val location = IntArray(2)
    view.getLocationInWindow(location)

    try {
        PixelCopy.request(
            window,
            Rect(location[0], location[1], location[0] + width, location[1] + height),
            bitmap,
            { result ->
                if (result == PixelCopy.SUCCESS) {
                    bitmapCallback(bitmap)
                } else {
                    bitmap.recycle()
                    bitmapCallback(null)
                }
            },
            Handler(Looper.getMainLooper())
        )
    } catch (_: RuntimeException) {
        bitmap.recycle()
        bitmapCallback(null)
    }
}

@SuppressLint("ViewConstructor")
private class MaskView(
    context: Context,
    bitmap: Bitmap
) : View(context) {
    private var bitmap: Bitmap? = bitmap
    private var maskRadius = 0f
    private val paint = Paint(ANTI_ALIAS_FLAG)

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val currentBitmap = bitmap ?: return
        with(canvas) {
            val layer = saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            drawBitmap(currentBitmap, 0f, 0f, null)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            paint.maskFilter = BlurMaskFilter(60f, BlurMaskFilter.Blur.NORMAL)
            drawCircle(0f, height.toFloat() / 2f, maskRadius, paint)
            paint.xfermode = null
            restoreToCount(layer)
        }
    }

    fun releaseBitmap() {
        bitmap?.recycle()
        bitmap = null
    }

    fun animActive(animTime: Long, animFinish: () -> Unit) {
        ValueAnimator.ofFloat(
            0f,
            hypot(rootView.width.toFloat(), rootView.height.toFloat())
        ).apply {
            duration = animTime
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                maskRadius = valueAnimator.animatedValue as Float
                invalidate()
            }
            addListener(onEnd = { animFinish() })
        }.start()
    }
}