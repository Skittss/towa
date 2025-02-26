package org.skitts.towa

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ichi2.anki.api.AddContentApi

const val ANKI_PERM_REQUEST      = 3212
const val ANKI_DEFAULT_DECK_NAME = "とは (Towa)"
const val ANKI_MODEL_NAME        = "とは"

class AnkiHelper(
    private val context: Context,
    private val activity: Activity
) {
    private val api = AddContentApi(context)

    suspend fun add(entry: DictEntry, defIdx: Int = -1): Boolean {
        val ankiExists = hasAnki()
        if (!ankiExists) return false

        val ankiRWperms = getAnkiReadWritePermissions()
        if (!ankiRWperms) return false

        val deckName = getDeckName()
        val deckId = getDeck(deckName)
        val modelId = getModel(deckId, ANKI_MODEL_NAME)

        val noteId = api.addNote(
            modelId,
            deckId,
            getAnkiCompatibleDictEntry(entry, defIdx),
            HashSet())
        return noteId != null
    }

    private fun getAnkiCompatibleDictEntry(entry: DictEntry, defIdx: Int = -1): Array<String> {
        val primaryFormFurigana: String =
            entry.furigana[Pair(entry.primaryForm, entry.primaryReading)] ?: entry.primaryForm
        val furiganaParts = primaryFormFurigana.split(";")

        var ankiCompatibleFurigana = ""
        for ((i, part) in furiganaParts.withIndex()) {
            if (i % 2 == 0) {
                ankiCompatibleFurigana += part.replace("{", "")
            } else {
                ankiCompatibleFurigana += "[" + part.replace("}", "]")
            }
        }

        var ankiCompatibleDefs = ""
        if (defIdx >= 0) {
            ankiCompatibleDefs += entry.definitions[defIdx].joinToString(", ")
        } else {
            entry.definitions.forEachIndexed{ i, d ->
                ankiCompatibleDefs += "$i. ${d.joinToString(", ")}"
                if (i != entry.definitions.size - 1) ankiCompatibleDefs += "<br>"
            }
        }

        val exampleJp: String? = if (defIdx < 0) entry.examplesJP[0] else entry.examplesJP[defIdx]
        val exampleEn: String? = if (defIdx < 0) entry.examplesEN[0] else entry.examplesEN[defIdx]

        var ankiCompatibleUsages = ""
        if (defIdx < 0) {
            ankiCompatibleUsages += entry.primaryUsages.joinToString(" / ")
        } else {
            ankiCompatibleUsages += entry.posInfo[defIdx]!!.joinToString(" / ")
        }

        return arrayOf(
            entry.primaryForm,
            ankiCompatibleFurigana,
            entry.primaryReading,
            ankiCompatibleDefs,
            ankiCompatibleUsages,
            exampleJp ?: "",
            exampleEn ?: ""
        )
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

    private suspend fun getDeckName(): String {
        val dbHelper = TowaDatabaseHelper(context)
        var deckName = dbHelper.readPreferenceSynchronous(PreferencesKeys.ANKI_DECK_NAME, "")

        if (deckName.isEmpty()) {
            deckName = ANKI_DEFAULT_DECK_NAME
            dbHelper.writePreference(PreferencesKeys.ANKI_DECK_NAME, deckName)
        }

        return deckName
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
            arrayOf("English"),
            arrayOf(ANKI_TEMPLATE_FRONT),
            arrayOf(ANKI_TEMPLATE_BACK),
            ANKI_TEMPLATE_CSS,
            deckId,
            0
        )
    }

}