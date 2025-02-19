package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes

class DictEntryLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry, this)
    }

    public fun populate(entry: DictEntry) {
        val primaryForm       = findViewById<FuriganaView>(R.id.primary_form)
        val otherForms        = findViewById<FuriganaView>(R.id.other_forms)
        val readingsCont      = findViewById<LinearLayout>(R.id.readings_container)
        val otherFormsCont    = findViewById<LinearLayout>(R.id.other_forms_container)
        val pitches           = findViewById<LinearLayout>(R.id.readings)
        val primaryUsages     = findViewById<LinearLayout>(R.id.primary_usages_container)
        val defContainer      = findViewById<LinearLayout>(R.id.def_container)

        if (entry.common) {
            addTag(R.drawable.word_tag_common)
        }

        if (entry.jlptLevel > 0) {
            addTag(R.drawable.word_tag_common)
        }

        val primaryFormFurigana: String? = entry.furigana[Pair(entry.primaryForm, entry.primaryReading)]
        primaryForm.setText(primaryFormFurigana ?: entry.primaryForm)

        if (entry.otherForms.isNotEmpty()) {
            val allReadings = listOf(entry.primaryReading) + entry.otherReadings
            val displayFurigana = entry.otherForms.map{ f ->
                var displayForm = f
                for (r in allReadings) {
                    val furigana: String? = entry.furigana[Pair(f, r)]
                    if (!furigana.isNullOrEmpty()) {
                        displayForm = furigana
                        break
                    }
                }
                displayForm
            }

            val otherFormStr: String = displayFurigana.joinToString(", ")
            otherForms.setText(otherFormStr)
            readingsCont.setPadding(0, 0, 0, 12)
            otherFormsCont.setPadding(0,0, 0, 24)
        } else {
            otherFormsCont.visibility    = GONE
            otherForms.visibility        = GONE
            readingsCont.setPadding(0,0, 0, 24)
        }

        // Primary reading
        val primaryIntonationView = IntonationView(context)
        val primaryIntonations: List<Int> = entry.intonations[entry.primaryReading] ?: listOf()
        primaryIntonationView.populate(entry.primaryReading, primaryIntonations)
        pitches.addView(primaryIntonationView)

        // Other readings
        for (otherReading in entry.otherReadings) {
            // TODO: Set to sub-colour
            val separatorView = TextView(context)
            separatorView.text = "|"
            separatorView.setPadding(0, 0, 30, 0)
            pitches.addView(separatorView)

            val otherIntonationView = IntonationView(context)
            val otherIntonations: List<Int> = entry.intonations[otherReading] ?: listOf()
            otherIntonationView.populate(otherReading, otherIntonations)
            pitches.addView(otherIntonationView)
        }

        val usages = TextView(context)
        usages.text = entry.primaryUsages.joinToString(" / ")
        usages.setTypeface(usages.typeface, Typeface.ITALIC)
        usages.setPadding(0,0,0, 8)
        primaryUsages.addView(usages)

        entry.definitions.forEachIndexed { i, defs ->
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
        }
    }

    private fun addTag(@DrawableRes resid: Int ) {
        val primaryFormCont   = findViewById<LinearLayout>(R.id.info_tag_container)

        val commonTagSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 13f, resources.displayMetrics)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 0, 20, 10)

        val commonTag = TextView(context)
        commonTag.layoutParams = layoutParams
        commonTag.width  = commonTagSize.toInt()
        commonTag.height = commonTagSize.toInt()
        commonTag.setBackgroundResource(resid)
        primaryFormCont.addView(commonTag, 0)
    }

}