package org.skitts.towa

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.activity.ComponentActivity

class TowaSearchPageLayout (
    context: Context,
) : LinearLayout(context) {
    private var resultsQueryStr: String? = null
    private var resultsView: TowaSearchResultsLayout? = null

    init {
        inflate(context, R.layout.towa_app_search, this)
    }

    fun setupView(activity: ComponentActivity) {
        val search = findViewById<SearchView>(R.id.search_page_search)
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val queryStr = query ?: ""

                showSearchResults(activity, queryStr)
                search.clearFocus()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        var searchCloseButtonId: Int = search.context.resources
            .getIdentifier("android:id/search_close_btn", null, null)
        val closeButton = search.findViewById<ImageView>(searchCloseButtonId)
        closeButton.setOnClickListener {
            search.setQuery("", false)
            showSearchResults(activity, "")
            search.clearFocus()
        }

        val searchIconId: Int = search.context.resources
            .getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = search.findViewById<ImageView>(searchIconId)
        searchIcon.setOnClickListener {
            val queryString = search.query.toString()
            showSearchResults(activity, queryString)
            search.clearFocus()
        }
    }

    fun setTheme() {
        val pageCont = findViewById<LinearLayout>(R.id.search_page_container)
        pageCont.setBackgroundColor(ThemeManager.colLight)

        val search = findViewById<SearchView>(R.id.search_page_search)
        val searchCloseButtonId: Int = search.context.resources
            .getIdentifier("android:id/search_close_btn", null, null)
        val closeButton = search.findViewById<ImageView>(searchCloseButtonId)
        closeButton.setColorFilter(ThemeManager.colAccentMed)

        val searchIconId: Int = search.context.resources
            .getIdentifier("android:id/search_mag_icon", null, null)
        val searchIcon = search.findViewById<ImageView>(searchIconId)
        searchIcon.setColorFilter(ThemeManager.colAccentMed)

        val backplateID: Int = search.context.resources
            .getIdentifier("android:id/search_plate", null, null)
        val backplate = search.findViewById<View>(backplateID)
        backplate.setBackgroundColor(Color.TRANSPARENT)

        val underline = findViewById<View>(R.id.search_page_search_underline)
        underline.setBackgroundColor(ThemeManager.colAccentMed)
    }

    fun showSearchResults(activity: ComponentActivity, query: String) {
        if (query == resultsQueryStr) return

        val resultsCont = findViewById<LinearLayout>(R.id.search_results_cont)
        if (resultsView != null) {
            resultsView!!.destroy()
            resultsCont.removeView(resultsView)
        }

        if (query == "") return

        resultsQueryStr = sanitizeInput(query)
        resultsView = TowaSearchResultsLayout.create(context, activity, query)

        resultsCont.addView(resultsView)
    }
}