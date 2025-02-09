package org.skitts.towa

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Locale

class IntonationView (
    context: Context,
) : LinearLayout(context) {
    init {}

    public fun populate(reading: String, intonations: List<Int>) {
        val rSpacing: Int = 40

        if (intonations.isEmpty()) {
            val noIntonation = TextView(context)
            noIntonation.text = reading
            noIntonation.setPadding(0, 0, rSpacing, 0)
            addView(noIntonation)
            return
        }

        for (i in intonations) {
            // Mora 0 assumed low unless intonation is 1: LH -> HL
            // Mora out of string idx range indicates intonation down-tick for following particle.
            // Once intonations go H -> L, they do not go back up

            val segmentRpad: Int = 7

            if (i != 1) {
                val lhText = reading.substring(0, 1)
                val lh = TextView(context)
                lh.text = lhText
                lh.setBackgroundResource(R.drawable.intonation_border_low2high)
                lh.setPadding(0, 0, segmentRpad, 0)
                addView(lh)

                val needsLowSegment: Boolean = i != 0 && i != reading.length
                val highLowSegmentEnd: Int = if (i == 0) reading.length else i
                val highPitchResource = if (i == 0)
                    R.drawable.intonation_border_high else
                    R.drawable.intonation_border_high2low

                val hText = reading.substring(1, highLowSegmentEnd)
                val h = TextView(context)
                h.text = hText
                h.setBackgroundResource(highPitchResource)
                h.setPadding(0, 0, segmentRpad, 0)
                addView(h)

                if (needsLowSegment) {
                    val lText = reading.substring(highLowSegmentEnd, reading.length)
                    val l = TextView(context)
                    l.text = lText
                    l.setBackgroundResource(R.drawable.intonation_border_low)
                    l.setPadding(0, 0, segmentRpad, 0)
                    addView(l)
                }
            } else {
                val hlText = reading.substring(0, 1)
                val hl = TextView(context)
                hl.text = hlText
                hl.setBackgroundResource(R.drawable.intonation_border_high2low)
                hl.setPadding(0, 0, segmentRpad, 0)
                addView(hl)

                val lText = reading.substring(1, reading.length)
                val l = TextView(context)
                l.text = lText
                l.setBackgroundResource(R.drawable.intonation_border_low)
                l.setPadding(0, 0, segmentRpad, 0)
                addView(l)
            }

            val spacing = TextView(context)
            spacing.text = ""
            spacing.setPadding(0, 0, rSpacing, 0)
            addView(spacing)
        }

//        val view = TextView(context)
//        view.text = String.format(Locale.getDefault(), "%s", reading)
//        view.setBackgroundResource(R.drawable.intonation_border_low)
//
//        addView(view)
    }
}