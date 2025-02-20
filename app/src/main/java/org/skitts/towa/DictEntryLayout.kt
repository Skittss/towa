package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.graphics.Typeface.*
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat

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

        if (entry.jlptLevel > 0) {
            val col     = ContextCompat.getColor(context, R.color.matcha_dark)
            val jlptStr = "N${entry.jlptLevel}"
            addTag(col, jlptStr)
        }

        if (entry.common) {
            val col = ContextCompat.getColor(context, R.color.matcha_accent_med)
            addTag(col, "C")
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
            otherFormsCont.visibility = GONE
            otherForms.visibility     = GONE
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
            separatorView.setTextColor(ContextCompat.getColor(context, R.color.matcha_text_secondary))
            separatorView.setPadding(0, 0, 30, 0)
            pitches.addView(separatorView)

            val otherIntonationView = IntonationView(context)
            val otherIntonations: List<Int> = entry.intonations[otherReading] ?: listOf()
            otherIntonationView.populate(otherReading, otherIntonations)
            pitches.addView(otherIntonationView)
        }

        val usages = TextView(context)
        usages.text = entry.primaryUsages.joinToString(" / ")
        usages.setTextColor(ContextCompat.getColor(context, R.color.matcha_text_secondary))
        usages.setTypeface(usages.typeface, ITALIC)
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

    private fun addTag(@ColorInt color: Int, text: String = "") {
        val primaryFormCont   = findViewById<LinearLayout>(R.id.info_tag_container)

        val tagSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17f, resources.displayMetrics)
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(0, 0, 20, 10)

        val tag = TextView(context)
        tag.layoutParams = layoutParams
        tag.width  = tagSize.toInt()
        tag.height = tagSize.toInt()
        tag.text = text
        tag.setTypeface(DEFAULT_BOLD)

        tag.gravity = Gravity.CENTER

        tag.textSize = 0.2f * tagSize.toFloat()
        tag.setTextColor(ContextCompat.getColor(context, R.color.matcha_light))

        val tagDrawable     = getDrawable(context, R.drawable.word_tag)!!
        val wrappedDrawable = DrawableCompat.wrap(tagDrawable)
        DrawableCompat.setTint(wrappedDrawable, color)

        tag.background = wrappedDrawable
        primaryFormCont.addView(tag, 0)
    }

}