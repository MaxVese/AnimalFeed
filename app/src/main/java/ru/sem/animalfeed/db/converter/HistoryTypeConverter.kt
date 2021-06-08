package ru.sem.animalfeed.db.converter

import androidx.room.TypeConverter
import ru.sem.animalfeed.model.HType


class HistoryTypeConverter {

    @TypeConverter
    fun getHType(numeral: Int?): HType {
        for (ds in HType.values()) {
            if (ds.ordinal === numeral) {
                return ds
            }
        }
        return HType.DEFAULT
    }

    @TypeConverter
    fun getHTypeToInt(status: HType?): Int {
        return if (status != null) status.ordinal else 0
    }
}