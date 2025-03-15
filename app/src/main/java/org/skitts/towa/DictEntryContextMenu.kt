package org.skitts.towa

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch

class DictEntryContextMenu(
    private val context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry_context_menu, this)
    }

    fun populate(
        activity: ComponentActivity,
        frame: FrameLayout,
        entry: DictEntry,
        defIdx: Int = -1
    ) {
        val overlayView     = findViewById<LinearLayout>(R.id.translucent_overlay)
        val menuContainer   = findViewById<LinearLayout>(R.id.menu_container)
        val menuItemList    = findViewById<LinearLayout>(R.id.menu_item_list)
        val addDefsDivisor  = findViewById<View>(R.id.add_defs_inbetween_divisor)
        val entryNameView   = findViewById<TextView>(R.id.entry_name)
        val allDefsButton   = findViewById<TextView>(R.id.add_all_defs_button)
        val singleDefButton = findViewById<TextView>(R.id.add_def_num_button)
        val cancelButton    = findViewById<TextView>(R.id.cancel_button)

        entryNameView.text = context.getString(R.string.context_menu_entry).replace("{}", entry.primaryForm)

        menuItemList.setBackgroundColor(ThemeManager.colLight)
        allDefsButton.setTextColor(ThemeManager.colTextPrimary)
        singleDefButton.setTextColor(ThemeManager.colTextPrimary)
        cancelButton.setTextColor(ThemeManager.colAccentMed)

        allDefsButton.setOnClickListener {
            addAnkiCard(activity, frame, menuContainer, entry, -1)
        }

        if (defIdx >= 0) {
            singleDefButton.text = context.getString(R.string.context_menu_add_single).replace("{}", (defIdx+1).toString())
            singleDefButton.setOnClickListener {
                addAnkiCard(activity, frame, menuContainer, entry, defIdx)
            }
        } else {
            singleDefButton.visibility = GONE
            addDefsDivisor.visibility = GONE
        }

        overlayView.setOnClickListener { close(frame) }
        cancelButton.setOnClickListener { close(frame) }
    }

    private fun addAnkiCard(
        activity: ComponentActivity,
        frame: FrameLayout,
        layout: ViewGroup,
        entry: DictEntry,
        defIdx: Int
    ) {
        setMenuButtonsClickable(false)

        val loadingBar = ProgressBar(context)
        loadingBar.scaleX = 0.65f
        loadingBar.scaleY = 0.65f
        loadingBar.setPadding(0,0,0,20)
        layout.addView(loadingBar, 0)

        val loadingText = TextView(context)
        loadingText.text = context.getString(R.string.context_menu_adding)
        loadingText.gravity = CENTER_HORIZONTAL
        loadingText.setPadding(0, 20, 0, 10)
        loadingText.setTextColor(ThemeManager.colTextPrimary)
        layout.addView(loadingText, 0)

        activity.lifecycle.coroutineScope.launch {
            val ankiHelper = AnkiHelper(context, activity)
            val added: Boolean = ankiHelper.add(entry, defIdx)

            layout.removeView(loadingText)
            layout.removeView(loadingBar)
            setMenuButtonsClickable(true)

            if (added) {
                close(frame)
                Toast.makeText(context, context.getString(R.string.context_menu_added), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context.getString(R.string.context_menu_not_added), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setMenuButtonsClickable(value: Boolean) {
        val allDefsButton       = findViewById<TextView>(R.id.add_all_defs_button)
        val singleDefButton     = findViewById<TextView>(R.id.add_def_num_button)
        allDefsButton.isClickable   = value
        singleDefButton.isClickable = value
    }

    fun open(frame: FrameLayout) {
        // TODO: This doesn't get the frame's height correctly so can't use MATCH_PARENT height.
        //       No fixes i've tried seem to work. PITA.
        val menuContainer   = findViewById<LinearLayout>(R.id.menu_container)
        menuContainer.measure(
            MeasureSpec.makeMeasureSpec(frame.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(frame.height, MeasureSpec.EXACTLY)
        )

        val animHeight: Float = menuContainer.measuredHeight.toFloat()
        menuContainer.translationY = animHeight
        menuContainer.animate().translationY(0.0f)
    }

    fun close(frame: FrameLayout) {
        val overlayView     = findViewById<LinearLayout>(R.id.translucent_overlay)
        val menuContainer   = findViewById<LinearLayout>(R.id.menu_container)
        overlayView.visibility = GONE
        menuContainer.animate().translationY(menuContainer.height.toFloat()).withEndAction { frame.removeView(this) }
    }
}