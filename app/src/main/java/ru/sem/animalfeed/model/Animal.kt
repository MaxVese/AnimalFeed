package ru.sem.animalfeed.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import ru.sem.animalfeed.db.converter.DateConverter
import ru.sem.animalfeed.db.converter.FeedTypeConverter
import ru.sem.animalfeed.db.converter.GenderConverter
import ru.sem.animalfeed.db.converter.TimeConverter


@Entity(tableName = "animals")
class Animal {

    companion object{
        fun createEmpty(): Animal{
            return Animal().apply {
                lastFeed = LocalDateTime.now()
                gender = Gender.UNKNOWN
            }
        }
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var photo: String? = null

    var name: String? = null

    @TypeConverters(value = [GenderConverter::class])
    var gender: Gender? = null

    var kind: String? = null

    @TypeConverters(value = [DateConverter::class])
    var lastFeed: LocalDateTime? = null

    @TypeConverters(value = [DateConverter::class])
    var nextFeed: LocalDateTime? = null

    var interval = 1

    @ColumnInfo(name="is_hand")
    var isHand: Boolean = false

    @TypeConverters(value = [FeedTypeConverter::class])
    var feedType: FeedType = FeedType.AUTO_SINGLE_DAY

    @ColumnInfo(name="is_hidden", defaultValue = "0")
    var isHidden: Boolean = false

    @ColumnInfo(name="grp_id")
    var groupId: Long? = null

    /*@ColumnInfo(name="is_mult_feed")
    var isMultipleFeed: Boolean = false*/

    @TypeConverters(value = [DateConverter::class])
    var birthDay: LocalDateTime? = null

    var note: String? = null

    @TypeConverters(value = [TimeConverter::class])
    var morning: LocalTime? = null

    @TypeConverters(value = [TimeConverter::class])
    var evening: LocalTime? = null
    

    /*fun calcNextFeedDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = lastFeed
        calendar.add(DateUtilsA.nextFieldValue, interval)
        return calendar.time
    }*/
}