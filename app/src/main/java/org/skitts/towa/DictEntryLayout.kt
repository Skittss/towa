package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Locale

class DictEntryLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry, this)
    }

    private var showMore: Boolean = false;
    private var currentEntry = DictEntry()

    private var moreButton: TextView = TextView(context)
    private var defEntries: MutableList<DictEntryDefLineLayout> = mutableListOf()

    public fun populate(entry: DictEntry) {
        currentEntry = entry
        defEntries.clear()

        val primaryForm   = findViewById<FuriganaView>(R.id.primary_form)
        val pitches       = findViewById<LinearLayout>(R.id.readings)
        val primaryUsages = findViewById<LinearLayout>(R.id.primary_usages_container)
        val defContainer  = findViewById<LinearLayout>(R.id.def_container)

        if (currentEntry.primaryFormWithFurigana != null) {
            primaryForm.setText(currentEntry.primaryFormWithFurigana)
        } else {
            primaryForm.setText(currentEntry.primaryForm)
        }

        val intonation = IntonationView(context)
        intonation.populate(currentEntry.primaryReading, currentEntry.intonation)
        pitches.addView(intonation)

        val usages = TextView(context)
        usages.text = entry.primaryUsages.joinToString(" / ")
        usages.setTypeface(usages.typeface, Typeface.ITALIC)
        usages.setPadding(0,0,0, 10)
        primaryUsages.addView(usages)

        currentEntry.definitions.forEachIndexed { i, defs ->
            val defLine = DictEntryDefLineLayout(defContainer.context)
            val pos: List<String> = entry.posInfo[i] ?: listOf()
            defLine.populate(
                i + 1,
                defs,
                entry.examplesJP[i],
                entry.examplesEN[i],
                pos,
                entry.miscInfo[i],
                entry.crossRefs[i])
            defContainer.addView(defLine)
            defEntries.add(defLine)
        }

        val otherForms = FuriganaView(context)
        //otherForms.text = entry.

    }

}