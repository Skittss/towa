from dataclasses import dataclass, field
import os
import sqlite3
import xml.etree.ElementTree as ET
import json
import towa_jmdict_entity_map as towa_enmap
import math

BASE_DIR = os.path.dirname(__file__)
DB_PATH  = os.path.join(BASE_DIR, "../app/src/main/assets/databases/towa.db")

# TODO: Two ways to look up a word:
#         - Form e.g. 私
#         - Reading e.g. わたし
#       As such, need two tables:
#         - Reading/Form -> Form IDs & Primary match flag
#         - Form IDs -> Definitions
#       
#       Weight lookups with 3 Match Flags:
#          2 - Exact match on primary form
#          1 - Exact match on primary form, but with alternate kana (e.g. half width, or katakana when it should be hiragana)
#          0 - Alternative reading or Form
# 

SQL_FURIGANA_TABLE_NAME   = "towafurigana"
SQL_INTONATION_TABLE_NAME = "towaintonation"
SQL_LOOKUP_TABLE_NAME     = "towalookup"
SQL_DICT_TABLE_NAME       = "towadict"

# This db is scuffed and not normalized at all - don't hurt me!
SQL_CREATE_FURIGANA_TABLE_COMMAND = f'''CREATE TABLE {SQL_FURIGANA_TABLE_NAME}
    (primary_form text NOT NULL, reading text NOT NULL, furigana_encoding text NOT NULL, PRIMARY KEY(primary_form, reading))'''
SQL_CREATE_INTONATION_TABLE_COMMAND = f'''CREATE TABLE {SQL_INTONATION_TABLE_NAME}
    (primary_form text NOT NULL, reading text NOT NULL, intonation_encoding text NOT NULL, PRIMARY KEY(primary_form, reading))'''
SQL_CREATE_LOOKUP_TABLE_COMMAND   = f'''CREATE TABLE {SQL_LOOKUP_TABLE_NAME}
    (form_or_reading text NOT NULL, form_ids text NOT NULL, primary_match_flags text)'''
SQL_CREATE_DICT_TABLE_COMMAND     = f'''CREATE TABLE {SQL_DICT_TABLE_NAME} 
    (form_id INTEGER NOT NULL, 
    priority integer,
    primary_form text, primary_reading text, 
    other_forms text, other_readings text, 
    definitions text, 
    pos_info text, field_info text, dialect_info text, misc_info text, 
    examples_jp text, examples_en text,
    cross_refs text)'''

AUDIO_KANJI_ALIVE_PATH       = os.path.join(BASE_DIR, "audio\\kanji_alive")
AUDIO_KANJI_ALIVE_INDEX_PATH = os.path.join(BASE_DIR, "audio\\kanji_alive_index.csv")
AUDIO_TOFUGU_PATH            = os.path.join(BASE_DIR, "audio\\tofugu")

FURIGANA_JMDICT_PATH         = os.path.join(BASE_DIR, "furigana\\JMdictFurigana.json")
FURIGANA_JMEDICT_PATH        = os.path.join(BASE_DIR, "furigana\\JMedictFurigana.json")

INTONATIONS_KANJIUM_PATH     = os.path.join(BASE_DIR, "intonation\\intonation.json")

WORDS_EN_JMDICT_PATH         = os.path.join(BASE_DIR, "words\\JMdict.xml")
WORDS_EN_JMNEDICT_PATH       = os.path.join(BASE_DIR, "words\\JMnedict.xml")

ELEMENT_TREE_XML_NAMESPACE_PREFIX  = '{http://www.w3.org/XML/1998/namespace}'

### ---- JMDict Parsing ----------------------------------------------------------------------------

@dataclass
class JMdictReadingElement:
    kanaReading: str  = ""
    noKanjiFlag: bool = False
    readingInfo: str  = ""

@dataclass
class JMdictKanjiElement:
    kanji: str    = ""

