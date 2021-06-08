package ru.sem.animalfeed.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.sem.animalfeed.model.Animal


@Dao
interface AnimalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(animal: Animal): Long

    @Update
    fun update(animal: Animal)

    @Delete
    fun delete(vararg animals: Animal)

    @Query("SELECT * FROM animals")
    fun getAll(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals WHERE is_hidden=0 order by name ASC")
    fun getAllNotHidden(): LiveData<List<Animal>>

    @Query("SELECT * FROM animals WHERE is_hidden=0 AND (grp_id=:groupId or (grp_id is null and :groupId is null)) order by name ASC")
    fun getAllNotHiddenByGroupId(groupId: Long?): LiveData<List<Animal>>

    @Query("SELECT * FROM animals")
    fun getAllStatic(): List<Animal>


    @Query("SELECT * FROM animals WHERE id=:id")
    fun getById(id: Long): Animal?

    @Query("DELETE FROM animals")
    fun clearAll()

    @Query("UPDATE animals set is_hidden=:isHidden WHERE id=:id")
    fun setHidden(isHidden: Boolean, id: Long)

    @Query("UPDATE animals set is_hidden=:isHidden")
    fun showAll(isHidden: Boolean)

    @Query("UPDATE animals set grp_id=null WHERE grp_id=:id")
    fun setNoGroupByGroupId(id: Long)

    @Query("SELECT COUNT(*) FROM animals")
    fun getAmount(): Int
}