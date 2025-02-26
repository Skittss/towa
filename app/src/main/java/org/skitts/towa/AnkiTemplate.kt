package org.skitts.towa

val ANKI_TEMPLATE_FIELDS: Array<String> =
    arrayOf(
        "primary_form",
        "primary_form_with_furigana",
        "readings",
        "definitions",
        "parts_of_speech",
        "example_jp",
        "example_en",
    ) // TODO: Audio

const val ANKI_TEMPLATE_FRONT = """
<span style="font-size: 50px;  ">{{primary_form}}</span>
"""

const val ANKI_TEMPLATE_BACK = """
<span style="font-size: 50px;  ">{{furigana:primary_form_with_furigana}}</span>
 
<hr id=answer>
 
<span style="font-size: 22px; ">{{definitions}}</span>
<br>
<span style="font-size: 35px; ">{{readings}}</span>
<br>
<span style="font-size: 14px; ">{{parts_of_speech}}</span>
<br>
<span style="font-size: 40px; ">{{example_jp}}</span>
<br>
<span style="font-size: 25px; ">{{example_en}}</span>
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