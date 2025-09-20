package com.example.sbooks.utils

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.example.sbooks.R

class CircleImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.gray_light)
    }

    private var borderWidth = 4f
    private var borderColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private var backgroundColor = ContextCompat.getColor(context, R.color.gray_light)

    init {
        // Get custom attributes if any
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircleImageView,
            0, 0
        ).apply {
            try {
                borderWidth = getDimension(R.styleable.CircleImageView_border_width, 4f)
                borderColor = getColor(R.styleable.CircleImageView_border_color,
                    ContextCompat.getColor(context, R.color.colorPrimary))
                backgroundColor = getColor(R.styleable.CircleImageView_background_color,
                    ContextCompat.getColor(context, R.color.gray_light))
            } finally {
                recycle()
            }
        }

        // Update paint colors
        borderPaint.apply {
            strokeWidth = borderWidth
            color = borderColor
        }
        backgroundPaint.color = backgroundColor

        scaleType = ScaleType.CENTER_CROP
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: run {
            // Draw background circle if no image
            drawBackground(canvas)
            return
        }

        if (width == 0 || height == 0) return

        val bitmap = getBitmapFromDrawable(drawable)
        val circularBitmap = getCircularBitmap(bitmap)

        if (circularBitmap != null) {
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = (minOf(width, height) - borderWidth) / 2f

            // Draw background circle
            canvas?.drawCircle(centerX, centerY, radius + borderWidth/2, backgroundPaint)

            // Draw image
            canvas?.drawBitmap(circularBitmap, centerX - circularBitmap.width/2f,
                centerY - circularBitmap.height/2f, paint)

            // Draw border
            canvas?.drawCircle(centerX, centerY, radius, borderPaint)
        } else {
            drawBackground(canvas)
        }
    }

    private fun drawBackground(canvas: Canvas?) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (minOf(width, height) - borderWidth) / 2f

        // Draw background circle
        canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw border
        canvas?.drawCircle(centerX, centerY, radius, borderPaint)

        // Draw default icon (user icon)
        val defaultIcon = ContextCompat.getDrawable(context, R.drawable.ic_users)
        defaultIcon?.let { drawable ->
            val iconSize = (radius * 0.6f).toInt()
            val left = (centerX - iconSize/2).toInt()
            val top = (centerY - iconSize/2).toInt()
            val right = left + iconSize
            val bottom = top + iconSize

            drawable.setBounds(left, top, right, bottom)
            drawable.setTint(ContextCompat.getColor(context, R.color.colorTextSecondary))
            drawable.draw(canvas!!)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        return try {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null) return null

        val size = minOf(width - borderWidth.toInt(), height - borderWidth.toInt())
        if (size <= 0) return null

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
            shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }

        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        return output
    }

    // Public methods to customize appearance
    fun setBorderWidth(width: Float) {
        borderWidth = width
        borderPaint.strokeWidth = width
        invalidate()
    }

    fun setBorderColor(color: Int) {
        borderColor = color
        borderPaint.color = color
        invalidate()
    }

    fun setBackgroundCircleColor(color: Int) {
        backgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }
}