package org.skitts.towa

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

class ThemeManager {
    companion object{
        suspend fun loadThemeForSession(context: Context) {
            val dbHelper = TowaDatabaseHelper(context)
            val themeName = dbHelper.readPreferenceSynchronous(PreferencesKeys.THEME, "")

            if (themeName.isEmpty()) {
                dbHelper.writePreference(PreferencesKeys.THEME, "matcha")
            }

            loadThemeColors(context, "tsuki")
//            loadThemeColors(context, themeName)
        }

        private fun loadThemeColors(context: Context, themeName: String) {
            when (themeName) {
                "matcha" -> {
                    colLight         = ContextCompat.getColor(context, R.color.matcha_light)
                    colDark          = ContextCompat.getColor(context, R.color.matcha_dark)
                    colAccentLight   = ContextCompat.getColor(context, R.color.matcha_accent_light)
                    colAccentMed     = ContextCompat.getColor(context, R.color.matcha_accent_med)
                    colAccentDark    = ContextCompat.getColor(context, R.color.matcha_accent_dark)
                    colTextPrimary   = ContextCompat.getColor(context, R.color.matcha_text_primary)
                    colTextSecondary = ContextCompat.getColor(context, R.color.matcha_text_secondary)
                    colTextDisabled  = ContextCompat.getColor(context, R.color.matcha_text_disabled)
                    colTaskbar       = ContextCompat.getColor(context, R.color.matcha_taskbar)
                    appTheme         = R.style.Theme_Towa_Matcha
                    overlayTheme     = R.style.Theme_Towa_Overlay_Matcha
                }
                "hinomaru" -> {
                    colLight         = ContextCompat.getColor(context, R.color.hinomaru_light)
                    colDark          = ContextCompat.getColor(context, R.color.hinomaru_dark)
                    colAccentLight   = ContextCompat.getColor(context, R.color.hinomaru_accent_light)
                    colAccentMed     = ContextCompat.getColor(context, R.color.hinomaru_accent_med)
                    colAccentDark    = ContextCompat.getColor(context, R.color.hinomaru_accent_dark)
                    colTextPrimary   = ContextCompat.getColor(context, R.color.hinomaru_text_primary)
                    colTextSecondary = ContextCompat.getColor(context, R.color.hinomaru_text_secondary)
                    colTextDisabled  = ContextCompat.getColor(context, R.color.hinomaru_text_disabled)
                    colTaskbar       = ContextCompat.getColor(context, R.color.hinomaru_taskbar)
                    appTheme         = R.style.Theme_Towa_Hinomaru
                    overlayTheme     = R.style.Theme_Towa_Overlay_Hinomaru
                }
                "tsuki" -> {
                    colLight         = ContextCompat.getColor(context, R.color.tsuki_light)
                    colDark          = ContextCompat.getColor(context, R.color.tsuki_dark)
                    colAccentLight   = ContextCompat.getColor(context, R.color.tsuki_accent_light)
                    colAccentMed     = ContextCompat.getColor(context, R.color.tsuki_accent_med)
                    colAccentDark    = ContextCompat.getColor(context, R.color.tsuki_accent_dark)
                    colTextPrimary   = ContextCompat.getColor(context, R.color.tsuki_text_primary)
                    colTextSecondary = ContextCompat.getColor(context, R.color.tsuki_text_secondary)
                    colTextDisabled  = ContextCompat.getColor(context, R.color.tsuki_text_disabled)
                    colTaskbar       = ContextCompat.getColor(context, R.color.tsuki_taskbar)
                    appTheme         = R.style.Theme_Towa_Tsuki
                    overlayTheme     = R.style.Theme_Towa_Overlay_Tsuki
                }
                else -> {
                    colLight         = ContextCompat.getColor(context, R.color.matcha_light)
                    colDark          = ContextCompat.getColor(context, R.color.matcha_dark)
                    colAccentLight   = ContextCompat.getColor(context, R.color.matcha_accent_light)
                    colAccentMed     = ContextCompat.getColor(context, R.color.matcha_accent_med)
                    colAccentDark    = ContextCompat.getColor(context, R.color.matcha_accent_dark)
                    colTextPrimary   = ContextCompat.getColor(context, R.color.matcha_text_primary)
                    colTextSecondary = ContextCompat.getColor(context, R.color.matcha_text_secondary)
                    colTextDisabled  = ContextCompat.getColor(context, R.color.matcha_text_disabled)
                    colTaskbar       = ContextCompat.getColor(context, R.color.matcha_taskbar)
                    appTheme         = R.style.Theme_Towa_Matcha
                    overlayTheme     = R.style.Theme_Towa_Overlay_Matcha
                }
            }
        }

        var colLight:         Int = Color.parseColor("#F3E9D2")
            private set
        var colDark:          Int = Color.parseColor("#114B5F")
            private set
        var colAccentLight:   Int = Color.parseColor("#C6DABF")
            private set
        var colAccentMed:     Int = Color.parseColor("#88D498")
            private set
        var colAccentDark:    Int = Color.parseColor("#1A936F")
            private set
        var colTextPrimary:   Int = Color.parseColor("#FF0000")
            private set
        var colTextSecondary: Int = Color.parseColor("#994C585C")
            private set
        var colTextDisabled:  Int = Color.parseColor("#654C585C")
            private set
        var colTaskbar:       Int = Color.parseColor("#114B5F")
            private set
        var appTheme:         Int = R.style.Theme_Towa_Matcha
            private set
        var overlayTheme:     Int = R.style.Theme_Towa_Overlay_Matcha
            private set
    }

}