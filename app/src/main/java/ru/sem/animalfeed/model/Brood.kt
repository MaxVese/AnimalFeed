package ru.sem.animalfeed.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import ru.sem.animalfeed.db.converter.DateConverter
import org.threeten.bp.LocalDateTime

@Entity(tableName = "broods")
class Brood(var pos: Int) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    var broodName:String = ""

    var fatherKind:String? = null
    var fatherPhoto:String? = null

    var motherKind:String? = null
    var motherPhoto:String? = null

    @TypeConverters(value = [DateConverter::class])
    var masonryDate: LocalDateTime? = null


    @TypeConverters(value = [DateConverter::class])
    var hatchingDate: LocalDateTime? = null
}