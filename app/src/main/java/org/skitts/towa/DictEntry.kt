package org.skitts.towa

data class DictEntry(
    var id: Int                                     = -1,
    var primaryForm: String                         = "",
    var otherForms: List<String>                    = mutableListOf(),
    var furigana: Map<Pair<String, String>, String> = mutableMapOf(),
    var primaryReading: String                      = "",
    var otherReadings: List<String>                 = mutableListOf(),
    var intonations: Map<String, List<Int>>         = mutableMapOf(),
    var definitions: MutableList<List<String>>      = mutableListOf(),
    var primaryUsages: List<String>                 = mutableListOf(),
    var posInfo: Map<Int, List<String>>             = mapOf(),
    var fieldInfo: Map<Int, List<String>>           = mapOf(),
    var dialectInfo: Map<Int, List<String>>         = mapOf(),
    var miscInfo: Map<Int, List<String>>            = mapOf(),
    var examplesEN: Map<Int, String>                = mapOf(),
    var examplesJP: Map<Int, String>                = mapOf(),
    var crossRefs: Map<Int, List<CrossRef>>         = mapOf(),
    var common: Boolean                             = false,
    var jlptLevel: Int                              = 0
) {}
