package ru.sem.animalfeed.model

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.db.converter.DateConverter
import ru.sem.animalfeed.db.converter.HistoryTypeConverter


@Entity(tableName = "history",
    foreignKeys = [ForeignKey(entity = Animal::class, parentColumns = ["id"], childColumns = ["animalId"], onDelete = CASCADE)]
)
class History {

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var animalId: Long? = null

    var info: String? = null

    @TypeConverters(value = [DateConverter::class])
    @ColumnInfo(name = "dt")
    var date: LocalDateTime? = null

    @TypeConverters(value = [HistoryTypeConverter::class])
    @ColumnInfo(name = "h_type")
    var htype: HType? = null

    @ColumnInfo(name="is_hidden", defaultValue = "0")
    var isHidden: Boolean = false
}