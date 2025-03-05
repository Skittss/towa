package org.skitts.towa

import android.content.Context
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView

class TowaSearchPageLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_app_search, this)
    }

    fun setTheme() {
        val pageCont        = findViewById<LinearLayout>(R.id.search_page_container)
        //val search          = findViewById<SearchView>(R.id.search_page_search)

        pageCont.setBackgroundColor(ThemeManager.colLight)

        //val searchText  = search.findViewById<EditText>(/* id = */ com.google.android.material.R.id.search_button)
        //val searchIcon  = search.findViewById<ImageView>()
        //val searchClose =
    }
}