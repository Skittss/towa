package org.skitts.towa

import android.content.Context
import android.util.AttributeSet

class DropdownTextView (
    context: Context,
    attrs: AttributeSet?,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs) {
    init {
        setBackgroundColor(ThemeManager.colLight)
        setTextColor(ThemeManager.colTextPrimary)
    }
}