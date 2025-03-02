package org.skitts.towa

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

class DictEntryDefLineLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry_def_line, this)
    }

    private var showDetails  = false
    private var hasExampleJP = false
    private var hasExampleEN = false
    private var hasMiscInfo  = false
    private var hasCrossRefs = false

    fun populate(
        activity: ComponentActivity,
        frame: FrameLayout,
        entry: DictEntry,
        num: Int
    ) {
        val defs: List<String>         = entry.definitions[num]
        val exampleJp: String?         = entry.examplesJP[num]
        val exampleEn: String?         = entry.examplesEN[num]
        val pos: List<String>          = entry.posInfo[num] ?: listOf()
        val miscInfo: List<String>?    = entry.miscInfo[num]
        val crossRefs: List<CrossRef>? = entry.crossRefs[num]

        hasExampleJP = exampleJp != null
        hasExampleEN = exampleEn != null
        hasMiscInfo  = miscInfo != null
        hasCrossRefs = crossRefs != null

        val defCont       = findViewById<LinearLayout>(R.id.def_line)
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

        val exampleJpCont = findViewById<LinearLayout>(R.id.def_example_jp_container)
        val exampleEnCont = findViewById<LinearLayout>(R.id.def_example_en_container)

        defCont.setOnClickListener {
            showDetails = !showDetails
            updateDetailsVisibility()
        }

        defCont.setOnLongClickListener {
            val contextMenu = DictEntryContextMenu(context)
            contextMenu.populate(activity, frame, entry, num)
            contextMenu.open(frame)
            frame.addView(contextMenu)
            true
        }

        val numStr: String = String.format(Locale.getDefault(), "%d.", num + 1)
        val xRefStr: String = String.format(Locale.getDefault(), "See Also: %s",
            crossRefs?.joinToString(", ") { c -> c.form })

        defNum.text  = numStr
        defNum.setTextColor(ThemeManager.colTextPrimary)
        defLine.text = defs.joinToString(", ")
        defLine.setTextColor(ThemeManager.colTextPrimary)

        posNum.text  = numStr
        posLine.text = pos.joinToString(" / ")
        posLine.setTextColor(ThemeManager.colTextSecondary)
        posLine.setTypeface(posLine.typeface, Typeface.ITALIC)

        exampleJpNum.text  = numStr
        exampleJpLine.text = exampleJp
        exampleJpLine.setTextColor(ThemeManager.colTextPrimary)

        exampleEnNum.text  = numStr
        exampleEnLine.text = exampleEn
        exampleEnLine.setTextColor(ThemeManager.colTextPrimary)

        miscNum.text  = numStr
        miscLine.text = miscInfo?.joinToString(", ") ?: ""
        miscLine.setTextColor(ThemeManager.colTextPrimary)
        miscLine.setTypeface(miscLine.typeface, Typeface.ITALIC)

        crossRefsNum.text  = numStr
        crossRefsLine.text = xRefStr
        crossRefsLine.setTextColor(ThemeManager.colTextPrimary)

        // Example spacing
        val exampleSpacingV = 12
        if (hasExampleJP && hasExampleEN) {
            exampleJpCont.setPadding(0,exampleSpacingV,0,0)
            exampleEnCont.setPadding(0,0, 0,exampleSpacingV)
        } else if (hasExampleJP) {
            exampleJpCont.setPadding(0,exampleSpacingV,0,exampleSpacingV)
        } else if (hasExampleEN) {
            exampleEnCont.setPadding(0,exampleSpacingV,0,exampleSpacingV)
        }

        // Extra padding for bottom def
        if (num == entry.definitions.size - 1) {
            val padSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics)
            defCont.setPadding(0,0,0,padSize.toInt())
        }

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

        val exampleJpCont = findViewById<LinearLayout>(R.id.def_example_jp_container)
        val exampleEnCont = findViewById<LinearLayout>(R.id.def_example_en_container)

        if (showDetails) {
            posNum.visibility        = INVISIBLE
            posLine.visibility       = VISIBLE
            exampleJpCont.visibility = if (hasExampleJP) VISIBLE else GONE
            exampleJpNum.visibility  = if (hasExampleJP) INVISIBLE else GONE
            exampleJpLine.visibility = if (hasExampleJP) VISIBLE else GONE
            exampleEnCont.visibility = if (hasExampleEN) VISIBLE else GONE
            exampleEnNum.visibility  = if (hasExampleEN) INVISIBLE else GONE
            exampleEnLine.visibility = if (hasExampleEN) VISIBLE else GONE
            miscNum.visibility       = if (hasMiscInfo) INVISIBLE else GONE
            miscLine.visibility      = if (hasMiscInfo) VISIBLE else GONE
            crossRefsNum.visibility  = if (hasCrossRefs) INVISIBLE else GONE
            crossRefsLine.visibility = if (hasCrossRefs) VISIBLE else GONE
        } else {
            posNum.visibility        = GONE
            posLine.visibility       = GONE
            exampleJpCont.visibility = GONE
            exampleJpNum.visibility  = GONE
            exampleJpLine.visibility = GONE
            exampleEnCont.visibility = GONE
            exampleEnNum.visibility  = GONE
            exampleEnLine.visibility = GONE
            miscNum.visibility       = GONE
            miscLine.visibility      = GONE
            crossRefsNum.visibility  = GONE
            crossRefsLine.visibility = GONE
        }
    }
}