package ru.sem.animalfeed.db.migration

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration6to7(val startVersion: Int, val oldVestion: Int) : Migration(
    startVersion,
    oldVestion
) {

    companion object{
        const val TAG = "Migration6to7"
        private const val sqlGroupTable = "CREATE TABLE \"groups\" (\n" +
                "\"id\"  INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "\"name\"  TEXT NOT NULL UNIQUE,\n" +
                "\"pos\"  INTEGER NOT NULL\n" +
                ");"
    }


    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(sqlGroupTable)
        Log.d(TAG, "migrate: success create groups table")
        database.execSQL("ALTER TABLE animals ADD COLUMN grp_id INTEGER")
        database.execSQL("ALTER TABLE animals ADD COLUMN birthDay INTEGER")
        database.execSQL("ALTER TABLE animals ADD COLUMN evening INTEGER")
        database.execSQL("ALTER TABLE animals ADD COLUMN morning INTEGER")
        database.execSQL("ALTER TABLE animals ADD COLUMN note TEXT")
        database.execSQL("ALTER TABLE animals ADD COLUMN feedType INTEGER DEFAULT 0 NOT NULL")

        database.execSQL("UPDATE animals SET feedType=0 WHERE is_hand=0")
        database.execSQL("UPDATE animals SET feedType=2 WHERE is_hand=1")
        //database.execSQL("ALTER TABLE animals ADD COLUMN is_mult_feed INTEGER DEFAULT 0 NOT NULL")
        Log.d(TAG, "migrate: success alter animals table")
    }
}