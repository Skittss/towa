package org.skitts.towa

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class DictateButtonLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.dictate_icon_image_layout, this)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    fun setup(width: Int, height: Int, color: Int) {
        val icon = findViewById<ImageView>(R.id.dictate_icon)
        icon.setColorFilter(color)

        icon.layoutParams.width = width
        icon.layoutParams.height = height
    }
}