@dataclass
class JMdictSenseElement:
    definitions: list[str] = field(default_factory=list)
    exampleJP:   str = "" # Only need one example per def really
    exampleEN:   str = ""
    posInfo:     list[str] = field(default_factory=list)
    fieldInfo:   list[str] = field(default_factory=list)
    dialectInfo: list[str] = field(default_factory=list)
    miscInfo:    list[str] = field(default_factory=list)
    crossRefs:   list[str] = field(default_factory=list)

@dataclass
class JMdictEntry:
    sequenceID:  int                        = -1
    priority:    int                        = math.inf
    kanjiInfo:   list[JMdictKanjiElement]   = field(default_factory=list)
    readingInfo: list[JMdictReadingElement] = field(default_factory=list)
    senseInfo:   list[JMdictSenseElement]   = field(default_factory=list)

@dataclass
class TowaDictEntry:
    id:             int
    priority:       int
    primaryForm:    str
    otherForms:     list[str]
    primaryReading: str 
    otherReadings:  list[str]
    definitions:    list[str]
    posInfo:        dict[int, list[str]]
    fieldInfo:      dict[int, list[str]]
    dialectInfo:    dict[int, list[str]]
    miscInfo:       dict[int, list[str]]
    examplesJP:     dict[int, str]
    examplesEN:     dict[int, str]
    crossRefs:      dict[int, list[str]]

def processJMdictPriority(pri: str) -> int:
    match pri:
        case "news1" | "ichi1" | "gai1" | "spec1":
            return 24
        case "news2" | "ichi2" | "gai2" | "spec2":
            return 48
        case _:
            # nfxx
            num = int(pri.removeprefix("nf"))
            return num
    
    return -1

def processJMdictReadingElement(r_ele: ET.Element) -> tuple[JMdictReadingElement, int]:
    readingElement = JMdictReadingElement()
    priority = math.inf

    for elmt in r_ele:
        elmtType: str = elmt.tag
        match(elmtType):
            case "reb":
                readingElement.kanaReading = elmt.text
            case "re_nokanji":
                readingElement.noKanjiFlag = True
            case "re_inf":
                readingElement.readingInfo = elmt.text
            case "re_pri":
                p = processJMdictPriority(elmt.text)
                priority = min(priority, p)

    return (readingElement, priority)

def processJMdictKanjiElement(k_ele: ET.Element) -> tuple[JMdictKanjiElement, int]:
    kanjiElement = JMdictKanjiElement()
    priority = math.inf

    for elmt in k_ele:
        elmtType: str = elmt.tag
        match(elmtType):
            case "keb":
                kanjiElement.kanji = elmt.text
            case "ke_inf":
                pass
            case "ke_pri":
                p = processJMdictPriority(elmt.text)
                priority = min(priority, p)

    return (kanjiElement, priority)

def processJMdictExampleElement(example: ET.Element) -> tuple[str, str]:
    sentenceJP: str = ""
    sentenceEN: str = ""

    for elmt in example:
        elmtType: str = elmt.tag
        match(elmtType):
            case "ex_sent":
                if elmt.attrib[f"{ELEMENT_TREE_XML_NAMESPACE_PREFIX}lang"] == "jpn":
                   sentenceJP = elmt.text
                elif elmt.attrib[f"{ELEMENT_TREE_XML_NAMESPACE_PREFIX}lang"] == "eng":
                   sentenceEN = elmt.text 

    return (sentenceJP, sentenceEN)

def processJMdictSenseElement(sense: ET.Element) -> JMdictSenseElement:
    senseElement = JMdictSenseElement()

    for elmt in sense:
        elmtType: str = elmt.tag
        match(elmtType):
            case "gloss":
                definition = elmt.text
                senseElement.definitions.append(definition)
            case "example":
                senseElement.exampleJP, senseElement.exampleEN = processJMdictExampleElement(elmt)
            case "pos":
                # TODO: Order these by importance / commonality?
                senseElement.posInfo.append(towa_enmap.text2entity(elmt.text))
            case "field":
                senseElement.fieldInfo.append(towa_enmap.text2entity(elmt.text))
            case "dial":
                senseElement.dialectInfo.append(towa_enmap.text2entity(elmt.text))
            case "misc":
                senseElement.miscInfo.append(towa_enmap.text2entity(elmt.text))
            case "xref":
                senseElement.crossRefs.append(elmt.text)

    return senseElement

