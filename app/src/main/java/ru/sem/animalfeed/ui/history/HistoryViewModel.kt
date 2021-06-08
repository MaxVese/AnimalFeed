package ru.sem.animalfeed.ui.history

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.R
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.HistoryDao
import ru.sem.animalfeed.model.*
import ru.sem.animalfeed.ui.base.BaseViewModel
import ru.sem.animalfeed.ui.base.SingleLiveEvent
import ru.sem.animalfeed.utils.FileUtils
import java.io.FileNotFoundException
import javax.inject.Inject


class HistoryViewModel @Inject constructor(private val historyDao: HistoryDao, val context: Context,
                                           private val animalDao: AnimalDao): BaseViewModel() {

    companion object{
        const val TAG ="HistoryVM"
    }

    //var historyData: LiveData<List<History>> = historyDao.findByAnimalId(-1)
    //val histIdData: MutableLiveData<Long> = MutableLiveData();
    val queryData: MutableLiveData<HistoryQuery> = MutableLiveData();

    //val editDate: MutableLiveData<Date> = MutableLiveData()

    val reportData : SingleLiveEvent<Resource<String>> = SingleLiveEvent()

    var historyCurrentData: MutableLiveData<History?> = MutableLiveData()
    var historyData: LiveData<List<History>> = Transformations.switchMap(queryData) {
        historyDao.filterHistory(queryData.value!!)
    }

    val loadEvent: SingleLiveEvent<Boolean> = SingleLiveEvent()
    var animalId: Long? = null

    init {
        loadEvent.postValue(true)
    }

    fun loadByAnimalId(animId: Long){
        //animIdData.value = animId
        this.animalId = animId
        val historyQuery =
            HistoryQuery(animId, HTypeWrapper(null),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                true)
        queryData.value = historyQuery
        //historyData = historyDao.findByAnimalId(animId)
       // Log.d("HistoryViewModel", "loadById: ${historyData.value?.size}")
    }

    fun delItem(history: History){
        historyDao.delete(history)
    }

    fun delItem(pos: Int){
        if(historyData.value!=null) {
            delItem(historyData.value!![pos])
        }
    }

    fun updateStartDate(date: LocalDateTime){
        val historyQuery = queryData.value
        historyQuery!!.startDate = date
        queryData.value = historyQuery
    }

    fun updateEndDate(date: LocalDateTime){
        val historyQuery = queryData.value
        historyQuery!!.endDate = date
        queryData.value = historyQuery
    }

    fun updateAllTime(all: Boolean){
        val historyQuery = queryData.value
        if(historyQuery!!.allTime == all){
            return
        }
        historyQuery!!.allTime = all
        queryData.value = historyQuery
    }

    fun updateHType(hType: HType?){
        val historyQuery = queryData.value
        if(historyQuery!!.hType.hType == hType){
            return
        }
        historyQuery!!.hType = HTypeWrapper(hType)
        queryData.value = historyQuery
    }

    fun loadById(historyId: Long, hType: HType, animalId: Long){
        var history = historyDao.findById(historyId)
        if(history == null){
            history = History()
            history!!.date = LocalDateTime.now()
            history!!.htype = hType
            history!!.animalId = animalId
        }
        historyCurrentData.value = history
    }

    fun updateDate(date: LocalDateTime){
        val history = historyCurrentData.value
        history!!.date = date
        historyCurrentData.value = history
    }

    fun updateCurrentHistInfo(s: String){
        if(historyCurrentData.value!!.info == s){
            return
        }
        historyCurrentData.value!!.info = s
    }

    fun saveCurrentHistory(){
        if(historyCurrentData.value!!.id==null){
            historyDao.insert(historyCurrentData.value!!)
        }else{
            historyDao.update(historyCurrentData.value!!)
        }

    }

    fun exportCSV() {
        if(historyData.value==null || historyData.value!!.isEmpty()){
            reportData.value = Resource.error(context.getString(R.string.nothing_to_save), null)
            return
        }
        Log.d(TAG, "exportCSV: animal id=${animalId}")
        val prefix = animalDao.getById(animalId ?: -1)?.name  ?: ""
        Log.d(TAG, "exportCSV: prefix=$prefix")
        try {
            val mess: String = FileUtils.exportCSV(historyData.value, context, prefix+"_history.csv")
            reportData.value = Resource.success(mess)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            reportData.value = Resource.error(context.getString(R.string.save_error), null)
        } catch (e: Exception) {
            e.printStackTrace()
            reportData.value = Resource.error(context.getString(R.string.save_error), null)
        }
    }

    fun hide(pos:Int): History {
        val item = historyData.value!![pos]
        historyDao.setHidden(true, item.id!!)
        return item
    }

    fun undoDelete(history: History){
        historyDao.setHidden(false, history.id!!)
    }
}