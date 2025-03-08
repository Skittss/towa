package org.skitts.towa

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.coroutineScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Locale

class TowaSettingsPageLayout private constructor(
    context: Context,
) : LinearLayout(context) {

    var onChangeTheme: (() -> Boolean)? = null
    var onChangeLanguagePreference: (() -> Boolean)? = null
    var onChangeLocalization: (() -> Boolean)? = null
    var onChangeAudioSourcePreference: (() -> Boolean)? = null

    companion object {
        fun create(
            context: Context,
            activity: ComponentActivity,
        ): TowaSettingsPageLayout {
            return TowaSettingsPageLayout(context).apply {
                setupView(activity);
            }
        }
    }

    init {
        inflate(context, R.layout.towa_app_settings, this)

        val themeList = findViewById<TextInputLayout>(R.id.page_settings_theme_list)
        val themeListItems = listOf(
            resources.getString(R.string.theme_name_matcha),
            resources.getString(R.string.theme_name_hinomaru),
            resources.getString(R.string.theme_name_tsuki)
        )
        val themeAdapter = ArrayAdapter(context, R.layout.list_item, themeListItems)
        (themeList.editText as? AutoCompleteTextView)?.setAdapter(themeAdapter)

        val languageList = findViewById<TextInputLayout>(R.id.page_settings_language_list)
        val languageListItems = listOf(
            resources.getString(R.string.language_name_en),
            resources.getString(R.string.language_name_jp),
        )
        val languageAdapter = ArrayAdapter(context, R.layout.list_item, languageListItems)
        (languageList.editText as? AutoCompleteTextView)?.setAdapter(languageAdapter)

        val localizationList = findViewById<TextInputLayout>(R.id.page_settings_localization_list)
        val localizationListItems = listOf(
            resources.getString(R.string.language_name_en),
            resources.getString(R.string.language_name_jp),
        )
        val localizationAdapter = ArrayAdapter(context, R.layout.list_item, localizationListItems)
        (localizationList.editText as? AutoCompleteTextView)?.setAdapter(localizationAdapter)

        val audioSourceList = findViewById<TextInputLayout>(R.id.page_settings_audio_source_list)
        val audioSourceListItems = listOf(
            resources.getString(R.string.audio_source_tofugu),
            resources.getString(R.string.audio_source_kanji_alive),
        )
        val audioSourceAdapter = ArrayAdapter(context, R.layout.list_item, audioSourceListItems)
        (audioSourceList.editText as? AutoCompleteTextView)?.setAdapter(audioSourceAdapter)
    }

    fun setTheme() {
        val pageCont             = findViewById<LinearLayout>(R.id.settings_page_container)
        val personalizationTitle = findViewById<TextView>(R.id.page_settings_personalization_title)
        val personalizationUnder = findViewById<View>(R.id.page_settings_personalization_div)
        val languageTitle        = findViewById<TextView>(R.id.page_settings_language_title)
        val languageUnder        = findViewById<View>(R.id.page_settings_language_div)
        val ankiTitle            = findViewById<TextView>(R.id.page_settings_anki_title)
        val ankiUnder            = findViewById<View>(R.id.page_settings_anki_div)

        val themeMenu        = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_theme_menu)
        val languageMenu     = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_language_menu)
        val localizationMenu = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_localization_menu)
        val audioSourceMenu  = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_audio_source_menu)

        pageCont.setBackgroundColor(ThemeManager.colLight)

        personalizationTitle.setTextColor(ThemeManager.colTextPrimary)
        personalizationTitle.setTypeface(personalizationTitle.typeface, Typeface.BOLD)
        personalizationUnder.setBackgroundColor(ThemeManager.colTextPrimary)

        languageTitle.setTextColor(ThemeManager.colTextPrimary)
        languageTitle.setTypeface(languageTitle.typeface, Typeface.BOLD)
        languageUnder.setBackgroundColor(ThemeManager.colTextPrimary)

        ankiTitle.setTextColor(ThemeManager.colTextPrimary)
        ankiTitle.setTypeface(ankiTitle.typeface, Typeface.BOLD)
        ankiUnder.setBackgroundColor(ThemeManager.colTextPrimary)

        themeMenu.setDropDownBackgroundTint(ThemeManager.colLight)
        languageMenu.setDropDownBackgroundTint(ThemeManager.colLight)
        localizationMenu.setDropDownBackgroundTint(ThemeManager.colLight)
        audioSourceMenu.setDropDownBackgroundTint(ThemeManager.colLight)
    }

    private fun setupView(activity: ComponentActivity) {
        val themeMenu        = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_theme_menu)
        val languageMenu     = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_language_menu)
        val localizationMenu = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_localization_menu)
        val ankiDeckNameMenu = findViewById<TextInputEditText>(R.id.page_settings_anki_deck_name_menu)
        val audioSourceMenu  = findViewById<MaterialAutoCompleteTextView>(R.id.page_settings_audio_source_menu)

        val selectedThemeName = ThemeManager.themeName.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        themeMenu.setText(selectedThemeName, false)

        themeMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedTheme = parent.getItemAtPosition(position) as String
            activity.lifecycle.coroutineScope.launch {
                ThemeManager.setThemeForSession(context, selectedTheme)
                ThemeManager.updateTheme(activity)

                onChangeTheme?.invoke()
            }
        }

        val selectedLanguageName = PreferencesManager.preferredTranslationLanguage.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        languageMenu.setText(selectedLanguageName, false)

        languageMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedLanguage = parent.getItemAtPosition(position) as String
            activity.lifecycle.coroutineScope.launch {
                PreferencesManager.setPreferredTranslationLanguage(context, selectedLanguage)

                onChangeLanguagePreference?.invoke()
            }
        }

        val selectedLocalizationeName = PreferencesManager.localization.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        localizationMenu.setText(selectedLocalizationeName, false)

        localizationMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedLocalization = parent.getItemAtPosition(position) as String
            activity.lifecycle.coroutineScope.launch {
                PreferencesManager.setLocalization(context, selectedLocalization)

                onChangeLocalization?.invoke()
            }
        }

        ankiDeckNameMenu.setText(PreferencesManager.ankiDeckName)
        ankiDeckNameMenu.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                activity.lifecycle.coroutineScope.launch {
                    PreferencesManager.setAnkiDeckName(context, s.toString())
                }
            }
        })



        val selectedAudioSourceName = PreferencesManager.preferredAudioSource.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        audioSourceMenu.setText(selectedAudioSourceName, false)

        audioSourceMenu.setOnItemClickListener { parent, view, position, id ->
            val selectedAudioSource = parent.getItemAtPosition(position) as String
            activity.lifecycle.coroutineScope.launch {
                PreferencesManager.setPreferredAudioSource(context, selectedAudioSource)

                onChangeAudioSourcePreference?.invoke()
            }
        }

        setTheme()
    }
}