def JMdict2TowaEntry(entry: JMdictEntry) -> TowaDictEntry:
    # Process Required fields (>= 1)
    primaryReading: str                  = entry.readingInfo[0].kanaReading
    priority:       int                  = 2147483647 if entry.priority == math.inf else entry.priority
    otherReadings:  list[str]            = [r.kanaReading for r in entry.readingInfo[1:]] 
    definitions:    list[list[str]]      = [s.definitions for s in entry.senseInfo]
    examplesJP:     dict[int, str]       = {i: s.exampleJP for i, s in enumerate(entry.senseInfo) if len(s.exampleJP) > 0}
    examplesEN:     dict[int, str]       = {i: s.exampleEN for i, s in enumerate(entry.senseInfo) if len(s.exampleEN) > 0}
    posInfo:        dict[int, list[str]] = {i: s.posInfo for i, s in enumerate(entry.senseInfo) if len(s.posInfo) > 0} 
    fieldInfo:      dict[int, list[str]] = {i: s.fieldInfo for i, s in enumerate(entry.senseInfo) if len(s.fieldInfo) > 0}
    dialectInfo:    dict[int, list[str]] = {i: s.dialectInfo for i, s in enumerate(entry.senseInfo) if len(s.dialectInfo) > 0}
    miscInfo:       dict[int, list[str]] = {i: s.miscInfo for i, s in enumerate(entry.senseInfo) if len(s.miscInfo) > 0}
    crossRefs:      dict[int, list[str]] = {i: s.crossRefs for i, s in enumerate(entry.senseInfo) if len(s.crossRefs) > 0}

    # Process Optional fields (>= 0)
    hasKanji = len(entry.kanjiInfo) > 0
    primaryForm:    str       = entry.kanjiInfo[0].kanji if hasKanji else primaryReading
    otherForms:     list[str] = [k.kanji for k in entry.kanjiInfo[1:]]

    id: int = entry.sequenceID

    return TowaDictEntry(
        id,
        priority,
        primaryForm,
        otherForms,
        primaryReading,
        otherReadings,
        definitions,
        posInfo,
        fieldInfo,
        dialectInfo,
        miscInfo,
        examplesJP,
        examplesEN,
        crossRefs
    )

def processJMdictEntry(entry: ET.Element) -> TowaDictEntry:
    entryInfo = JMdictEntry()

    for element in entry:
        elementType: str = element.tag
        match(elementType):
            case "ent_seq":
                entryInfo.sequenceID = int(element.text)
            case "k_ele":
                kInfo, priority = processJMdictKanjiElement(element)
                entryInfo.priority = min(entryInfo.priority, priority)
                entryInfo.kanjiInfo.append(kInfo)
            case "r_ele":
                rInfo, priority = processJMdictReadingElement(element)
                entryInfo.priority = min(entryInfo.priority, priority)
                entryInfo.readingInfo.append(rInfo)
            case "sense": 
                sInfo = processJMdictSenseElement(element)
                entryInfo.senseInfo.append(sInfo)

    return JMdict2TowaEntry(entryInfo)
                

def parseJMdict(path: str) -> list[TowaDictEntry]:
    # See http://www.edrdg.org/jmdict/jmdict_dtd_h.html
    #  For doc format
    tree = ET.parse(path)
    root = tree.getroot()

    return [processJMdictEntry(entry) for entry in root]

### ---- Furigana Parsing --------------------------------------------------------------------------

@dataclass
class FuriganaEntry:
    primaryForm:      str
    reading:          str
    furiganaEncoding: str

def parseFuriganaEntry(entry: str) -> str:
    # encode furignana like: {漢字; かんじ}
    encodedReading: str = ""
    for reading in entry["furigana"]:
        if "rt" in reading:
            encodedReading += f"{{{reading["ruby"]};{reading["rt"]}}}"
        else:
            encodedReading += reading["ruby"]

    return encodedReading

