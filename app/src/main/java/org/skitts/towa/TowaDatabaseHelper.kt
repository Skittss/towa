package org.skitts.towa

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.IOException

object PreferencesKeys {
    val DB_VER         = intPreferencesKey("db_ver")
    val ANKI_DECK_NAME = stringPreferencesKey("anki_deck_name")
    val THEME          = stringPreferencesKey("theme")
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "configs")

class TowaDatabaseHelper(
    private var context: Context
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VER) {

    companion object {
        private const val DB_NAME:       String = "towa.db"
        private const val DB_ASSET_PATH: String = "databases/$DB_NAME"
        private const val DB_VER:        Int    = 1;
    }
    private var dbIsReady: Boolean = false

    fun <T> readPreference(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    suspend fun <T> readPreferenceSynchronous(key: Preferences.Key<T>, defaultValue: T): T {
        return readPreference<T>(key, defaultValue).firstOrNull() ?: defaultValue
    }

    suspend fun <T> writePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences -> preferences[key] = value }
    }

    suspend fun initializeDB() {
        if (databaseExists()) {
            val dbVersion = readPreferenceSynchronous(PreferencesKeys.DB_VER, -1)
            if (DB_VER != dbVersion) {
                val dbFile: File = context.getDatabasePath(DB_NAME)
                if (!dbFile.delete()) {
                    throw IOException("Failed to update Towa's database following a version change.")
                }
                createDatabase()
            }
        }
        else {
            createDatabase()
        }
        dbIsReady = true
    }

    override fun onCreate(db: SQLiteDatabase?) {}
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    private fun databaseExists(): Boolean {
        val dbFile: File = context.getDatabasePath(DB_NAME)
        return dbFile.exists()
    }

    private suspend fun createDatabase() {
        val parentPath: String = context.getDatabasePath(DB_NAME).getParent()
            ?: throw IOException("Could not find parent directory of Towa database.")
        val path: String = context.getDatabasePath(DB_NAME).path

        val parentDir = File(parentPath)
        if (!parentDir.exists()) {
            if (!parentDir.mkdir()) {
                throw IOException("Could not make directory for Towa database.")
            }
        }

        val file = File(path)
        if (file.exists()) {
            throw IOException("Tried to overwrite existing Towa database.")
        }

        val assetFile = context.assets.open(DB_ASSET_PATH)
        assetFile.copyTo(file.outputStream())
        Log.d("#DB", "Copying DB from bundled assets...")

        writePreference(PreferencesKeys.DB_VER, DB_VER)
    }

}