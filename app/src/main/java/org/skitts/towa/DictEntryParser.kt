package org.skitts.towa

import android.database.sqlite.SQLiteDatabase
import android.content.Context
import androidx.compose.ui.text.capitalize
import java.util.Locale

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
        val priorityCol       = dictCursor.getColumnIndex("priority")
        val primaryFormCol    = dictCursor.getColumnIndex("primary_form")
        val primaryReadingCol = dictCursor.getColumnIndex("primary_reading")
        val otherFormsCol     = dictCursor.getColumnIndex("other_forms")
        val otherReadingsCol  = dictCursor.getColumnIndex("other_readings")
        val definitionsCol    = dictCursor.getColumnIndex("definitions")
        val posCol            = dictCursor.getColumnIndex("pos_info")
        val fieldCol          = dictCursor.getColumnIndex("field_info")
        val dialectCol        = dictCursor.getColumnIndex("dialect_info")
        val miscCol           = dictCursor.getColumnIndex("misc_info")
        val examplesEnCol     = dictCursor.getColumnIndex("examples_en")
        val examplesJpCol     = dictCursor.getColumnIndex("examples_jp")
        val crossRefCol       = dictCursor.getColumnIndex("cross_refs")
        val jlptLevelCol      = dictCursor.getColumnIndex("jlpt_level")

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

            // Definitions
            val defStr: String               = dictCursor.getString(definitionsCol)
            val defListStr: List<String>     = defStr.split("␟")
            val defList: List<List<String>>  = defListStr.map{ d -> d.split("␞") }
            defList.map{ def -> entry.definitions.add(def) }

            // Examples
            entry.examplesEN = processExampleString(dictCursor.getString(examplesEnCol))
            entry.examplesJP = processExampleString(dictCursor.getString(examplesJpCol))

            // Parts of Speech
            entry.posInfo     = processInfoString(dictCursor.getString(posCol))

            val usages: MutableSet<String> = mutableSetOf()
            entry.posInfo.values.forEach { v-> usages.addAll(v) }
            entry.primaryUsages = usages.toList()

            // Def info
            entry.fieldInfo   = processInfoString(dictCursor.getString(fieldCol))
            entry.dialectInfo = processInfoString(dictCursor.getString(dialectCol))
            entry.miscInfo    = processInfoString(dictCursor.getString(miscCol), capitalize = true)
            entry.crossRefs = processCrossRefs(dictCursor.getString(crossRefCol))

            // Main Forms & Readings
            entry.primaryForm    = dictCursor.getString(primaryFormCol)
            entry.primaryReading = dictCursor.getString(primaryReadingCol)

            // Other forms & readings
            val otherFormsStr: String = dictCursor.getString(otherFormsCol)
            val otherFormsList: MutableList<String> = mutableListOf()
            otherFormsStr.split("␟")
                .filter{ f -> f.isNotEmpty() }
                .map{ f -> otherFormsList.add(f) }
            entry.otherForms = otherFormsList

            val otherReadingsStr: String = dictCursor.getString(otherReadingsCol)
            val otherReadingsList: MutableList<String>  = mutableListOf()
            otherReadingsStr.split("␟")
                .filter{ r-> r.isNotEmpty() }
                .map{ r -> otherReadingsList.add(r) }

            entry.otherReadings  = otherReadingsList

            val readings: List<String> = listOf(entry.primaryReading) + entry.otherReadings
            entry.intonations = getIntonations(dictionary, entry.primaryForm, readings)

            val forms: List<String> = listOf(entry.primaryForm) + entry.otherForms
            entry.furigana= getFurigana(dictionary, forms, readings)

            // Priority / Common word
            val priority: Int = dictCursor.getInt(priorityCol)
            entry.common = priority < 2
            entry.jlptLevel = dictCursor.getInt(jlptLevelCol)

            if (exactFormMatch) exactFormEntries.add(entry)
            else if (exactReadingMatch) exactReadingEntries.add(entry)
            else similarEntries.add(entry)
        }

        dictCursor.close()

        return exactFormEntries.plus(exactReadingEntries).plus(similarEntries)
    }

    private fun getFurigana(
        dictionary: SQLiteDatabase,
        forms: List<String>,
        readings: List<String>,
    ): Map<Pair<String, String>, String> {
        val furiganaMap: MutableMap<Pair<String, String>, String> = mutableMapOf()
        val formsStr: String = forms.joinToString(",") { f -> "\'${f}\'" }
        val readingsStr: String = readings.joinToString(",") { r -> "\'${r}\'" }

        val furiganaLookupStr: String =
            "SELECT * FROM towafurigana WHERE primary_form IN (${formsStr}) AND reading IN (${readingsStr})"
        val furiganaCursor = dictionary.rawQuery(furiganaLookupStr, null)

        val formCol     = furiganaCursor.getColumnIndex("primary_form")
        val readingCol  = furiganaCursor.getColumnIndex("reading")
        val furiganaCol = furiganaCursor.getColumnIndex("furigana_encoding")
        while (furiganaCursor.moveToNext()) {
            val form     = furiganaCursor.getString(formCol)
            val reading  = furiganaCursor.getString(readingCol)
            val furigana = furiganaCursor.getString(furiganaCol)
            furiganaMap[Pair(form, reading)] = furigana
        }
        furiganaCursor.close()

        return furiganaMap
    }

    private fun getIntonations(
        dictionary: SQLiteDatabase,
        primaryForm: String,
        readings: List<String>
    ): Map<String, List<Int>> {
        val intonationMap: MutableMap<String, List<Int>> = mutableMapOf()
        val readingsStr: String = readings.joinToString(",") { r -> "\'${r}\'" }

        val intonationLookupStr: String =
            "SELECT reading, intonation_encoding FROM towaintonation WHERE primary_form = '${primaryForm}' AND reading IN (${readingsStr})"
        val intonationCursor = dictionary.rawQuery(intonationLookupStr, null)

        val intonationCol        = intonationCursor.getColumnIndex("intonation_encoding")
        val intonationReadingCol = intonationCursor.getColumnIndex("reading")
        while (intonationCursor.moveToNext()) {
            val encodingStr = intonationCursor.getString(intonationCol)
            val readingStr  = intonationCursor.getString(intonationReadingCol)

            val intonations: List<Int> = encodingStr.split(",").map{ d -> d.toInt() }
            intonationMap[readingStr] = intonations
        }
        intonationCursor.close()

        return intonationMap
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

    private fun processInfoString(info: String, cvtCode: Boolean = true, capitalize: Boolean = false): Map<Int, List<String>> {
        if (info.isEmpty()) return mapOf()

        val infoMapEntries: List<String> = info.split("␟")
        val infoMap: MutableMap<Int, List<String>> = mutableMapOf()
        infoMapEntries.map{ e ->
            val entry = e.split("␞")
            val key: Int = entry[0].toInt()
            val vals: List<String> = entry.subList(1, entry.size).map { c ->
                var str: String = ""
                if (cvtCode) str = code2en(c)
                else         str = c

                if (capitalize) str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                else it.toString() } else str
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