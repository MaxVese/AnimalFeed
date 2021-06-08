package ru.sem.animalfeed.db.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import ru.sem.animalfeed.model.Group

@Dao
interface GroupsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Group): Long

    @Update
    fun update(vararg item: Group)

    @Delete
    fun delete(vararg iItem: Group)

    @Query("SELECT * FROM groups ORDER BY pos ASC")
    fun getAll(): LiveData<List<Group>>

    @Query("SELECT * FROM groups ORDER BY pos ASC")
    fun getAllStock(): List<Group>

    //@Transaction
    fun getAllWithNoGroup(groupTitle: String): LiveData<List<Group>> {
        val r = ArrayList<Group>()
        r.add(Group.createNoGroup(groupTitle))
        r.addAll(getAllStock())
        return MutableLiveData<List<Group>>(r)
    }

    @Query("UPDATE groups set pos=:newPos WHERE id=:id")
    fun setPosition(newPos: Int, id: Long)


    @Query("SELECT * FROM groups WHERE id=:id")
    fun findByIdLive(id: Long): LiveData<Group?>

    @Query("SELECT * FROM groups WHERE id=:id")
    fun findById(id: Long): Group?

    @Query("SELECT * FROM groups WHERE name=:name")
    fun findByName(name: String): Group?


    @Transaction
    fun onSwiped(map: Map<Long, Int>){
        for ((k, v) in map) {
            setPosition(v, k)
        }
    }
}
