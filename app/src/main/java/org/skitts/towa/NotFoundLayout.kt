package org.skitts.towa

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView

class NotFoundLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_not_found, this)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
    }

    fun populate(header: String, footer: String, kaomoji: String) {
        val kaomojiCont = findViewById<TextView>(R.id.not_found_kaomoji)
        val headerCont  = findViewById<TextView>(R.id.not_found_header)
        val footerCont  = findViewById<TextView>(R.id.not_found_footer)

        kaomojiCont.text = kaomoji
        kaomojiCont.setTextColor(ThemeManager.colTextDisabled)
        headerCont.text  = header
        headerCont.setTextColor(ThemeManager.colTextPrimary)
        footerCont.text  = footer
        footerCont.setTextColor(ThemeManager.colTextSecondary)
    }

}