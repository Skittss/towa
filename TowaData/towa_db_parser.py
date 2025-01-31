from dataclasses import dataclass, field
import os
import sqlite3
import xml.etree.ElementTree as ET

BASE_DIR = os.path.dirname(__file__)
DB_PATH  = os.path.join(BASE_DIR, "../app/src/main/assets/towa.db")

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

SQL_LOOKUP_TABLE_NAME = "towalookup"
SQL_DICT_TABLE_NAME   = "towadict"

SQL_CREATE_LOOKUP_TABLE_COMMAND = f'''CREATE TABLE {SQL_LOOKUP_TABLE_NAME}
    (form_or_reading text NOT NULL, form_ids text NOT NULL, primary_match_flags text)'''
SQL_CREATE_DICT_TABLE_COMMAND = f'''CREATE TABLE {SQL_DICT_TABLE_NAME} 
    (form_id INTEGER NOT NULL, primary_form text, primary_reading text, other_forms text, other_readings text, definitions text, example_jp text, example_en text)'''

AUDIO_KANJI_ALIVE_PATH       = os.path.join(BASE_DIR, "audio\\kanji_alive")
AUDIO_KANJI_ALIVE_INDEX_PATH = os.path.join(BASE_DIR, "audio\\kanji_alive_index.csv")
AUDIO_TOFUGU_PATH            = os.path.join(BASE_DIR, "audio\\tofugu")

WORDS_EN_JMDICT_PATH         = os.path.join(BASE_DIR, "words\\JMdict.xml")
WORDS_EN_JMNEDICT_PATH       = os.path.join(BASE_DIR, "words\\JMnedict.xml")

ELEMENT_TREE_XML_NAMESPACE_PREFIX  = '{http://www.w3.org/XML/1998/namespace}'

def parseJMnedict(path: str):
    pass

### ---- JMDict Parsing ----------------------------------------------------------------------------

@dataclass
class JMdictReadingElement:
    kanaReading: str  = ""
    noKanjiFlag: bool = False
    readingInfo: str  = ""

@dataclass
class JMdictKanjiElement:
    kanji: str  = ""

@dataclass
class JMdictSenseElement:
    definitions: list[str] = field(default_factory=list)
    exampleJP:   str = ""
    exampleEN:   str = ""

@dataclass
class JMdictEntry:
    sequenceID:  int                        = -1
    kanjiInfo:   list[JMdictKanjiElement]   = field(default_factory=list)
    readingInfo: list[JMdictReadingElement] = field(default_factory=list)
    senseInfo:   list[JMdictSenseElement]   = field(default_factory=list)

@dataclass
class TowaDictEntry:
    id:               int
    primaryForm:      str
    otherForms:       list[str]
    primaryReading:   str 
    otherReadings:    list[str]
    definitions:      list[str]
    primaryExampleJP: str
    primaryExampleEN: str

def processJMdictReadingElement(r_ele: ET.Element) -> JMdictReadingElement:
    readingElement = JMdictReadingElement()

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
                pass

    return readingElement

def processJMdictKanjiElement(k_ele: ET.Element):
    kanjiElement = JMdictKanjiElement()

    for elmt in k_ele:
        elmtType: str = elmt.tag
        match(elmtType):
            case "keb":
                kanjiElement.kanji = elmt.text
            case "ke_inf":
                pass
            case "ke_pri":
                pass

    return kanjiElement

def processJMdictExampleElement(example: ET.Element) -> tuple[str, str]:
    sentenceJP: str = ""
    sentenceEN: str = ""

    for elmt in example:
        elmtType: str = elmt.tag
        match(elmtType):
            case "ex_sent":
                if elmt.attrib[f"{ELEMENT_TREE_XML_NAMESPACE_PREFIX}lang"] == "jpn":
                   sentenceJP = elmt.text#
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

    return senseElement

def JMdict2TowaEntry(entry: JMdictEntry) -> TowaDictEntry:
    # Process Required fields (>= 1)
    primaryReading:   str             = entry.readingInfo[0].kanaReading
    otherReadings:    list[str]       = [r.kanaReading for r in entry.readingInfo[1:]] 
    definitions:      list[list[str]] = [s.definitions for s in entry.senseInfo]
    primaryExampleJP: str             = entry.senseInfo[0].exampleJP
    primaryExampleEN: str             = entry.senseInfo[0].exampleEN

    # Process Optional fields (>= 0)
    hasKanji = len(entry.kanjiInfo) > 0
    primaryForm:    str       = entry.kanjiInfo[0].kanji if hasKanji else primaryReading
    otherForms:     list[str] = [k.kanji for k in entry.kanjiInfo[1:]]

    id: int = entry.sequenceID

    return TowaDictEntry(
        id,
        primaryForm,
        otherForms,
        primaryReading,
        otherReadings,
        definitions,
        primaryExampleJP,
        primaryExampleEN
    )

def processJMdictEntry(entry: ET.Element) -> TowaDictEntry:
    entryInfo = JMdictEntry()

    for element in entry:
        elementType: str = element.tag
        match(elementType):
            case "ent_seq":
                entryInfo.sequenceID = int(element.text)
            case "k_ele":
                kInfo = processJMdictKanjiElement(element)
                entryInfo.kanjiInfo.append(kInfo)
            case "r_ele":
                rInfo = processJMdictReadingElement(element)
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


### ------------------------------------------------------------------------------------------------

def parseWords() -> list[TowaDictEntry]:
    #parseJMnedict()
    dictData: list[TowaDictEntry] = parseJMdict(WORDS_EN_JMDICT_PATH)

    return dictData

def parseAudio():
    pass

def createDB(path: str) -> sqlite3.Connection:
    _, file_extension = os.path.splitext(path)
    if file_extension != ".db":
        return None

    if os.path.exists(path):
         os.remove(path)

    con = sqlite3.connect(path)
    cur = con.cursor()
    cur.execute(SQL_CREATE_LOOKUP_TABLE_COMMAND)
    cur.execute(SQL_CREATE_DICT_TABLE_COMMAND)
    con.commit()

    return con

def serializeTowaDictEntry(entry: TowaDictEntry) -> list[tuple[int, str, str, str, str, str, str, str]]:
    definitionTexts: list[str] = [", ".join(deflist) for deflist in entry.definitions]

    # Sqlite starts writing blobs unless I use the unit separator character directly... too bad!
    serializedOtherForms: str = '␟'.join(entry.otherForms)
    serializedOtherReadings: str = '␟'.join(entry.otherReadings)
    serializedDefinitions: str = '␟'.join(definitionTexts)

    return (
        entry.id,
        entry.primaryForm,
        entry.primaryReading,
        serializedOtherForms,
        serializedOtherReadings,
        serializedDefinitions,
        entry.primaryExampleJP,
        entry.primaryExampleEN
    )

def writeDB(con: sqlite3.Connection, dictData: list[TowaDictEntry]):
    cur = con.cursor()

    lookupTableData: dict[str, list[tuple[int, int]]]                    = {} 
    serializedDict:  list[tuple[int, str, str, str, str, str, str, str]] = []

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

    cur.executemany(f"INSERT INTO {SQL_DICT_TABLE_NAME} VALUES (?, ?, ?, ?, ?, ?, ?, ?)", serializedDict)
    con.commit()

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

def parse():
    con = createDB(DB_PATH)
    if con is None:
        print(f"Failed to create DB @ \"{DB_PATH}\"")
        return 

    print("Parsing dict data...")
    dictData: list[TowaDictEntry] = parseWords()
    print("Writing DB...")
    writeDB(con, dictData)
    print("Done.")

if __name__ == "__main__":
    parse()