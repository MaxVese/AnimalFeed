package ru.sem.animalfeed.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Migration4to5(val startVersion: Int, val oldVestion: Int) : Migration(startVersion, oldVestion) {

    companion object{
        const val TAG = "Migration4to5"
    }


    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL("UPDATE history set h_type=0 WHERE h_type is NULL")
    }
}