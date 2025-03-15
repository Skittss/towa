package org.skitts.towa

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import java.util.Locale

class IntonationView (
    context: Context,
) : FlexboxLayout(context) {
    init {
        flexDirection = FlexDirection.ROW
        flexWrap = FlexWrap.WRAP
    }

    fun populate(reading: String, intonations: List<Int>) {
        val rSpacing: Int = 30

        if (intonations.isEmpty()) {
            val noIntonation = TextView(context)
            noIntonation.text = reading
            noIntonation.setTextColor(ThemeManager.colTextPrimary)
            noIntonation.setPadding(0, 0, rSpacing, 0)
            addView(noIntonation)
            return
        }

        for (i in intonations) {
            // Mora 0 assumed low unless intonation is 1: LH -> HL
            // Mora out of string idx range indicates intonation down-tick for following particle.
            // Once intonations go H -> L, they do not go back up

            val segmentRpad: Int = 7

            val intonationCont = LinearLayout(context)
            intonationCont.orientation = HORIZONTAL
            intonationCont.setPadding(0,0,0, 8)

            if (i != 1) {
                val lhText = reading.substring(0, 1)
                val lh = TextView(context)
                lh.text = lhText
                lh.setTextColor(ThemeManager.colTextPrimary)
                lh.background = getColouredIntonationBackground(R.drawable.intonation_border_low2high)
                lh.setPadding(0, 0, segmentRpad, 0)
                intonationCont.addView(lh)

                val needsLowSegment: Boolean = i != 0 && i != reading.length
                val highLowSegmentEnd: Int = if (i == 0) reading.length else i
                val highPitchResource = if (i == 0)
                    R.drawable.intonation_border_high else
                    R.drawable.intonation_border_high2low

                val hText = reading.substring(1, highLowSegmentEnd)
                val h = TextView(context)
                h.text = hText
                h.setTextColor(ThemeManager.colTextPrimary)
                h.background = getColouredIntonationBackground(highPitchResource)
                h.setPadding(0, 0, segmentRpad, 0)
                intonationCont.addView(h)

                if (needsLowSegment) {
                    val lText = reading.substring(highLowSegmentEnd, reading.length)
                    val l = TextView(context)
                    l.text = lText
                    l.setTextColor(ThemeManager.colTextPrimary)
                    l.background = getColouredIntonationBackground(R.drawable.intonation_border_low)
                    l.setPadding(0, 0, segmentRpad, 0)
                    intonationCont.addView(l)
                }
            } else {
                val hlText = reading.substring(0, 1)
                val hl = TextView(context)
                hl.text = hlText
                hl.setTextColor(ThemeManager.colTextPrimary)
                hl.background = getColouredIntonationBackground(R.drawable.intonation_border_high2low)
                hl.setPadding(0, 0, segmentRpad, 0)
                intonationCont.addView(hl)

                val lText = reading.substring(1, reading.length)
                val l = TextView(context)
                l.text = lText
                l.setTextColor(ThemeManager.colTextPrimary)
                l.background = getColouredIntonationBackground(R.drawable.intonation_border_low)
                l.setPadding(0, 0, segmentRpad, 0)
                intonationCont.addView(l)
            }

            val spacing = TextView(context)
            spacing.text = ""
            spacing.setPadding(0, 0, rSpacing, 0)
            intonationCont.addView(spacing)

            addView(intonationCont)
        }
    }

    private fun getColouredIntonationBackground(@DrawableRes resid: Int): Drawable {
        val tagDrawable     = getDrawable(context, resid)!!
        val wrappedDrawable = DrawableCompat.wrap(tagDrawable)
        DrawableCompat.setTint(wrappedDrawable, ThemeManager.colAccentMed)

        return wrappedDrawable
    }
}