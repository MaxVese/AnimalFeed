package ru.sem.animalfeed.db.converter

import androidx.room.TypeConverter
import ru.sem.animalfeed.model.FeedType


class FeedTypeConverter {

    @TypeConverter
    fun getFeedType(numeral: Int): FeedType {
        for (ds in FeedType.values()) {
            if (ds.ordinal === numeral) {
                return ds
            }
        }
        return FeedType.AUTO_SINGLE_DAY
    }

    @TypeConverter
    fun getFeedInt(status: FeedType): Int {
        return  status.ordinal
    }
}