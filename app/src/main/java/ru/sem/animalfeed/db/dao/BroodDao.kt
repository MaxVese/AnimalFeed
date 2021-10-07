package ru.sem.animalfeed.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.Brood

@Dao
interface BroodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(brood: Brood): Long

    @Update
    fun update(brood: Brood)

    @Delete
    fun delete(vararg broods: Brood)

    @Query("SELECT * FROM broods ORDER BY pos ASC")
    fun getAll(): LiveData<List<Brood>>

    @Query("SELECT * FROM broods WHERE id=:id")
    fun getById(id: Long): Brood?

    @Query("UPDATE broods set pos=:newPos WHERE id=:id")
    fun setPosition(newPos: Int, id: Long)

    @Transaction
    fun onSwiped(map: Map<Long, Int>){
        for ((k, v) in map) {
            setPosition(v, k)
        }
    }
}