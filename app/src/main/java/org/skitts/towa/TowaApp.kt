package org.skitts.towa

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.coroutineScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.prefs.Preferences
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private var bottomMenuIdx = 0
    private var bottomMenuView: BottomNavigationView? = null
    private val bottomNavItems: Array<Int> = arrayOf(
        R.id.bottom_menu_search,
        R.id.bottom_menu_settings,
        R.id.bottom_menu_about)
    private var currentNavView: View? = null

    private var searchPage: TowaSearchPageLayout? = null
    private var settingsPage: TowaSettingsPageLayout? = null
    private var aboutPage: TowaAboutPageLayout? = null

    inner class TowaAppSwipeListener(
        private val context: Context
    ) : OnSwipeListener(context) {

        override fun onSwipeLeft() {
            bottomMenuIdx = min(bottomNavItems.size, bottomMenuIdx + 1)
            setSelectedNavView(bottomMenuIdx)
        }

        override fun onSwipeRight() {
            bottomMenuIdx = max(0, bottomMenuIdx - 1)
            setSelectedNavView(bottomMenuIdx)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.coroutineScope.launch {
            // TODO: Would be nice to avoid waiting for this somehow
            PreferencesManager.loadPreferencesForSession(this@MainActivity)
            ThemeManager.loadThemeForSession(this@MainActivity)
            ThemeManager.updateTheme(this@MainActivity)
            initPages()
            showApp()
            updateTheme(false)
        }
    }

    private fun updateTheme(recreatePages: Boolean = true) {
        bottomMenuView = findViewById(R.id.towa_bottom_nav)!!

        val activeCols = getBottomNavColorStateList()
        bottomMenuView!!.setBackgroundColor(ThemeManager.colTaskbar)
        bottomMenuView!!.itemTextColor = activeCols
        bottomMenuView!!.itemIconTintList = activeCols

        val rippleCols = getBottomNavRippleColorStateList()
        bottomMenuView!!.itemRippleColor = rippleCols

        if (recreatePages) initPages()
        showNavView(bottomNavItems[bottomMenuIdx])
    }

    private fun getBottomNavRippleColorStateList(): ColorStateList {
        val states = arrayOf(intArrayOf(android.R.attr.state_enabled))

        val refCol = ThemeManager.colAccentMed
        val transparentCol = Color.argb(
            35,
            Color.red(refCol), Color.green(refCol), Color.blue(refCol))

        val colors = intArrayOf(transparentCol)
        return ColorStateList(states, colors)
    }

    private fun getBottomNavColorStateList(): ColorStateList {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_pressed),
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_pressed),
        )
        val colors = intArrayOf(
            ThemeManager.colTaskbarEnabled,
            ThemeManager.colTaskbarEnabled,
            ThemeManager.colTaskbarDisabled,
            ThemeManager.colTaskbarDisabled
        )

        return ColorStateList(states, colors)
    }

    private fun initPages() {
        createSearchPage()
        createSettingsPage()
        createAboutPage()
    }

    private fun createSearchPage() {
        searchPage = TowaSearchPageLayout(this@MainActivity)
        searchPage!!.setupView(this@MainActivity)
        searchPage!!.setTheme()
        searchPage!!.setOnTouchListener(TowaAppSwipeListener(this))
    }

    private fun createSettingsPage() {
        settingsPage = TowaSettingsPageLayout.create(this, this)
        settingsPage!!.onChangeTheme = { updateTheme(); true }
        settingsPage!!.setTheme()
        settingsPage!!.setOnTouchListener(TowaAppSwipeListener(this))
    }

    private fun createAboutPage() {
        aboutPage = TowaAboutPageLayout(this)
        aboutPage!!.setTheme()
        aboutPage!!.setOnTouchListener(TowaAppSwipeListener(this))
    }

    private fun showApp() {
        setContentView(R.layout.towa_app)

        bottomMenuView = findViewById(R.id.towa_bottom_nav)
        bottomMenuView!!.setOnItemSelectedListener { item -> showNavView(item.itemId) }
        bottomMenuView!!.setOnTouchListener(TowaAppSwipeListener(this))

        setSelectedNavView(0)
    }

    private fun showNavView(idx: Int): Boolean {
        var ret = true

        val frame: FrameLayout = findViewById(R.id.towa_app_frame)
        if (currentNavView != null) frame.removeView(currentNavView)

        when (idx) {
            R.id.bottom_menu_search -> {
                currentNavView = searchPage
                bottomMenuIdx = 0
            }
            R.id.bottom_menu_settings -> {
                createSettingsPage()
                currentNavView = settingsPage
                bottomMenuIdx = 1
            }
            R.id.bottom_menu_about -> {
                createAboutPage()
                currentNavView = aboutPage
                bottomMenuIdx = 2
            }
            else -> {
                ret = false
            }
        }

        if (currentNavView != null) {
            frame.addView(currentNavView)
        }

        return ret
    }

    private fun setSelectedNavView(idx: Int = -1) {
        if (idx < 0 || idx >= bottomNavItems.size) return
        bottomMenuView!!.selectedItemId = bottomNavItems[bottomMenuIdx]
    }
}

