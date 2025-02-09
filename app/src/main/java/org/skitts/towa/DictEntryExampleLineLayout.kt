package org.skitts.towa

import android.content.Context
import android.widget.LinearLayout
import android.widget.LinearLayout.inflate
import android.widget.TextView
import java.util.Locale

class DictEntryExampleLineLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_dict_entry_example_line, this)
    }

    public fun populate(num: Int, example: String) {
        val defNum      = findViewById<TextView>(R.id.def_example_num)
        val defExample  = findViewById<TextView>(R.id.def_example)

        defNum.text = String.format(Locale.getDefault(), "%d.", num)
        defExample.text = example
    }
}