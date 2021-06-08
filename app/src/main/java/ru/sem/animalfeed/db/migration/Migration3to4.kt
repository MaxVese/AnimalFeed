package ru.sem.animalfeed.db.migration

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class Migration3to4(val startVersion: Int, val oldVestion: Int) : Migration(startVersion, oldVestion) {

    companion object{
        const val TAG = "Migration3to4"
    }

    private val sqlHistory = """CREATE TABLE IF NOT EXISTS "history" (
"id"  INTEGER PRIMARY KEY AUTOINCREMENT,
"animalId"  INTEGER,
"info"  TEXT,
"dt"  INTEGER, FOREIGN KEY ("animalId") REFERENCES "animals" ("id") ON DELETE CASCADE
);"""

    private data class HistoryHelper(val id: Long, val animalId: Long, val info: String, val dt: Long)

    override fun migrate(database: SupportSQLiteDatabase) {
        val formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val cursor = database.query("select * from history")

        val helpers: ArrayList<HistoryHelper> = ArrayList()
        while (cursor.moveToNext()) {
            val dt =
                LocalDateTime.parse(cursor.getString(3), formatter)
            helpers.add(
                HistoryHelper(
                    cursor.getLong(0),
                    cursor.getLong(1),
                    cursor.getString(2),
                    dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            )
        }
        cursor.close()
        database.execSQL("DROP TABLE history")
        database.execSQL(sqlHistory)

        database.beginTransaction()
        try {
            for (i in helpers.indices) {
                val values = ContentValues()
                values.put("animalId", helpers[i].animalId);
                values.put("info", helpers[i].info);
                values.put("dt", helpers[i].dt);
                values.put("id", helpers[i].id)
                database.insert("history", SQLiteDatabase.CONFLICT_REPLACE, values)
            }
            database.setTransactionSuccessful()
            Log.d(TAG, "migrate: success")
        } catch (e: Exception) {
            Log.d(TAG, "migrate: failure migrate animals data", e)
        } finally {
            database.endTransaction()
        }

        database.execSQL("ALTER TABLE history ADD COLUMN h_type INTEGER DEFAULT NULL")
    }
}