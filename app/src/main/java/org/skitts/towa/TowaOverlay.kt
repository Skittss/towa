package org.skitts.towa

import org.skitts.towa.DictEntryLayout

import android.os.Bundle
import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.content.Intent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.widget.ConstraintLayout
import org.skitts.towa.ui.theme.TowaTheme
import java.io.File

const val DB_REL_ASSET_PATH = "/towa.db"

class TowaOverlay : Activity() {
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
        val db: SQLiteDatabase = openDB()
        val entries: MutableList<DictEntry> = queryDictionaryEntries(db, text)

        val dictView = buildDictUI(entries)

        val translationView = layoutInflater.inflate(R.layout.towa_overlay, null)

        val builder = AlertDialog.Builder(this)
        builder.setView(dictView).setOnDismissListener { finish() }
        val alertDialog = builder.show()

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
            val entryLayout: DictEntryLayout = DictEntryLayout(this);
            entryLayout.populate(entry)
            verticalList.addView(entryLayout)
        }

        val scroll: ScrollView = ScrollView(this)
        scroll.addView(verticalList)

        val cons: ConstraintLayout = ConstraintLayout(this)
        cons.addView(scroll)

        return cons
    }

    private fun queryDictionaryEntries(dictionary: SQLiteDatabase, text: String) : MutableList<DictEntry> {
        val lookupCursor = dictionary.rawQuery(
            "SELECT form_ids, primary_match_flags FROM towalookup WHERE form_or_reading = ? LIMIT 100",
            arrayOf(text)
        )
        if (lookupCursor.count == 0) { return mutableListOf<DictEntry>() }

        val idCol = lookupCursor.getColumnIndex("form_ids")
        val matchFlagCol = lookupCursor.getColumnIndex("primary_match_flags")

        // There should only be one relevant entry in the lookup table
        lookupCursor.moveToNext()
        val ids        = lookupCursor.getString(idCol)
        val matchFlags = lookupCursor.getString(matchFlagCol)
        lookupCursor.close()

        val dictCursor = dictionary.rawQuery(
            "SELECT * FROM towadict WHERE form_id IN (${ids})",
            null
        )
        if (dictCursor.count == 0) { return mutableListOf<DictEntry>() }

        val formIdCol         = dictCursor.getColumnIndex("form_id")
        val primaryFormCol    = dictCursor.getColumnIndex("primary_form")
        val primaryReadingCol = dictCursor.getColumnIndex("primary_reading")
        val definitionsCol    = dictCursor.getColumnIndex("definitions")

        val relevantEntries: MutableList<DictEntry> = mutableListOf<DictEntry>()
        // Pre
        while (dictCursor.moveToNext()) {
            val entry = DictEntry()

            val definitions    = dictCursor.getString(definitionsCol)

            entry.primaryForm    = dictCursor.getString(primaryFormCol)
            entry.primaryReading = dictCursor.getString(primaryReadingCol)
            //entry.definitions    = entry.definitions + List<String>(definitions)

            val formId         = dictCursor.getInt(formIdCol)

            relevantEntries.add(entry)
        }

        dictCursor.close()

        return relevantEntries
    }

    private fun openDB() : SQLiteDatabase {
        val dbPath: String = cacheDir.absolutePath + DB_REL_ASSET_PATH
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            assets.open("towa.db").copyTo(dbFile.outputStream())
        }

        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)
    }

}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.towa_overlay)
    }
}