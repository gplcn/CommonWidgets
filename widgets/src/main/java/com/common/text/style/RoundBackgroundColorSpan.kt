package com.common.text.style

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.TextPaint
import android.text.style.ReplacementSpan

class RoundBackgroundColorSpan(
    private val bgColor: Int,
    private val textColor: Int,
    private val mRadius: Int,
    private val mStrokeWidth: Float,
    private val mfontSizePx: Float,
    private val mLeftMargin: Float,
    private val mRightMargin: Float,
    private val mLRpadding: Float
) : ReplacementSpan() {
    private var mSize = 0
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: FontMetricsInt
    ): Int {
        val p: Paint = getCustomTextPaint(paint)
        mSize = (p.measureText(text, start, end) + (2 * mRadius).toFloat()).toInt()
        return (mSize.toFloat() + mLRpadding * 2.0f + mLeftMargin + mRightMargin).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        var x = x
        val defaultColor = paint.color
        val defaultStrokeWidth = paint.strokeWidth
        x += mLeftMargin
        val p: Paint = getCustomTextPaint(paint)
        val fm = p.fontMetricsInt
        val div = Math.abs((y + fm.descent + y + fm.ascent) / 2 - (bottom + top) / 2)
        p.color = bgColor
        p.style = Paint.Style.STROKE
        val halfStrokeWidth = mStrokeWidth / 2.0f
        p.strokeWidth = mStrokeWidth
        p.isAntiAlias = true
        val left = x + halfStrokeWidth
        val top1 =
            y.toFloat() + halfStrokeWidth + p.ascent() - div.toFloat()
        val right =
            x - halfStrokeWidth + mSize.toFloat() + 2.0f * mLRpadding
        val bottom1 =
            y.toFloat() - halfStrokeWidth + p.descent() - div.toFloat()
        val rectF = RectF(left, top1, right, bottom1)
        canvas.drawRoundRect(rectF, mRadius.toFloat(), mRadius.toFloat(), p)
        p.style = Paint.Style.FILL
        p.color = textColor
        p.strokeWidth = defaultStrokeWidth
        val x1 = x + mRadius.toFloat() + mLRpadding
        val y1 = y - div
        canvas.drawText(text, start, end, x1, y1.toFloat(), p)
        paint.color = defaultColor
    }

    private fun getCustomTextPaint(srcPaint: Paint): TextPaint {
        val paint = TextPaint(srcPaint)
        paint.textSize = mfontSizePx
        return paint
    }

}