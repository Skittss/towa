package org.skitts.towa

fun sanitizeInput(text: String): String {
    var res: String = text
    res = res.replace("\\s".toRegex(), "")
    res = sanitizerMap.entries.fold(res) { acc, (k, v) -> acc.replace(k, v) }

    return res
}

private val sanitizerMap: Map<String, String> = mapOf(
    "0" to "０",
    "1" to "１",
    "2" to "２",
    "3" to "３",
    "4" to "４",
    "5" to "５",
    "6" to "６",
    "7" to "７",
    "8" to "８",
    "9" to "９",
    "!" to "！",
    "?" to "？"
)