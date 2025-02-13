package org.skitts.towa

import android.database.sqlite.SQLiteDatabase
import android.content.Context

class DictEntryParser (
    private val context: Context
) {
    suspend fun queryDictionaryEntries(text: String) : List<DictEntry> {
        val dictionary: SQLiteDatabase = openDB()

        // Exact: ID -> Match on Form / reading (true / false)
        val allMatches: MutableSet<Int> = mutableSetOf()
        val exactMatches: MutableMap<Int, Boolean> = mutableMapOf()

        val exactLookupStr: String =
            "SELECT form_ids, primary_match_flags FROM towalookup WHERE form_or_reading = '${text}'"
        val exactLookupCursor = dictionary.rawQuery(exactLookupStr, null)

        val exactIdCol         = exactLookupCursor.getColumnIndex("form_ids")
        val exactMatchFlagsCol = exactLookupCursor.getColumnIndex("primary_match_flags")
        while (exactLookupCursor.moveToNext()) {
            val idEntry = exactLookupCursor.getString(exactIdCol)
            val matchFlagEntry = exactLookupCursor.getString(exactMatchFlagsCol)

            val ids: List<Int> = idEntry.split(",").map{ i -> i.toInt() }
            val matchFlags: List<Boolean> = matchFlagEntry.split(",").map{ m -> m.toInt() == 1 }
            ids.forEachIndexed{ i, id -> exactMatches[id] = matchFlags[i] }
            allMatches.addAll(ids)
        }
        exactLookupCursor.close()

        val similarLookupStr: String =
            "SELECT form_ids, primary_match_flags FROM towalookup WHERE form_or_reading LIKE '${text}${"_%"}'"
        val similarLookupCursor = dictionary.rawQuery(similarLookupStr, null)

        val similarIdCol = similarLookupCursor.getColumnIndex("form_ids")
        while (similarLookupCursor.moveToNext()) {
            val idEntry = similarLookupCursor.getString(similarIdCol)

            val ids: List<Int> = idEntry.split(",").map{ i -> i.toInt() }
            allMatches.addAll(ids)
        }
        similarLookupCursor.close()

        val queryString: String =
            "SELECT * FROM towadict WHERE form_id IN (${allMatches.toList().joinToString(",")}) ORDER BY priority ASC LIMIT 100"

        val dictCursor = dictionary.rawQuery(queryString,null)

        val formIdCol         = dictCursor.getColumnIndex("form_id")
        val primaryFormCol    = dictCursor.getColumnIndex("primary_form")
        val primaryReadingCol = dictCursor.getColumnIndex("primary_reading")
        val definitionsCol    = dictCursor.getColumnIndex("definitions")
        val posCol            = dictCursor.getColumnIndex("pos_info")
        val fieldCol          = dictCursor.getColumnIndex("field_info")
        val dialectCol        = dictCursor.getColumnIndex("dialect_info")
        val miscCol           = dictCursor.getColumnIndex("misc_info")
        val examplesEnCol     = dictCursor.getColumnIndex("examples_en")
        val examplesJpCol     = dictCursor.getColumnIndex("examples_jp")
        var crossRefCol       = dictCursor.getColumnIndex("cross_refs")

        val exactFormEntries: MutableList<DictEntry> = mutableListOf<DictEntry>()
        val exactReadingEntries: MutableList<DictEntry> = mutableListOf<DictEntry>()
        val similarEntries: MutableList<DictEntry> = mutableListOf<DictEntry>()
        while (dictCursor.moveToNext()) {
            val entry = DictEntry()

            val formId = dictCursor.getInt(formIdCol)
            entry.id = formId
            // TODO: Might need to additionally weight exact form + exact reading above just exact form (e.g. 格)
            val exactFormMatch = exactMatches[entry.id]?.equals(true) ?: false
            val exactReadingMatch = exactMatches[entry.id]?.equals(false) ?: false

            val defStr: String               = dictCursor.getString(definitionsCol)
            val defListStr: List<String>     = defStr.split("␟")
            val defList: List<List<String>>  = defListStr.map{ d -> d.split("␞") }

            entry.examplesEN = processExampleString(dictCursor.getString(examplesEnCol))
            entry.examplesJP = processExampleString(dictCursor.getString(examplesJpCol))

            entry.posInfo     = processInfoString(dictCursor.getString(posCol))

            val usages: MutableSet<String> = mutableSetOf()
            entry.posInfo.values.forEach { v-> usages.addAll(v) }
            entry.primaryUsages = usages.toList()

            entry.fieldInfo   = processInfoString(dictCursor.getString(fieldCol))
            entry.dialectInfo = processInfoString(dictCursor.getString(dialectCol))
            entry.miscInfo    = processInfoString(dictCursor.getString(miscCol))

            entry.primaryForm    = dictCursor.getString(primaryFormCol)
            entry.primaryReading = dictCursor.getString(primaryReadingCol)

            entry.crossRefs = processCrossRefs(dictCursor.getString(crossRefCol))

            val furiganaCursor = dictionary.rawQuery(
                "SELECT furigana_encoding FROM towafurigana WHERE primary_form = ? AND reading = ?",
                arrayOf(entry.primaryForm, entry.primaryReading)
            )
            val furiganaCol = furiganaCursor.getColumnIndex("furigana_encoding")
            if (furiganaCursor.moveToNext()) {
                entry.primaryFormWithFurigana = furiganaCursor.getString(furiganaCol)
            }
            furiganaCursor.close()

            val intonationCursor = dictionary.rawQuery(
                "SELECT intonation_encoding FROM towaintonation WHERE primary_form = ? AND reading = ?",
                arrayOf(entry.primaryForm, entry.primaryReading)
            )
            val intonationCol = intonationCursor.getColumnIndex("intonation_encoding")
            if (intonationCursor.moveToNext()) {
                val encodingStr = intonationCursor.getString(intonationCol)
                entry.intonation = encodingStr.split(",").map{ d -> d.toInt() }
            }
            intonationCursor.close()

            defList.map{ def -> entry.definitions.add(def) }

            if (exactFormMatch) exactFormEntries.add(entry)
            else if (exactReadingMatch) exactReadingEntries.add(entry)
            else similarEntries.add(entry)
        }

        dictCursor.close()

        return exactFormEntries.plus(exactReadingEntries).plus(similarEntries)
    }

    private fun processExampleString(examplesStr: String): Map<Int, String> {
        if (examplesStr.isEmpty()) return mapOf()

        val examplesEntries: List<String> = examplesStr.split("␟")
        val examplesMap: MutableMap<Int, String> = mutableMapOf()
        examplesEntries.map{ e ->
            val kv = e.split("␞")
            examplesMap.put(kv[0].toInt(), kv[1])
        }

        return examplesMap
    }

    private fun processInfoString(info: String, cvtCode: Boolean = true): Map<Int, List<String>> {
        if (info.isEmpty()) return mapOf()

        val infoMapEntries: List<String> = info.split("␟")
        val infoMap: MutableMap<Int, List<String>> = mutableMapOf()
        infoMapEntries.map{ e ->
            val entry = e.split("␞")
            val key: Int = entry[0].toInt()
            val vals: List<String> = entry.subList(1, entry.size).map { c ->
                if (cvtCode) code2en(c) else c
            }
            infoMap.put(key, vals)
        }

        return infoMap
    }

    private fun processCrossRefs(xref: String): Map<Int, List<CrossRef>> {
        if (xref.isEmpty()) return mapOf()

        val xrefMapEntries: List<String> = xref.split("␟")
        val xrefMap: MutableMap<Int, List<CrossRef>> = mutableMapOf()
        xrefMapEntries.map{ e ->
            val entry = e.split("␞")
            val key: Int = entry[0].toInt()

            val vals: List<CrossRef> = entry.subList(1, entry.size).map { c ->
                processXref(c)
            }
            xrefMap.put(key, vals)
        }

        return xrefMap
    }

    private fun processXref(xref: String): CrossRef {
        val crossRef = CrossRef()
        val info: List<String> = xref.split("・")
        crossRef.form = info[0]

        if (info.size > 1) {
            var defIdxFirst = false
            val val1: Int? = info[1].toIntOrNull()
            if (val1 != null) {
                defIdxFirst = true
                crossRef.defIdx = info[1].toInt()
            } else {
                crossRef.reading = info[1]
            }

            if (info.size > 2) {
                if (defIdxFirst) {
                    crossRef.reading = info[2]
                } else {
                    crossRef.defIdx = info[2].toInt()
                }
            }
        }

        return crossRef
    }

    private suspend fun openDB() : SQLiteDatabase {
        val dbHelper = TowaDatabaseHelper(context)
        dbHelper.initializeDB()

        return dbHelper.readableDatabase
    }


}