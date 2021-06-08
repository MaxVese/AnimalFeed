package ru.sem.animalfeed.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration5to6(val startVersion: Int, val oldVestion: Int) : Migration(startVersion, oldVestion) {

    companion object{
        const val TAG = "Migration5to6"
    }


    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE history ADD COLUMN is_hidden INTEGER DEFAULT 0 NOT NULL")
        database.execSQL("ALTER TABLE animals ADD COLUMN is_hidden INTEGER DEFAULT 0 NOT NULL")
    }
}