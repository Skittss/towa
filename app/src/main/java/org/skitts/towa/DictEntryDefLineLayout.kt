package org.skitts.towa

import android.content.Context
import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class DictEntryDefLineLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry_def_line, this)
    }

    var showDetails  = false
    var hasExampleJP = false
    var hasExampleEN = false
    var hasMiscInfo  = false

    public fun populate(
        num: Int,
        defs: List<String>,
        exampleJp: String?,
        exampleEn: String?,
        pos: List<String>,
        miscInfo: List<String>?
    ) {
        hasExampleJP = exampleJp != null
        hasExampleEN = exampleEn != null
        hasMiscInfo  = miscInfo != null

        val defCont       = findViewById<LinearLayout>(R.id.def_line_list_container)
        val defNum        = findViewById<TextView>(R.id.def_defs_number)
        val defLine       = findViewById<TextView>(R.id.def_defs)
        val posNum        = findViewById<TextView>(R.id.def_pos_number)
        val posLine       = findViewById<TextView>(R.id.def_pos)
        val exampleJpNum  = findViewById<TextView>(R.id.def_example_jp_num)
        val exampleJpLine = findViewById<TextView>(R.id.def_example_jp)
        val exampleEnNum  = findViewById<TextView>(R.id.def_example_en_num)
        val exampleEnLine = findViewById<TextView>(R.id.def_example_en)
        val miscNum       = findViewById<TextView>(R.id.def_info_number)
        val miscLine      = findViewById<TextView>(R.id.def_info)

        defCont.setOnClickListener {
            showDetails = !showDetails
            updateDetailsVisibility()
        }

        val numStr: String = String.format(Locale.getDefault(), "%d.", num)

        defNum.text        = numStr
        defLine.text       = defs.joinToString(", ")

        posNum.text        = numStr
        posLine.text       = pos.joinToString(" / ")
        posLine.setTypeface(posLine.typeface, Typeface.ITALIC)

        exampleJpNum.text         = numStr
        exampleJpLine.text        = exampleJp

        exampleEnNum.text        = numStr
        exampleEnLine.text       = exampleEn

        miscNum.text        = numStr
        miscLine.text       = miscInfo?.joinToString(", ") ?: ""
        miscLine.setTypeface(miscLine.typeface, Typeface.ITALIC)

        updateDetailsVisibility()
    }

    private fun updateDetailsVisibility() {
        val posNum        = findViewById<TextView>(R.id.def_pos_number)
        val posLine       = findViewById<TextView>(R.id.def_pos)
        val exampleJpNum  = findViewById<TextView>(R.id.def_example_jp_num)
        val exampleJpLine = findViewById<TextView>(R.id.def_example_jp)
        val exampleEnNum  = findViewById<TextView>(R.id.def_example_en_num)
        val exampleEnLine = findViewById<TextView>(R.id.def_example_en)
        val miscNum       = findViewById<TextView>(R.id.def_info_number)
        val miscLine      = findViewById<TextView>(R.id.def_info)

        if (showDetails) {
            posNum.visibility        = INVISIBLE
            posLine.visibility       = VISIBLE
            exampleJpNum.visibility  = if (hasExampleJP) INVISIBLE else GONE
            exampleJpLine.visibility = if (hasExampleJP) VISIBLE else GONE
            exampleEnNum.visibility  = if (hasExampleEN) INVISIBLE else GONE
            exampleEnLine.visibility = if (hasExampleEN) VISIBLE else GONE
            miscNum.visibility       = if (hasMiscInfo) INVISIBLE else GONE
            miscLine.visibility      = if (hasMiscInfo) VISIBLE else GONE
        } else {
            posNum.visibility        = GONE
            posLine.visibility       = GONE
            exampleJpNum.visibility  = GONE
            exampleJpLine.visibility = GONE
            exampleEnNum.visibility  = GONE
            exampleEnLine.visibility = GONE
            miscNum.visibility       = GONE
            miscLine.visibility      = GONE
        }
    }
}