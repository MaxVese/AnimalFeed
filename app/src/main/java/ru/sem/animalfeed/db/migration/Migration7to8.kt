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

    }


    override fun migrate(database: SupportSQLiteDatabase) {
        Log.d(TAG,"add pos column")


    }
}