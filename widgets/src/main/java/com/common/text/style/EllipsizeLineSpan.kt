package com.common.text.style

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.style.LineBackgroundSpan
import android.text.style.ReplacementSpan

/**
 * 可以将超出控件宽度的文字单行显示在行末添加省略号"..."
 */
class EllipsizeLineSpan : ReplacementSpan(), LineBackgroundSpan {
    private var layoutLeft = 0
    private var layoutRight = 0

    override fun drawBackground(
        c: Canvas, p: Paint,
        left: Int, right: Int,
        top: Int, baseline: Int, bottom: Int,
        text: CharSequence, start: Int, end: Int,
        lnum: Int
    ) {
        val clipRect = Rect()
        c.getClipBounds(clipRect)
        layoutLeft = clipRect.left
        layoutRight = clipRect.right
    }

    override fun getSize(
        paint: Paint, text: CharSequence, start: Int, end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return layoutRight - layoutLeft
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int,
        x: Float, top: Int, y: Int, bottom: Int, paint: Paint
    ) {
        var end = end
        val textWidth = paint.measureText(text, start, end)

        if (x + Math.ceil(textWidth.toDouble()).toInt() < layoutRight) {
            //文字不足一行时
            canvas.drawText(text, start, end, x, y.toFloat(), paint)
        } else {
            //文本超出一行时
            //计算省略符宽度 绘制文字的时预留省略符位置
            val ellipsiswid = paint.measureText("\u2026")
            end = start + paint.breakText(
                text,
                start,
                end,
                true,
                layoutRight.toFloat() - x - ellipsiswid,
                null
            )
            canvas.drawText(text, start, end, x, y.toFloat(), paint)
            canvas.drawText("\u2026", x + paint.measureText(text, start, end), y.toFloat(), paint)
        }

    }


}
