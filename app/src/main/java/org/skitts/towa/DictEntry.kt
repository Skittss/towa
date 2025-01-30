package org.skitts.towa

data class DictEntry(
    var primaryForm:    String             = "",
    var primaryReading: String             = "",
    var definitions:    List<List<String>> = List(3) { mutableListOf<String>() },
    var examples:       List<String>       = mutableListOf<String>()
) {}
