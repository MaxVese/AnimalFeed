package ru.sem.animalfeed.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "groups"/*, indices = [Index(value = ["name"], unique = true)]*/)
class Group(var pos: Int, var name: String) {

    companion object{
        fun createNoGroup(title: String, pos: Int =0): Group{
            return Group(pos, title)
        }
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}