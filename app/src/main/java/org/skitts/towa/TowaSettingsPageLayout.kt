package org.skitts.towa

import android.content.Context
import android.widget.LinearLayout

class TowaSettingsPageLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_app_settings, this)
    }

    fun setTheme() {
        val pageCont        = findViewById<LinearLayout>(R.id.settings_page_container)

        pageCont.setBackgroundColor(ThemeManager.colLight)
    }
}