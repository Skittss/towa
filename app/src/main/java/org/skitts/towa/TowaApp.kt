package org.skitts.towa

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.lifecycle.coroutineScope
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
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
            ThemeManager.loadThemeForSession(this@MainActivity)
            updateWindowBarThemes()
            setTheme(ThemeManager.appTheme)
            initPages()
            showApp()
            updateTheme()
        }
    }

    private fun updateWindowBarThemes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
            window.decorView.setOnApplyWindowInsetsListener { view, insets ->
                view.setBackgroundColor(ThemeManager.colTaskbar)
                insets
            }
        } else {
            // For Android 14 and below
            window.statusBarColor = ThemeManager.colTaskbar
        }
    }

    private fun updateTheme() {
        bottomMenuView = findViewById(R.id.towa_bottom_nav)!!

        val activeCols = getBottomNavColorStateList()
        bottomMenuView!!.setBackgroundColor(ThemeManager.colTaskbar)
        bottomMenuView!!.itemTextColor = activeCols
        bottomMenuView!!.itemIconTintList = activeCols

        val rippleCols = getBottomNavRippleColorStateList()
        bottomMenuView!!.itemRippleColor = rippleCols
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
            ThemeManager.colTextPrimary,
            ThemeManager.colAccentLight,
            ThemeManager.colTextDisabled,
            ThemeManager.colTextDisabled
        )

        return ColorStateList(states, colors)
    }

    private fun initPages() {
        searchPage = TowaSearchPageLayout(this@MainActivity)
        searchPage!!.setupView(this@MainActivity)
        searchPage!!.setTheme()
    }

    private fun showApp() {
        setContentView(R.layout.towa_app)

        bottomMenuView = findViewById(R.id.towa_bottom_nav)
        bottomMenuView!!.setOnItemSelectedListener { item ->
            var ret = true

            val frame: FrameLayout = findViewById(R.id.towa_app_frame)
            if (currentNavView != null) frame.removeView(currentNavView)

            when (item.itemId) {
                R.id.bottom_menu_search -> {
                    currentNavView = searchPage
                    bottomMenuIdx = 0
                }
                R.id.bottom_menu_settings -> {
                    val page = TowaSettingsPageLayout(this)
                    page.setTheme()
                    currentNavView = page
                    bottomMenuIdx = 1
                }
                R.id.bottom_menu_about -> {
                    val page = TowaAboutPageLayout(this)
                    page.setTheme()
                    currentNavView = page
                    bottomMenuIdx = 2
                }
                else -> {
                    ret = false
                }
            }

            if (currentNavView != null) {
                frame.addView(currentNavView)
            }

            ret
        }

        val frame: FrameLayout = findViewById(R.id.towa_app_frame)
        frame.setOnTouchListener(TowaAppSwipeListener(this))

        setSelectedNavView(0)
    }

    private fun setSelectedNavView(idx: Int = -1) {
        if (idx < 0 || idx >= bottomNavItems.size) return
        bottomMenuView!!.selectedItemId = bottomNavItems[bottomMenuIdx]
    }
}

