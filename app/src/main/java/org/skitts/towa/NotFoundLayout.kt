package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Locale

class NotFoundLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_not_found, this)
    }

    public fun populate(header: String, footer: String) {
        val headerCont = findViewById<TextView>(R.id.not_found_header)
        val footerCont = findViewById<TextView>(R.id.not_found_footer)

        headerCont.text = header
        footerCont.text = footer
    }

}