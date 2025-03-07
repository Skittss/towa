package org.skitts.towa

import android.content.Context
import android.view.Gravity
import android.view.Gravity.CENTER
import android.view.Gravity.CENTER_HORIZONTAL
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class TowaSearchResultsLoadingLayout(
    context: Context,
) : LinearLayout(context) {
    init {
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        orientation = VERTICAL
        gravity = CENTER_HORIZONTAL

        val loadingBar = ProgressBar(context)
        loadingBar.setPadding(0, 80, 0, 40)
        addView(loadingBar)

        val loadingText = TextView(context)
        loadingText.text = "検索中～。。。"
        loadingText.setTextColor(ThemeManager.colTextPrimary)
        loadingText.gravity = Gravity.CENTER_HORIZONTAL
        loadingText.setPadding(0, 0, 0, 80)
        addView(loadingText)
    }
}