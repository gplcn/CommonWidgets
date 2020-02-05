package com.common.widgets

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import com.common.text.style.EllipsizeLineSpan
import java.util.regex.Pattern

/**
 * 如果每个换行符前的文本超出控件一行的长度自动截断,行末显示省略号"..."
 */
class EllipsizeEveryLineTextView : AppCompatTextView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    fun setLineEllipsizeText(text: String) {
        val ssb = SpannableStringBuilder(text)
        val pattern = Pattern.compile(".*\\n", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(ssb)
        var start = 0
        while (matcher.find()) {
            ssb.setSpan(
                EllipsizeLineSpan(),
                start,
                matcher.end(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            start = matcher.end()
        }
        ssb.setSpan(
            EllipsizeLineSpan(),
            start,
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        setText(ssb, TextView.BufferType.SPANNABLE)
    }


    @Deprecated("")
    override fun setText(text: CharSequence, type: TextView.BufferType) {
        super.setText(text, type)
    }


}
