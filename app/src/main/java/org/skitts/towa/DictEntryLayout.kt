package org.skitts.towa

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView

class DictEntryLayout (
    context: Context,
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.towa_overlay, this)
    }

    public fun populate(entry: DictEntry) {
        val primaryForm = findViewById<TextView>(R.id.primary_form)
        val pitches     = findViewById<TextView>(R.id.pitches)

        primaryForm.text = entry.primaryForm
        pitches.text     = entry.primaryReading
    }
}