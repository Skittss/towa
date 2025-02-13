package org.skitts.towa

data class DictEntry(
    var id: Int                                     = -1,
    var primaryForm: String                         = "",
    var primaryFormWithFurigana: String?            = null,
    var furigana: Map<Pair<String, String>, String> = mapOf(),
    var primaryReading: String                      = "",
    var intonation: List<Int>                       = mutableListOf<Int>(),
    var definitions: MutableList<List<String>>      = mutableListOf<List<String>>(),
    var primaryUsages: List<String>                 = mutableListOf<String>(),
    var posInfo: Map<Int, List<String>>             = mapOf(),
    var fieldInfo: Map<Int, List<String>>           = mapOf(),
    var dialectInfo: Map<Int, List<String>>         = mapOf(),
    var miscInfo: Map<Int, List<String>>            = mapOf(),
    var examplesEN: Map<Int, String>                = mapOf(),
    var examplesJP: Map<Int, String>                = mapOf(),
    var crossRefs: Map<Int, List<CrossRef>>         = mapOf()
) {}
