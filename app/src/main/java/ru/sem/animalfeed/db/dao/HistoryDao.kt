package ru.sem.animalfeed.db.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.model.History
import ru.sem.animalfeed.model.HistoryQuery

@Dao
interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(historyItem: History): Long

    @Update
    fun update(vararg historyItem: History)

    @Delete
    fun delete(vararg historyItem: History)

    @Query("SELECT * FROM history")
    fun getAll(): LiveData<List<History>>

    @Query("UPDATE history set is_hidden=:isHidden WHERE id=:id")
    fun setHidden(isHidden: Boolean, id: Long)

    @Query("SELECT * FROM history WHERE animalId=:animId")
    fun findByAnimalId(animId: Long): LiveData<List<History>>

    @Query("SELECT * FROM history WHERE animalId=:animId AND is_hidden=0")
    fun getAllNotHiddenByAnimalId(animId: Long): LiveData<List<History>>

    @Query("SELECT * FROM history WHERE id=:id")
    fun findByIdLive(id: Long): LiveData<History?>

    @Query("SELECT * FROM history WHERE id=:id")
    fun findById(id: Long): History?

    @Query("SELECT * FROM history WHERE info like :info order by datetime(dt) desc")
    fun findByInfo(info: String): LiveData<List<History>>

    @Query("SELECT * FROM history WHERE (animalId=:animId) AND (dt BETWEEN :from AND :toDate) AND h_type IN(:hTypes) AND is_hidden=0 order by dt desc")
    fun filterHistoryWD(animId: Long, from: LocalDateTime, toDate: LocalDateTime, hTypes: Array<Int>): LiveData<List<History>>

    @Query("SELECT * FROM history WHERE (animalId=:animId) AND h_type IN (:hTypes) AND is_hidden=0 order by dt desc")
    fun filterHistoryAllTime(animId: Long, hTypes: Array<Int>): LiveData<List<History>>

   /* @Query("SELECT * FROM history WHERE dt BETWEEN :from AND :toDate")
    fun filterHistoryWD2(from: Date, toDate: Date): LiveData<List<History>>*/

    fun filterHistory(historyQuery: HistoryQuery): LiveData<List<History>> {
        Log.d(TAG, "filterHistory: animId=${historyQuery.animalId} types in(${historyQuery.hType.apply()})")
        return if(historyQuery.allTime){
            filterHistoryAllTime(historyQuery.animalId, historyQuery.hType.apply())
        }else {
            filterHistoryWD(historyQuery.animalId, historyQuery.startDate, historyQuery.endDate, historyQuery.hType.apply())
            //filterHistoryWD2( historyQuery.startDate, historyQuery.endDate)
        }
    }

    @Query("SELECT * FROM history WHERE (animalId=:animId) AND dt = :dateTime order by id desc limit 1")
    fun getLastHistoryByDate(animId: Long, dateTime: LocalDateTime): History?
}

const val TAG = "HistoryDao"