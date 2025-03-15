package org.skitts.towa

val ANKI_TEMPLATE_FIELDS: Array<String> =
    arrayOf(
        "primary_form",
        "primary_form_with_furigana",
        "primary_reading",
        "definitions",
        "parts_of_speech",
        "example_jp",
        "example_en",
        "audio_example",
        "other_forms",
        "other_readings"
    )

const val INTONATION_BORDER_STYLE_THIN = "1px solid cyan"
const val INTONATION_LOW_HIGH_TEMPLATE_THIN = """<span style="border-bottom: $INTONATION_BORDER_STYLE_THIN; border-right: $INTONATION_BORDER_STYLE_THIN; ">{}</span>"""
const val INTONATION_HIGH_LOW_TEMPLATE_THIN = """<span style="border-top: $INTONATION_BORDER_STYLE_THIN; border-right: $INTONATION_BORDER_STYLE_THIN; ">{}</span>"""
const val INTONATION_LOW_TEMPLATE_THIN = """<span style="border-bottom: $INTONATION_BORDER_STYLE_THIN; ">{}</span>"""
const val INTONATION_HIGH_TEMPLATE_THIN = """<span style="border-top: $INTONATION_BORDER_STYLE_THIN; ">{}</span>"""

const val INTONATION_BORDER_STYLE = "5px solid cyan"
const val INTONATION_LOW_HIGH_TEMPLATE = """<span style="border-bottom: $INTONATION_BORDER_STYLE; border-right: $INTONATION_BORDER_STYLE; ">{}</span>"""
const val INTONATION_HIGH_LOW_TEMPLATE = """<span style="border-top: $INTONATION_BORDER_STYLE; border-right: $INTONATION_BORDER_STYLE; ">{}</span>"""
const val INTONATION_LOW_TEMPLATE = """<span style="border-bottom: $INTONATION_BORDER_STYLE; ">{}</span>"""
const val INTONATION_HIGH_TEMPLATE = """<span style="border-top: $INTONATION_BORDER_STYLE; ">{}</span>"""

const val ANKI_TEMPLATE_FRONT = """
<span style="font-size: 50px;  ">{{primary_form}}</span>
"""

const val ANKI_TEMPLATE_BACK = """
<span style="font-size: 50px;  ">{{furigana:primary_form_with_furigana}}</span>
 
<hr id=answer>
 
<span style="font-size: 35px; ">{{audio_example}} {{primary_reading}}</span>
<br>
<span style="font-size: 14px; ">{{parts_of_speech}}</span>
<br>
<span style="font-size: 22px; ">{{definitions}}</span>
<br>
<br>
<span style="font-size: 25px; ">{{example_jp}}</span>
<br>
<span style="font-size: 25px; ">{{example_en}}</span>
<br>
<br>
<span style="font-size: 14px; ">{{other_forms}}</span>
<br>
<span style="font-size: 14px; ">{{other_readings}}</span>
<br>
"""

const val ANKI_TEMPLATE_CSS = """
.card {
 font-family: arial;
 font-size: 25px;
 text-align: center;
 color: White;
 background-color: Black;
}
"""