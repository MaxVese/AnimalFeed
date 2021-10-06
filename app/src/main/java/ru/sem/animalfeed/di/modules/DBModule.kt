package ru.sem.animalfeed.di.modules

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ru.sem.animalfeed.db.AppDatabase
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.GroupsDao
import ru.sem.animalfeed.db.dao.HistoryDao
import ru.sem.animalfeed.db.migration.*
import javax.inject.Singleton


@Module
class DBModule {

    @Singleton
    @Provides
    internal fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app.applicationContext,
            AppDatabase::class.java, AppDatabase.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .addMigrations(Migration2to3(2, 3))
            .addMigrations(Migration3to4(3, 4))
            .addMigrations(Migration4to5(4, 5))
            .addMigrations(Migration5to6(5, 6))
            .addMigrations(Migration6to7(6, 7))
//            .addMigrations(Migration7to8(7, 8))
            .allowMainThreadQueries()
            .build()
    }

    @Singleton
    @Provides
    internal fun provideAnimalDao(appDatabase: AppDatabase): AnimalDao {
        return appDatabase.getAnimalDao()
    }

    @Singleton
    @Provides
    internal fun provideHistoryDao(appDatabase: AppDatabase): HistoryDao {
        return appDatabase.getHistoryDao()
    }

    @Singleton
    @Provides
    internal fun provideGroupsDaoDao(appDatabase: AppDatabase): GroupsDao {
        return appDatabase.getGroupsDaoDao()
    }
}