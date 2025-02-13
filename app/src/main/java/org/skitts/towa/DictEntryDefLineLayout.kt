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
    var hasCrossRefs = false

    public fun populate(
        num: Int,
        defs: List<String>,
        exampleJp: String?,
        exampleEn: String?,
        pos: List<String>,
        miscInfo: List<String>?,
        crossRefs: List<CrossRef>?
    ) {
        hasExampleJP = exampleJp != null
        hasExampleEN = exampleEn != null
        hasMiscInfo  = miscInfo != null
        hasCrossRefs = crossRefs != null

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
        val crossRefsNum  = findViewById<TextView>(R.id.def_cross_ref_num)
        val crossRefsLine = findViewById<TextView>(R.id.def_cross_ref)

        defCont.setOnClickListener {
            showDetails = !showDetails
            updateDetailsVisibility()
        }

        val numStr: String = String.format(Locale.getDefault(), "%d.", num)
        val xRefStr: String = String.format(Locale.getDefault(), "See Also: %s",
            crossRefs?.joinToString(", ") { c -> c.form })

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

        crossRefsNum.text = numStr
        crossRefsLine.text = xRefStr

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
        val crossRefsNum  = findViewById<TextView>(R.id.def_cross_ref_num)
        val crossRefsLine = findViewById<TextView>(R.id.def_cross_ref)

        if (showDetails) {
            posNum.visibility        = INVISIBLE
            posLine.visibility       = VISIBLE
            exampleJpNum.visibility  = if (hasExampleJP) INVISIBLE else GONE
            exampleJpLine.visibility = if (hasExampleJP) VISIBLE else GONE
            exampleEnNum.visibility  = if (hasExampleEN) INVISIBLE else GONE
            exampleEnLine.visibility = if (hasExampleEN) VISIBLE else GONE
            miscNum.visibility       = if (hasMiscInfo) INVISIBLE else GONE
            miscLine.visibility      = if (hasMiscInfo) VISIBLE else GONE
            crossRefsNum.visibility  = if (hasCrossRefs) INVISIBLE else GONE
            crossRefsLine.visibility = if (hasCrossRefs) VISIBLE else GONE
        } else {
            posNum.visibility        = GONE
            posLine.visibility       = GONE
            exampleJpNum.visibility  = GONE
            exampleJpLine.visibility = GONE
            exampleEnNum.visibility  = GONE
            exampleEnLine.visibility = GONE
            miscNum.visibility       = GONE
            miscLine.visibility      = GONE
            crossRefsNum.visibility  = GONE
            crossRefsLine.visibility = GONE
        }
    }
}