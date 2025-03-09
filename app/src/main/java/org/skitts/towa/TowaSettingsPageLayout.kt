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
import org.w3c.dom.Text
import java.util.Locale
import kotlin.math.max

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
            resources.getString(R.string.localization_name_en),
            resources.getString(R.string.localization_name_jp),
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
        val dictTitle            = findViewById<TextView>(R.id.page_settings_dictionary_title)
        val dictUnder            = findViewById<View>(R.id.page_settings_dictionary_div)
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

        dictTitle.setTextColor(ThemeManager.colTextPrimary)
        dictTitle.setTypeface(ankiTitle.typeface, Typeface.BOLD)
        dictUnder.setBackgroundColor(ThemeManager.colTextPrimary)

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
        val dictLengthMenu   = findViewById<TextInputEditText>(R.id.page_settings_dict_length_menu)

        var selectedThemeName: String = ""
        when (ThemeManager.themeName) {
            "matcha" -> { selectedThemeName = context.getString(R.string.theme_name_matcha) }
            "hinomaru" -> { selectedThemeName = context.getString(R.string.theme_name_hinomaru) }
            "tsuki" -> { selectedThemeName = context.getString(R.string.theme_name_tsuki) }
        }
        themeMenu.setText(selectedThemeName, false)

        themeMenu.setOnItemClickListener { parent, view, position, id ->
            val localeSelectedTheme = parent.getItemAtPosition(position) as String
            val selectedTheme: String
            when (localeSelectedTheme) {
                context.getString(R.string.theme_name_matcha) -> { selectedTheme = "matcha" }
                context.getString(R.string.theme_name_hinomaru) -> { selectedTheme = "hinomaru" }
                context.getString(R.string.theme_name_tsuki) -> { selectedTheme = "tsuki" }
                else -> { selectedTheme = "matcha"}
            }

            activity.lifecycle.coroutineScope.launch {
                ThemeManager.setThemeForSession(context, selectedTheme)
                ThemeManager.updateTheme(activity)

                onChangeTheme?.invoke()
            }
        }

        var selectedLanguageName: String = ""
        when (PreferencesManager.preferredTranslationLanguage) {
            "english" -> { selectedLanguageName = context.getString(R.string.language_name_en) }
            "japanese" -> { selectedLanguageName = context.getString(R.string.language_name_jp) }
        }
        languageMenu.setText(selectedLanguageName, false)

        languageMenu.setOnItemClickListener { parent, view, position, id ->
            val localeSelectedLanguage = parent.getItemAtPosition(position) as String
            val selectedLanguage: String
            when (localeSelectedLanguage) {
                context.getString(R.string.language_name_en) -> { selectedLanguage = "english" }
                context.getString(R.string.language_name_jp) -> { selectedLanguage = "japanese" }
                else -> { selectedLanguage = "english"}
            }

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

        var selectedAudioSourceName: String = ""
        when (PreferencesManager.preferredAudioSource) {
            "tofugu" -> { selectedAudioSourceName = context.getString(R.string.audio_source_tofugu) }
            "kanji alive" -> { selectedAudioSourceName = context.getString(R.string.audio_source_kanji_alive) }
        }
        audioSourceMenu.setText(selectedAudioSourceName, false)

        audioSourceMenu.setOnItemClickListener { parent, view, position, id ->
            val localeSelectedAudioSource = parent.getItemAtPosition(position) as String
            val selectedAudioSource: String
            when (localeSelectedAudioSource) {
                context.getString(R.string.audio_source_tofugu) -> { selectedAudioSource = "tofugu" }
                context.getString(R.string.audio_source_kanji_alive) -> { selectedAudioSource = "kanji alive" }
                else -> { selectedAudioSource = "kanji alive"}
            }

            activity.lifecycle.coroutineScope.launch {
                PreferencesManager.setPreferredAudioSource(context, selectedAudioSource)

                onChangeAudioSourcePreference?.invoke()
            }
        }

        dictLengthMenu.setText(PreferencesManager.dictLength.toInt().toString())
        dictLengthMenu.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) return

                val newS = s.replace(Regex("[^0-9]"), "")
                if (newS != s.toString()) {
                    dictLengthMenu.setText(newS)
                    return
                };

                activity.lifecycle.coroutineScope.launch {
                    val len = max(s.toString().toInt(), 0)
                    PreferencesManager.setDictLength(context, len.toUInt())
                }
            }
        })

        setTheme()
    }
}