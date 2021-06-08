package ru.sem.animalfeed.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.sem.animalfeed.db.converter.DateConverter
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.GroupsDao
import ru.sem.animalfeed.db.dao.HistoryDao
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.Group
import ru.sem.animalfeed.model.History


@Database(entities = [Animal::class, History::class, Group::class],
    version = AppDatabase.VERSION, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
       const val VERSION = 7

       const val DB_NAME = "animals.db"
    }

    abstract fun getAnimalDao(): AnimalDao

    abstract fun getHistoryDao(): HistoryDao

    abstract fun getGroupsDaoDao(): GroupsDao

}