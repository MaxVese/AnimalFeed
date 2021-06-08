package ru.sem.animalfeed.db.converter

import androidx.room.TypeConverter
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoField


class TimeConverter {

    @TypeConverter
    fun dateToTimestamp(date: LocalTime?): Long? {
        return date?.getLong(ChronoField.SECOND_OF_DAY)
    }

    @TypeConverter
    fun timestampToDate(timestamp: Long?): LocalTime? {
        return if(timestamp!=null) {
            LocalTime.ofSecondOfDay(timestamp!!)
        }else null
    }
}