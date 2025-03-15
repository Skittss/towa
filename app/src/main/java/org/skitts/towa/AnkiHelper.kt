package org.skitts.towa

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.ichi2.anki.FlashCardsContract
import com.ichi2.anki.api.AddContentApi
import java.io.File
import java.io.OutputStream

const val ANKI_PERM_REQUEST      = 3212
const val ANKI_MODEL_NAME        = "とは"
const val AUDIO_TOFUGU_PATH      = "audio/tofugu"
const val AUDIO_KANJI_ALIVE_PATH = "audio/kanji_alive"

class AnkiHelper(
    private val context: Context,
    private val activity: Activity
) {
    private val api = AddContentApi(context)

    fun add(entry: DictEntry, defIdx: Int = -1): Boolean {
        val ankiExists = hasAnki()
        if (!ankiExists) return false

        val ankiRWperms = getAnkiReadWritePermissions()
        if (!ankiRWperms) return false

        val entryData: Array<String> = getAnkiCompatibleDictEntry(entry, defIdx)
        if (entryData.isEmpty()) return false

        val deckName = getDeckName()
        val deckId = getDeck(deckName)
        val modelId = getModel(deckId, ANKI_MODEL_NAME)

        val noteId = api.addNote(
            modelId,
            deckId,
            entryData,
            HashSet())
        return noteId != null
    }

    private fun getAnkiCompatibleDictEntry(entry: DictEntry, defIdx: Int = -1): Array<String> {
        var usedDefIdx = defIdx
        if (defIdx < 0 && entry.definitions.size == 1) usedDefIdx = 0

        val primaryFormFurigana: String =
            entry.furigana[Pair(entry.primaryForm, entry.primaryReading)] ?: entry.primaryForm
        val furiganaParts = primaryFormFurigana.split("{")

        var ankiCompatibleFurigana = ""
        for (part in furiganaParts) {
            ankiCompatibleFurigana += part
                .replace(";", "[")
                .replace("}", "]")
        }

        var ankiCompatibleDefs = ""
        if (usedDefIdx >= 0) {
            ankiCompatibleDefs += entry.definitions[usedDefIdx].joinToString(", ")
        } else {
            entry.definitions.forEachIndexed{ i, d ->
                ankiCompatibleDefs += "$i. ${d.joinToString(", ")}"
                if (i != entry.definitions.size - 1) ankiCompatibleDefs += "<br>"
            }
        }

        val exampleJp: String? = if (usedDefIdx < 0) entry.examplesJP[0] else entry.examplesJP[usedDefIdx]
        val exampleEn: String? = if (usedDefIdx < 0) entry.examplesEN[0] else entry.examplesEN[usedDefIdx]

        var ankiCompatibleUsages = ""
        if (usedDefIdx < 0) {
            ankiCompatibleUsages += entry.primaryUsages.joinToString(" / ")
        } else {
            ankiCompatibleUsages += entry.posInfo[usedDefIdx]!!.joinToString(" / ")
        }

        val hasTofuguExample = entry.audioSources.and(0b0001) > 0
        val hasKanjiAliveExample = entry.audioSources.and(0b0010) > 0
        val useTofugu = (!hasKanjiAliveExample && hasTofuguExample)
                || (hasTofuguExample && PreferencesManager.preferredAudioSource == "tofugu")
        val useKanjiAlive = (!useTofugu && hasKanjiAliveExample)
                || (hasKanjiAliveExample && PreferencesManager.preferredAudioSource == "kanji_alive")

        var audioExample = ""
        if (useTofugu) {
            val audioPath = copyAudioFileToAnki(
                "${AUDIO_TOFUGU_PATH}/${entry.primaryForm}.mp3",
                "とは_${entry.primaryForm}_tofugu"
            )
            if (audioPath.isNotEmpty()) audioExample =  "[sound:$audioPath]"
        }
        else if (useKanjiAlive) {
            val audioPath = copyAudioFileToAnki(
                "${AUDIO_KANJI_ALIVE_PATH}/${entry.primaryForm}.mp3",
                "とは_${entry.primaryForm}_kanji_alive"
            )
            if (audioPath.isNotEmpty()) audioExample =  "[sound:$audioPath]"
        }

        var primaryReading = entry.primaryReading
        if (!entry.intonations[entry.primaryReading].isNullOrEmpty()) {
            primaryReading = getIntonationReadingStr(
                entry.primaryReading,
                entry.intonations[entry.primaryReading]!!.first(),
                false)
        }

        val otherForms =
            if (entry.otherForms.isNotEmpty()) "Other forms: " + entry.otherForms.joinToString(", ")
            else ""

        var otherReadings = ""
        if (entry.otherReadings.isNotEmpty()) {
            otherReadings += "Other readings: "

            val strs = entry.otherReadings.map { reading ->
                var strReading =
                if (!entry.intonations[reading].isNullOrEmpty())
                    getIntonationReadingStr(reading, entry.intonations[reading]!!.first(), true)
                else
                    reading
                strReading
            }

            otherReadings += strs.joinToString(", ")
        }

        return arrayOf(
            entry.primaryForm,
            ankiCompatibleFurigana,
            primaryReading,
            ankiCompatibleDefs,
            ankiCompatibleUsages,
            exampleJp ?: "",
            exampleEn ?: "",
            audioExample,
            otherForms,
            otherReadings
        )
    }

    private fun copyAudioFileToAnki(assetPath: String, externalName: String): String {
        val lastPathSegment = Uri.parse(assetPath).lastPathSegment ?: assetPath

        val file = File(context.cacheDir, lastPathSegment)
        if (!file.exists()) {
            context.assets.open(assetPath).use { inputStream ->
                file.outputStream().use { outputStream: OutputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
        file.deleteOnExit();

        val fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
        context.grantUriPermission(
            "com.ichi2.anki",
            fileUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )

        val contentValues = ContentValues()
        contentValues.put(FlashCardsContract.AnkiMedia.FILE_URI, fileUri.toString())
        contentValues.put(
            FlashCardsContract.AnkiMedia.PREFERRED_NAME,
            externalName
        )

        val contentResolver = context.contentResolver
        val returnUri =
            contentResolver.insert(FlashCardsContract.AnkiMedia.CONTENT_URI, contentValues)
            ?: return ""

        Log.d("#DB", "Copied audio file to Anki: ${returnUri.path}")

        return returnUri.path.toString()
    }

    fun getIntonationReadingStr(reading: String, i: Int, thin: Boolean): String {
        // Mora 0 assumed low unless intonation is 1: LH -> HL
        // Mora out of string idx range indicates intonation down-tick for following particle.
        // Once intonations go H -> L, they do not go back up
        val lowHighTemplate = if (thin) INTONATION_LOW_HIGH_TEMPLATE_THIN else INTONATION_LOW_HIGH_TEMPLATE
        val highLowTemplate = if (thin) INTONATION_HIGH_LOW_TEMPLATE_THIN else INTONATION_HIGH_LOW_TEMPLATE
        val lowTemplate  = if (thin) INTONATION_LOW_TEMPLATE_THIN  else INTONATION_LOW_TEMPLATE
        val highTemplate = if (thin) INTONATION_HIGH_TEMPLATE_THIN else INTONATION_HIGH_TEMPLATE

        var str = ""

        if (i != 1) {
            val lhText = reading.substring(0, 1)
            str += lowHighTemplate.replace("{}", lhText)

            val needsLowSegment: Boolean = i != 0 && i != reading.length
            val highLowSegmentEnd: Int = if (i == 0) reading.length else i
            val highPitchResource = if (i == 0)
                highTemplate else
                highLowTemplate

            val hText = reading.substring(1, highLowSegmentEnd)
            str += highPitchResource.replace("{}", hText)

            if (needsLowSegment) {
                val lText = reading.substring(highLowSegmentEnd, reading.length)
                str += lowTemplate.replace("{}", lText)
            }
        } else {
            val hlText = reading.substring(0, 1)
            str += highLowTemplate.replace("{}", hlText)

            val lText = reading.substring(1, reading.length)
            str += lowTemplate.replace("{}", lText)
        }

        return str
    }

    private fun hasAnki(): Boolean {
        return AddContentApi.getAnkiDroidPackageName(context) != null
    }

    private fun getAnkiReadWritePermissions(): Boolean {
        if (hasAnkiReadWritePermissions()) return true

        ActivityCompat.requestPermissions(
            activity,
            arrayOf(AddContentApi.READ_WRITE_PERMISSION),
            ANKI_PERM_REQUEST
        )

        return hasAnkiReadWritePermissions()
    }

    private fun hasAnkiReadWritePermissions(): Boolean {
        val permission = ContextCompat.checkSelfPermission(context, AddContentApi.READ_WRITE_PERMISSION)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun getDeckName(): String {
        return PreferencesManager.ankiDeckName
    }

    private fun getDeck(name: String): Long {
        val api = AddContentApi(context)
        for ((id, deckName) in api.deckList) {
            if (name.equals(deckName, ignoreCase = true)) return id
        }
        return api.addNewDeck(name)
    }

    private fun getModel(deckId: Long, name: String): Long {
        val api = AddContentApi(context)
        for ((id, modelName) in api.deckList) {
            if (name.equals(modelName, ignoreCase = true)) return id
        }
        return api.addNewCustomModel(
            name,
            ANKI_TEMPLATE_FIELDS,
            arrayOf("English Def"),
            arrayOf(ANKI_TEMPLATE_FRONT),
            arrayOf(ANKI_TEMPLATE_BACK),
            ANKI_TEMPLATE_CSS,
            deckId,
            0
        )
    }

}