package org.skitts.towa

import android.content.Context
import android.widget.LinearLayout
import android.widget.LinearLayout.inflate
import android.widget.TextView
import java.util.Locale

class DictEntryInfoLineLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry_def_line, this)
    }

    public fun populate(num: Int, defs: List<String>) {
        val defNum      = findViewById<TextView>(R.id.def_defs_number)
        val defLine     = findViewById<TextView>(R.id.def_defs)

        defNum.text = String.format(Locale.getDefault(), "%d.", num)
        defLine.text = defs.joinToString(", ");
    }
}