def parseJMdictFurigana(path: str) -> list[FuriganaEntry]:
    with open(path, 'r', encoding='utf-8-sig') as f:
        furiganaJson = json.load(f)

    return [FuriganaEntry(f["text"], f["reading"], parseFuriganaEntry(f)) for f in furiganaJson]
        
### ---- Intonation Parsing ------------------------------------------------------------------------

@dataclass
class IntonationEntry:
    primaryForm:        str
    reading:            str
    intonationEncoding: str

def parseIntonationEntry(entry: str) -> list:
    # encode furignana like: {漢字; かんじ}
    encodedReading: str = ""
    for reading in entry["furigana"]:
        if "rt" in reading:
            encodedReading += f"{{{reading["ruby"]};{reading["rt"]}}}"
        else:
            encodedReading += reading["ruby"]

    return encodedReading

def parseKanjiumIntonations(path: str) -> list[IntonationEntry]:
    with open(path, 'r', encoding='utf-8-sig') as f:
        intonationJson = json.load(f)

    entries: list[IntonationEntry] = []
    for form in intonationJson:
        formEntry = intonationJson[form]
        for reading in formEntry:
            entries.append(IntonationEntry(form, reading, formEntry[reading]))

    return entries

### ------------------------------------------------------------------------------------------------

def parseFurigana() -> list[FuriganaEntry]:
    furiganaData = parseJMdictFurigana(FURIGANA_JMDICT_PATH)

    return furiganaData

def parseIntionations() -> list[IntonationEntry]:
    intonationData = parseKanjiumIntonations(INTONATIONS_KANJIUM_PATH)
    return intonationData

def parseWords() -> list[TowaDictEntry]:
    #parseJMnedict()
    dictData: list[TowaDictEntry] = parseJMdict(WORDS_EN_JMDICT_PATH)

    return dictData

def parseAudio():
    pass

def serializeFuriganaEntry(entry: FuriganaEntry) -> tuple[str, str, str]:
    return (entry.primaryForm, entry.reading, entry.furiganaEncoding)

def serializeIntonationEntry(entry: IntonationEntry) -> tuple[str, str, str]:
    return (entry.primaryForm, entry.reading, entry.intonationEncoding)

def serializeTowaDictEntry(entry: TowaDictEntry) -> tuple:
    definitionTexts: list[str] = ["␞".join(deflist) for deflist in entry.definitions]

    # Sqlite starts writing blobs unless I use the unit separator character directly... too bad!
    serializedOtherForms:    str = '␟'.join(entry.otherForms)
    serializedOtherReadings: str = '␟'.join(entry.otherReadings)
    serializedDefinitions:   str = '␟'.join(definitionTexts)

    serializedExamplesJP: str = '␟'.join([str(i) + '␞' + ex for i, ex in entry.examplesJP.items()])
    serializedExamplesEN: str = '␟'.join([str(i) + '␞' + ex for i, ex in entry.examplesEN.items()])

    serializedPosInfo:     str = '␟'.join(['␞'.join([str(i), *[p for p in pInfo]]) for i, pInfo in entry.posInfo.items()])
    serializedFieldInfo:   str = '␟'.join(['␞'.join([str(i), *[f for f in fInfo]]) for i, fInfo in entry.fieldInfo.items()])
    serializedDialectInfo: str = '␟'.join(['␞'.join([str(i), *[d for d in dInfo]]) for i, dInfo in entry.dialectInfo.items()])
    serializedMiscInfo:    str = '␟'.join(['␞'.join([str(i), *[m for m in mInfo]]) for i, mInfo in entry.miscInfo.items()])
    serializedCrossRefs:   str = '␟'.join(['␞'.join([str(i), *[x for x in cRefs]]) for i, cRefs in entry.crossRefs.items()])

    return (
        entry.id,
        entry.priority,
        entry.primaryForm,
        entry.primaryReading,
        serializedOtherForms,
        serializedOtherReadings,
        serializedDefinitions,
        serializedPosInfo,
        serializedFieldInfo,
        serializedDialectInfo,
        serializedMiscInfo,
        serializedExamplesJP,
        serializedExamplesEN,
        serializedCrossRefs
    )

