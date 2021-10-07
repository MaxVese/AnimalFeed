package ru.sem.animalfeed.db.migration

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration7to8(val startVersion: Int, val oldVestion: Int) : Migration(
        startVersion,
        oldVestion
) {

    companion object{
        const val TAG = "Migration7to8"
        private const val sqlBroodTable = "CREATE TABLE \"broods\" (\n" +
                "\"id\"  INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\"pos\"  INTEGER,\n" +
                "\"broodName\"  TEXT NOT NULL UNIQUE,\n" +
                "\"fatherKind\"  TEXT ,\n" +
                "\"fatherPhoto\"  TEXT ,\n" +
                "\"motherKind\"  TEXT ,\n" +
                "\"motherPhoto\"  TEXT ,\n" +
                "\"masonryDate\"  INTEGER ,\n" +
                "\"hatchingDate\"  INTEGER \n" +
                ");"
    }


    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(sqlBroodTable)

    }
}