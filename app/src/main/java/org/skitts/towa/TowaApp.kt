package org.skitts.towa

import android.content.Context
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {

    private var bottomMenuIdx = 0
    private var bottomMenuView: BottomNavigationView? = null

    inner class TowaAppSwipeListener(
        private val context: Context
    ) : OnSwipeListener(context) {

        private val bottomNavItems: Array<Int> = arrayOf(
            R.id.bottom_menu_home,
            R.id.bottom_menu_settings,
            R.id.bottom_menu_about)

        override fun onSwipeLeft() {
            bottomMenuIdx = min(bottomNavItems.size, bottomMenuIdx + 1)
            bottomMenuView!!.selectedItemId = bottomNavItems[bottomMenuIdx]
        }

        override fun onSwipeRight() {
            bottomMenuIdx = max(0, bottomMenuIdx - 1)
            bottomMenuView!!.selectedItemId = bottomNavItems[bottomMenuIdx]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.towa_app)

        bottomMenuView = findViewById(R.id.towa_bottom_nav)

        val frame: FrameLayout = findViewById(R.id.towa_app_frame)
        frame.setOnTouchListener(TowaAppSwipeListener(this))
    }
}