def writeDB(
    con: sqlite3.Connection, 
    dictData: list[TowaDictEntry], 
    furiganaData: list[FuriganaEntry],
    intonationData: list[IntonationEntry]
):
    cur = con.cursor()

    # Furigana Table
    serializedFurigana: list[tuple[str, str, str]] = []
    for fEntry in furiganaData:
        serializedFurigana.append(serializeFuriganaEntry(fEntry))
    
    cur.executemany(f"INSERT INTO {SQL_FURIGANA_TABLE_NAME} VALUES (?, ?, ?)", serializedFurigana)
    con.commit()

    # Intonation Data
    serializedIntonation: list[tuple[str, str, str]] = []
    for iEntry in intonationData:
        serializedIntonation.append(serializeIntonationEntry(iEntry))

    cur.executemany(f"INSERT INTO {SQL_INTONATION_TABLE_NAME} VALUES (?, ?, ?)", serializedIntonation)
    con.commit()

    # Dict Table
    # TODO: Null empty values to save space?
    lookupTableData: dict[str, list[tuple[int, int]]] = {} 
    serializedDict:  list[tuple]                      = []

    for towaEntry in dictData:
        # Readings
        allReadings: list[str] = [towaEntry.primaryReading, *towaEntry.otherReadings]
        for reading in allReadings:
            readingIsPrimaryForm = (towaEntry.primaryForm == reading)
            lookupTableData.setdefault(reading, []).append((towaEntry.id, int(readingIsPrimaryForm)))
        
        # Forms
        if (towaEntry.primaryForm != towaEntry.primaryReading):
            lookupTableData.setdefault(towaEntry.primaryForm, []).append((towaEntry.id, 1))

        for form in towaEntry.otherForms:
            lookupTableData.setdefault(form, []).append((towaEntry.id, 0))

        serializedDict.append(serializeTowaDictEntry(towaEntry))

    valArgStr: str = ",".join(["?"] * 14)
    cur.executemany(f"INSERT INTO {SQL_DICT_TABLE_NAME} VALUES ({valArgStr})", serializedDict)
    con.commit()

    # Lookup Table
    # To serialized format
    serializedLookup: list[tuple[str, str, str]] = []
    for form_or_reading, values in lookupTableData.items():
        serialziedFormIDs = ""
        serializedMatchPriorities = ""

        for i in range(0, len(values)):
            formID, matchPriority = values[i]
            delimiter = ',' if (i != len(values) - 1) else ''

            serialziedFormIDs += str(formID) + delimiter
            serializedMatchPriorities += str(matchPriority) + delimiter

        serializedLookup.append((form_or_reading, serialziedFormIDs, serializedMatchPriorities))
        
    cur.executemany(f"INSERT INTO {SQL_LOOKUP_TABLE_NAME} VALUES (?, ?, ?)", serializedLookup)
    con.commit()

def createDB(path: str) -> sqlite3.Connection:
    _, file_extension = os.path.splitext(path)
    if file_extension != ".db":
        return None

    if os.path.exists(path):
         os.remove(path)

    con = sqlite3.connect(path)
    cur = con.cursor()
    cur.execute(SQL_CREATE_FURIGANA_TABLE_COMMAND)
    cur.execute(SQL_CREATE_INTONATION_TABLE_COMMAND)
    cur.execute(SQL_CREATE_LOOKUP_TABLE_COMMAND)
    cur.execute(SQL_CREATE_DICT_TABLE_COMMAND)
    con.commit()

    return con

def parse():
    con = createDB(DB_PATH)
    if con is None:
        print(f"Failed to create DB @ \"{DB_PATH}\"")
        return 

    print("Parsing dict data...")
    dictData: list[TowaDictEntry] = parseWords()

    print("Parsing furigana...")
    furiganaData: list[FuriganaEntry] = parseFurigana()

    print("Parsing Intonations...")
    intonationData: list[IntonationEntry] = parseIntionations()

    print("Writing DB...")
    writeDB(con, dictData, furiganaData, intonationData)

    print("Done.")

if __name__ == "__main__":
    parse()