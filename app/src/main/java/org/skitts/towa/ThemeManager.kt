package org.skitts.towa

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.KeyEventDispatcher.Component

class ThemeManager {
    companion object{

        val validThemes: HashSet<String> = hashSetOf(
            "matcha",
            "hinomaru",
            "tsuki"
        )

        suspend fun loadThemeForSession(context: Context) {
            val dbHelper = TowaDatabaseHelper(context)
            themeName = dbHelper.readPreferenceSynchronous(PreferencesKeys.THEME, "")

            if (themeName.isEmpty()) {
                themeName = "matcha"
                dbHelper.writePreference(PreferencesKeys.THEME, themeName)
            }

            loadThemeColors(context, themeName)
        }

        suspend fun setThemeForSession(context: Context, theme: String) {
            val sanitizedTheme = theme.lowercase()
            if (!validThemes.contains(sanitizedTheme)) return

            themeName = sanitizedTheme

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.THEME, themeName)
            loadThemeColors(context, themeName)
        }

        fun updateTheme(activity: ComponentActivity) {
            activity.setTheme(appTheme)
            updateWindowBarThemes(activity, true)
        }

        private fun updateWindowBarThemes(
            activity: ComponentActivity,
            forceRefresh: Boolean = false
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
                activity.window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                    view.setBackgroundColor(colStatusBar)
                    insets
                }
            } else {
                // For Android 14 and below
                activity.window.statusBarColor = colStatusBar
            }

            if (forceRefresh) {
                activity.window.decorView.dispatchApplyWindowInsets(
                    activity.window.decorView.rootWindowInsets
                )
            }
        }

        private fun loadThemeColors(context: Context, themeName: String) {
            when (themeName) {
                "matcha" -> {
                    colLight           = ContextCompat.getColor(context, R.color.matcha_light)
                    colDark            = ContextCompat.getColor(context, R.color.matcha_dark)
                    colAccentLight     = ContextCompat.getColor(context, R.color.matcha_accent_light)
                    colAccentMed       = ContextCompat.getColor(context, R.color.matcha_accent_med)
                    colAccentDark      = ContextCompat.getColor(context, R.color.matcha_accent_dark)
                    colTextPrimary     = ContextCompat.getColor(context, R.color.matcha_text_primary)
                    colTextSecondary   = ContextCompat.getColor(context, R.color.matcha_text_secondary)
                    colTextDisabled    = ContextCompat.getColor(context, R.color.matcha_text_disabled)
                    colTaskbar         = ContextCompat.getColor(context, R.color.matcha_taskbar)
                    colTaskbar         = ContextCompat.getColor(context, R.color.matcha_taskbar)
                    colTaskbarEnabled  = ContextCompat.getColor(context, R.color.matcha_taskbar_enabled)
                    colTaskbarDisabled = ContextCompat.getColor(context, R.color.matcha_taskbar_disabled)
                    colStatusBar       = ContextCompat.getColor(context, R.color.matcha_statusbar)
                    appTheme           = R.style.Theme_Towa_Matcha
                    overlayTheme       = R.style.Theme_Towa_Overlay_Matcha
                }
                "hinomaru" -> {
                    colLight           = ContextCompat.getColor(context, R.color.hinomaru_light)
                    colDark            = ContextCompat.getColor(context, R.color.hinomaru_dark)
                    colAccentLight     = ContextCompat.getColor(context, R.color.hinomaru_accent_light)
                    colAccentMed       = ContextCompat.getColor(context, R.color.hinomaru_accent_med)
                    colAccentDark      = ContextCompat.getColor(context, R.color.hinomaru_accent_dark)
                    colTextPrimary     = ContextCompat.getColor(context, R.color.hinomaru_text_primary)
                    colTextSecondary   = ContextCompat.getColor(context, R.color.hinomaru_text_secondary)
                    colTextDisabled    = ContextCompat.getColor(context, R.color.hinomaru_text_disabled)
                    colTaskbar         = ContextCompat.getColor(context, R.color.hinomaru_taskbar)
                    colTaskbarEnabled  = ContextCompat.getColor(context, R.color.hinomaru_taskbar_enabled)
                    colTaskbarDisabled = ContextCompat.getColor(context, R.color.hinomaru_taskbar_disabled)
                    colStatusBar       = ContextCompat.getColor(context, R.color.hinomaru_statusbar)
                    appTheme           = R.style.Theme_Towa_Hinomaru
                    overlayTheme       = R.style.Theme_Towa_Overlay_Hinomaru
                }
                "tsuki" -> {
                    colLight           = ContextCompat.getColor(context, R.color.tsuki_light)
                    colDark            = ContextCompat.getColor(context, R.color.tsuki_dark)
                    colAccentLight     = ContextCompat.getColor(context, R.color.tsuki_accent_light)
                    colAccentMed       = ContextCompat.getColor(context, R.color.tsuki_accent_med)
                    colAccentDark      = ContextCompat.getColor(context, R.color.tsuki_accent_dark)
                    colTextPrimary     = ContextCompat.getColor(context, R.color.tsuki_text_primary)
                    colTextSecondary   = ContextCompat.getColor(context, R.color.tsuki_text_secondary)
                    colTextDisabled    = ContextCompat.getColor(context, R.color.tsuki_text_disabled)
                    colTaskbar         = ContextCompat.getColor(context, R.color.tsuki_taskbar)
                    colTaskbarEnabled  = ContextCompat.getColor(context, R.color.tsuki_taskbar_enabled)
                    colTaskbarDisabled = ContextCompat.getColor(context, R.color.tsuki_taskbar_disabled)
                    colStatusBar       = ContextCompat.getColor(context, R.color.tsuki_statusbar)
                    appTheme           = R.style.Theme_Towa_Tsuki
                    overlayTheme       = R.style.Theme_Towa_Overlay_Tsuki
                }
                else -> {
                    colLight           = ContextCompat.getColor(context, R.color.matcha_light)
                    colDark            = ContextCompat.getColor(context, R.color.matcha_dark)
                    colAccentLight     = ContextCompat.getColor(context, R.color.matcha_accent_light)
                    colAccentMed       = ContextCompat.getColor(context, R.color.matcha_accent_med)
                    colAccentDark      = ContextCompat.getColor(context, R.color.matcha_accent_dark)
                    colTextPrimary     = ContextCompat.getColor(context, R.color.matcha_text_primary)
                    colTextSecondary   = ContextCompat.getColor(context, R.color.matcha_text_secondary)
                    colTextDisabled    = ContextCompat.getColor(context, R.color.matcha_text_disabled)
                    colTaskbar         = ContextCompat.getColor(context, R.color.matcha_taskbar)
                    colTaskbarEnabled  = ContextCompat.getColor(context, R.color.matcha_taskbar_enabled)
                    colTaskbarDisabled = ContextCompat.getColor(context, R.color.matcha_taskbar_disabled)
                    colStatusBar       = ContextCompat.getColor(context, R.color.matcha_statusbar)
                    appTheme           = R.style.Theme_Towa_Matcha
                    overlayTheme       = R.style.Theme_Towa_Overlay_Matcha
                }
            }
        }

        var themeName:          String = ""
            private set
        var colLight:           Int = Color.parseColor("#F3E9D2")
            private set
        var colDark:            Int = Color.parseColor("#114B5F")
            private set
        var colAccentLight:     Int = Color.parseColor("#C6DABF")
            private set
        var colAccentMed:       Int = Color.parseColor("#88D498")
            private set
        var colAccentDark:      Int = Color.parseColor("#1A936F")
            private set
        var colTextPrimary:     Int = Color.parseColor("#FF0000")
            private set
        var colTextSecondary:   Int = Color.parseColor("#994C585C")
            private set
        var colTextDisabled:    Int = Color.parseColor("#654C585C")
            private set
        var colTaskbar:         Int = Color.parseColor("#114B5F")
            private set
        var colTaskbarEnabled:  Int = Color.parseColor("#FFF9F7")
            private set
        var colTaskbarDisabled: Int = Color.parseColor("#8A000000")
            private set
        var colStatusBar:       Int = Color.parseColor("#114B5F")
            private set
        var appTheme:           Int = R.style.Theme_Towa_Matcha
            private set
        var overlayTheme:       Int = R.style.Theme_Towa_Overlay_Matcha
            private set
    }

}