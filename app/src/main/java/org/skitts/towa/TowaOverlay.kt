package org.skitts.towa

import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.content.Intent
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import androidx.core.view.setPadding
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

const val DB_NAME           = "towa.db"
const val DB_REL_ASSET_PATH = "databases/$DB_NAME"

class TowaOverlay : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_PROCESS_TEXT) {
            val text: String = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
            showDictionary(text)
        } else if (intent.action == Intent.ACTION_SEND) {
            val text: String = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            showDictionary(text)
        }
    }

    private fun showDictionary(text: String) {
        val vertLayout = LinearLayout(this)
        vertLayout.orientation = VERTICAL

        val loadingBar = ProgressBar(this)
        loadingBar.setPadding(0, 80, 0, 40)
        vertLayout.addView(loadingBar)

        val loadingText = TextView(this)
        loadingText.text = "検索中。。。"
        loadingText.gravity = Gravity.CENTER_HORIZONTAL
        loadingText.setPadding(0, 0, 0, 80)
        vertLayout.addView(loadingText)

        val builder = AlertDialog.Builder(this, R.style.Theme_Towa_Dialog_Matcha)
        builder.setView(vertLayout).setOnDismissListener { finish() }
        val alertDialog = builder.show()

        lifecycle.coroutineScope.launch {
            val db: SQLiteDatabase = openDB()
            val entries: MutableList<DictEntry> = queryDictionaryEntries(db, text)

            val dictView = buildDictUI(entries)

            val translationView = layoutInflater.inflate(R.layout.towa_overlay, null)

            alertDialog.setContentView(dictView)
        }

        //alertDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
    }

    private fun buildDictUI(entries: MutableList<DictEntry>) : View {
        if (entries.size == 0) {
            // TODO: nice looking layout here :)
            val noFoundView = TextView(this)
            noFoundView.text = "No entries Found."

            return noFoundView
        }

        val verticalList: LinearLayout = LinearLayout(this)
        verticalList.orientation = VERTICAL

        for (entry in entries) {
            val entryLayout = DictEntryLayout(this);
            entryLayout.populate(entry)
            verticalList.addView(entryLayout)
        }

        val scroll: ScrollView = ScrollView(this)
        scroll.addView(verticalList)

        val cons: ConstraintLayout = ConstraintLayout(this)
        cons.addView(scroll)

        return cons
    }

    // TODO: These funcs should really be in a separate file & class
    private fun queryDictionaryEntries(dictionary: SQLiteDatabase, text: String) : MutableList<DictEntry> {
        val lookupCursor = dictionary.rawQuery(
            "SELECT form_ids, primary_match_flags FROM towalookup WHERE form_or_reading = ? LIMIT 100",
            arrayOf(text)
        )

        val idCol = lookupCursor.getColumnIndex("form_ids")
        val matchFlagCol = lookupCursor.getColumnIndex("primary_match_flags")

        // There should only be one relevant entry in the lookup table
        val found = lookupCursor.moveToNext()
        if (!found) {  return mutableListOf<DictEntry>() }

        val ids        = lookupCursor.getString(idCol)
        val matchFlags = lookupCursor.getString(matchFlagCol)
        lookupCursor.close()

        val dictCursor = dictionary.rawQuery(
            "SELECT * FROM towadict WHERE form_id IN (${ids})",
            null
        )

        val formIdCol         = dictCursor.getColumnIndex("form_id")
        val primaryFormCol    = dictCursor.getColumnIndex("primary_form")
        val primaryReadingCol = dictCursor.getColumnIndex("primary_reading")
        val definitionsCol    = dictCursor.getColumnIndex("definitions")
        val posCol            = dictCursor.getColumnIndex("pos_info")
        val fieldCol          = dictCursor.getColumnIndex("field_info")
        val dialectCol        = dictCursor.getColumnIndex("dialect_info")
        val miscCol           = dictCursor.getColumnIndex("misc_info")
        val examplesEnCol     = dictCursor.getColumnIndex("examples_en")
        val examplesJpCol     = dictCursor.getColumnIndex("examples_jp")

        val relevantEntries: MutableList<DictEntry> = mutableListOf<DictEntry>()
        while (dictCursor.moveToNext()) {
            val entry = DictEntry()

            val formId = dictCursor.getInt(formIdCol)
            entry.id = formId

            val defStr: String               = dictCursor.getString(definitionsCol)
            val defListStr: List<String>     = defStr.split("␟")
            val defList: List<List<String>>  = defListStr.map{ d -> d.split("␞") }

            entry.examplesEN = processExampleString(dictCursor.getString(examplesEnCol))
            entry.examplesJP = processExampleString(dictCursor.getString(examplesJpCol))

            entry.posInfo     = processInfoString(dictCursor.getString(posCol))

            val usages: MutableSet<String> = mutableSetOf()
            entry.posInfo.values.forEach { v-> usages.addAll(v) }
            entry.primaryUsages = usages.toList()

            entry.fieldInfo   = processInfoString(dictCursor.getString(fieldCol))
            entry.dialectInfo = processInfoString(dictCursor.getString(dialectCol))
            entry.miscInfo    = processInfoString(dictCursor.getString(miscCol))

            entry.primaryForm    = dictCursor.getString(primaryFormCol)
            entry.primaryReading = dictCursor.getString(primaryReadingCol)

            val furiganaCursor = dictionary.rawQuery(
                "SELECT furigana_encoding FROM towafurigana WHERE primary_form = ? AND reading = ?",
                arrayOf(entry.primaryForm, entry.primaryReading)
            )
            val furiganaCol = furiganaCursor.getColumnIndex("furigana_encoding")
            if (furiganaCursor.moveToNext()) {
                entry.primaryFormWithFurigana = furiganaCursor.getString(furiganaCol)
            }
            furiganaCursor.close()

            val intonationCursor = dictionary.rawQuery(
                "SELECT intonation_encoding FROM towaintonation WHERE primary_form = ? AND reading = ?",
                arrayOf(entry.primaryForm, entry.primaryReading)
            )
            val intonationCol = intonationCursor.getColumnIndex("intonation_encoding")
            if (intonationCursor.moveToNext()) {
                val encodingStr = intonationCursor.getString(intonationCol)
                entry.intonation = encodingStr.split(",").map{ d -> d.toInt() }
            }
            intonationCursor.close()

            defList.map{ def -> entry.definitions.add(def) }

            relevantEntries.add(entry)
        }

        dictCursor.close()

        return relevantEntries
    }

    private fun processExampleString(examplesStr: String): Map<Int, String> {
        if (examplesStr.isEmpty()) return mapOf()

        val examplesEntries: List<String> = examplesStr.split("␟")
        val examplesMap: MutableMap<Int, String> = mutableMapOf()
        examplesEntries.map{ e ->
            val kv = e.split("␞")
            examplesMap.put(kv[0].toInt(), kv[1])
        }

        return examplesMap
    }

    private fun processInfoString(info: String): Map<Int, List<String>> {
        if (info.isEmpty()) return mapOf()

        val infoMapEntries: List<String> = info.split("␟")
        val infoMap: MutableMap<Int, List<String>> = mutableMapOf()
        infoMapEntries.map{ e ->
            val entry = e.split("␞")
            val key: Int = entry[0].toInt()
            val vals: List<String> = entry.subList(1, entry.size).map { c -> code2en(c) }
            infoMap.put(key, vals)
        }

        return infoMap
    }

    private suspend fun openDB() : SQLiteDatabase {
        val dbHelper = TowaDatabaseHelper(this)
        dbHelper.initializeDB()

        return dbHelper.readableDatabase
    }

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.towa_overlay)
    }
}