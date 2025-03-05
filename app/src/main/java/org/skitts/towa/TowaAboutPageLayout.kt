package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

class TowaAboutPageLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_app_about, this)
    }

    fun setTheme() {
        val pageCont        = findViewById<LinearLayout>(R.id.about_page_container)
        val aboutTitle      = findViewById<TextView>(R.id.about_page_about_title)
        val aboutUnder      = findViewById<View>(R.id.about_about_underline)
        val about           = findViewById<TextView>(R.id.about_page_about)
        val sourcesTitle    = findViewById<TextView>(R.id.about_page_sources_title)
        val sourcesUnder    = findViewById<View>(R.id.about_sources_underline)
        val sources         = findViewById<TextView>(R.id.about_page_sources_preamble)
        val wordTitle       = findViewById<TextView>(R.id.about_word_subheading)
        val word            = findViewById<TextView>(R.id.about_word)
        val furiganaTitle   = findViewById<TextView>(R.id.about_furigana_subheading)
        val furigana        = findViewById<TextView>(R.id.about_furigana)
        val audioTitle      = findViewById<TextView>(R.id.about_audio_subheading)
        val audio           = findViewById<TextView>(R.id.about_audio)
        val jlptTitle       = findViewById<TextView>(R.id.about_jlpt_subheading)
        val jlpt            = findViewById<TextView>(R.id.about_jlpt)

        pageCont.setBackgroundColor(ThemeManager.colLight)
        aboutTitle.setTextColor(ThemeManager.colTextPrimary)
        aboutTitle.setTypeface(aboutTitle.typeface, Typeface.BOLD)
        aboutUnder.setBackgroundColor(ThemeManager.colTextPrimary)
        about.setTextColor((ThemeManager.colTextPrimary))

        sourcesTitle.setTextColor(ThemeManager.colTextPrimary)
        sourcesTitle.setTypeface(sourcesTitle.typeface, Typeface.BOLD)
        sourcesUnder.setBackgroundColor(ThemeManager.colTextPrimary)
        sources.setTextColor(ThemeManager.colTextPrimary)

        wordTitle.setTextColor(ThemeManager.colTextPrimary)
        wordTitle.setTypeface(wordTitle.typeface, Typeface.BOLD)
        word.setTextColor(ThemeManager.colTextPrimary)
        word.movementMethod = LinkMovementMethod.getInstance()

        furiganaTitle.setTextColor(ThemeManager.colTextPrimary)
        furiganaTitle.setTypeface(furiganaTitle.typeface, Typeface.BOLD)
        furigana.setTextColor(ThemeManager.colTextPrimary)
        furigana.movementMethod = LinkMovementMethod.getInstance()

        audioTitle.setTextColor(ThemeManager.colTextPrimary)
        audioTitle.setTypeface(audioTitle.typeface, Typeface.BOLD)
        audio.setTextColor(ThemeManager.colTextPrimary)
        audio.movementMethod = LinkMovementMethod.getInstance()

        jlptTitle.setTextColor(ThemeManager.colTextPrimary)
        jlptTitle.setTypeface(jlptTitle.typeface, Typeface.BOLD)
        jlpt.setTextColor(ThemeManager.colTextPrimary)
        jlpt.movementMethod = LinkMovementMethod.getInstance()

    }
}
