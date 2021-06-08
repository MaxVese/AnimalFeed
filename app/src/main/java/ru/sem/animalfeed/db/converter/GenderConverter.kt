package ru.sem.animalfeed.db.converter

import androidx.room.TypeConverter
import ru.sem.animalfeed.model.Gender


class GenderConverter {

    @TypeConverter
    fun getGender(numeral: Int): Gender? {
        for (ds in Gender.values()) {
            if (ds.ordinal === numeral) {
                return ds
            }
        }
        return null
    }

    @TypeConverter
    fun getStatusInt(status: Gender?): Int? {
        return if (status != null) status.ordinal else null
    }
}