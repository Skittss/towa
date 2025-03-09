package org.skitts.towa

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.core.view.KeyEventDispatcher.Component
import java.util.Locale

const val DEFAULT_TRANSLATION_LANGUAGE = "english"
const val DEFAULT_LOCALIZATION = "english"
const val DEFAULT_ANKI_DECK_NAME = "とは (Towa)"
const val DEFAULT_AUDIO_SOURCE = "tofugu"
const val DEFAULT_DICT_LENGTH = 100u

class PreferencesManager {
    companion object{

        private val validLanguages: HashSet<String> = hashSetOf(
            "english",
            "japanese"
        )
        private val validLocalizations: HashSet<String> = hashSetOf(
            "english",
            "日本語"
        )
        private val validAudioSources: HashSet<String> = hashSetOf(
            "tofugu",
            "kanji alive"
        )

        suspend fun loadPreferencesForSession(context: Context) {
            val dbHelper = TowaDatabaseHelper(context)

            preferredTranslationLanguage = dbHelper.readPreferenceSynchronous(
                PreferencesKeys.PREF_TRANSLATION_LANGUAGE, "")
            if (preferredTranslationLanguage.isEmpty()) {
                preferredTranslationLanguage = DEFAULT_TRANSLATION_LANGUAGE
                dbHelper.writePreference(PreferencesKeys.PREF_TRANSLATION_LANGUAGE, preferredTranslationLanguage)
            }

            localization = dbHelper.readPreferenceSynchronous(
                PreferencesKeys.LOCALIZATION, "")
            if (localization.isEmpty()) {
                localization = DEFAULT_LOCALIZATION
                dbHelper.writePreference(PreferencesKeys.LOCALIZATION, localization)
            }

            ankiDeckName = dbHelper.readPreferenceSynchronous(
                PreferencesKeys.ANKI_DECK_NAME, "")
            if (ankiDeckName.isEmpty()) {
                ankiDeckName = DEFAULT_ANKI_DECK_NAME
                dbHelper.writePreference(PreferencesKeys.ANKI_DECK_NAME, ankiDeckName)
            }

            preferredAudioSource = dbHelper.readPreferenceSynchronous(
                PreferencesKeys.PREF_AUDIO_SOURCE, "")
            if (preferredAudioSource.isEmpty()) {
                preferredAudioSource = DEFAULT_AUDIO_SOURCE
                dbHelper.writePreference(PreferencesKeys.PREF_AUDIO_SOURCE, preferredAudioSource)
            }

            val dictLengthInt = dbHelper.readPreferenceSynchronous(
                PreferencesKeys.DICT_LENGTH, -1)
            if (dictLengthInt < 0) {
                dictLength = DEFAULT_DICT_LENGTH
                dbHelper.writePreference(PreferencesKeys.DICT_LENGTH, dictLengthInt)
            } else {
                dictLength = dictLengthInt.toUInt()
            }
        }

        fun applyLocalization(activity: ComponentActivity) {
            val loc = if (localization == "english")
                Locale.ENGLISH else Locale.JAPANESE
            val config = activity.resources.configuration
            config.setLocale(loc)

            activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        }

        suspend fun setPreferredTranslationLanguage(context: Context, language: String) {
            val sanitizedLanguage = language.lowercase()
            if (!validLanguages.contains(sanitizedLanguage)) return

            preferredTranslationLanguage = sanitizedLanguage

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.PREF_TRANSLATION_LANGUAGE, preferredTranslationLanguage)
        }

        suspend fun setLocalization(context: Context, loc: String) {
            val sanitizedLocalization = loc.lowercase()
            if (!validLocalizations.contains(sanitizedLocalization)) return

            localization = sanitizedLocalization

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.LOCALIZATION, localization)
        }

        suspend fun setAnkiDeckName(context: Context, name: String) {
            val sanitizedDeckName = name.lowercase()
            if (!validLocalizations.contains(sanitizedDeckName)) return

            ankiDeckName = sanitizedDeckName

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.ANKI_DECK_NAME, ankiDeckName)
        }

        suspend fun setPreferredAudioSource(context: Context, source: String) {
            val sanitizedSource = source.lowercase()
            if (!validAudioSources.contains(sanitizedSource)) return

            preferredAudioSource = sanitizedSource

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.PREF_AUDIO_SOURCE, preferredAudioSource)
        }

        suspend fun setDictLength(context: Context, length: UInt) {
            dictLength = length

            val dbHelper = TowaDatabaseHelper(context)
            dbHelper.writePreference(PreferencesKeys.DICT_LENGTH, dictLength.toInt())
        }

        var preferredTranslationLanguage: String = ""
            private set
        var localization:                 String = ""
            private set
        var ankiDeckName:                 String = ""
            private set
        var preferredAudioSource:         String = ""
            private set
        var dictLength:                   UInt = 50u
            private set
    }

}