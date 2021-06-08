package ru.sem.animalfeed.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.BuildConfig
import ru.sem.animalfeed.R
import ru.sem.animalfeed.alarm.EatReceiver
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.GroupsDao
import ru.sem.animalfeed.db.dao.HistoryDao
import ru.sem.animalfeed.model.*
import ru.sem.animalfeed.ui.base.BaseViewModel
import ru.sem.animalfeed.ui.base.SingleLiveEvent
import ru.sem.animalfeed.utils.AnimalSyncUtil
import ru.sem.animalfeed.utils.ImageFilePath
import ru.sem.animalfeed.utils.XiaomiUtilities
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class MainViewModel @Inject constructor(
    val animalDao: AnimalDao, val historyDao: HistoryDao,
    val context: Context, private val groupsDao: GroupsDao
): BaseViewModel() {

    init {
        fixVersion291()
    }

    private val groupIdData: MutableLiveData<Long?> = MutableLiveData(null)

    fun setGroupId(groupId: Long?){
        Log.d(TAG, "setGroupId: $groupId")
        groupIdData.value = groupId
    }

    fun setGroupId(groupName: String){
        val group = groupsDao.findByName(groupName)
        if(group!=null){
            setGroupId(group!!.id)
        }
    }

    val animalData: LiveData<List<Animal>> = Transformations.switchMap(groupIdData) {
        Log.d(TAG, "select by groupId: $it")
        if(it==null){
            animalDao.getAll()
        }else {
            animalDao.getAllNotHiddenByGroupId(it)
        }
    }//animalDao.getAllNotHidden()

    val powerDlgData: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val newDlgData: SingleLiveEvent<Boolean> = SingleLiveEvent()

    val customMessageData: SingleLiveEvent<String> = SingleLiveEvent()

    val groupsData = groupsDao.getAll()

    val groupWithNoGroupData: LiveData<List<Group>> = Transformations.map(groupsData) {
        val result = ArrayList<Group>(it.size+1)
        result.add(Group.createNoGroup(context.getString(R.string.all),0))
        result.addAll(it)
        result
    }


    fun isGroupExist(group: Group):Boolean{
        return groupsDao.findByName(group.name) !=null
    }

    companion object{
        const val TAG = "MainViewModel"
        val newNotShowVersion= intArrayOf(14, 15)//!!добавить версии, где не нужно показывать дилог что новго
    }
    init {
        AnimalSyncUtil(animalDao, historyDao, context).sync()
        val pref = context.getSharedPreferences("conf", Context.MODE_PRIVATE)
        if(!pref.getBoolean("${BuildConfig.VERSION_CODE}new", false)){
            pref.edit()
                .putBoolean("${BuildConfig.VERSION_CODE}new", true)
                .apply()
            if(!newNotShowVersion.contains(BuildConfig.VERSION_CODE)){
                newDlgData.call()
            }else{
                if(XiaomiUtilities.isMIUI()) {
                    powerDlgData.call()
                }
            }
        }else {
            if(XiaomiUtilities.isMIUI()) {
                powerDlgData.call()
            }
        }
    }

    fun checkNeedShowPowerDlg(){
        powerDlgData.call()
    }

    private fun resizeIfNeed(){
        if(BuildConfig.VERSION_CODE == 14) {
            val pref = context.getSharedPreferences("conf", Context.MODE_PRIVATE)
            if(!pref.getBoolean("resize", false)){
                pref.edit().putBoolean("resize", true).apply()
                Log.d(TAG, "resizeIfNeed: now")
                val animals = animalDao.getAllStatic()
                for (animal in animals){
                    if(animal.photo!=null && animal.photo!!.isNotEmpty()) {
                        try {
                            ImageFilePath.resizeImage(
                                File(animal.photo!!),
                                ImageFilePath.DEFAULT_IMAGE_WIDTH,
                                context
                            )
                        }catch (e: Exception){}
                    }
                }
            }
        }
    }

    fun delAnimal(animal: Animal){
        if(!animal.isHand){
            val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, EatReceiver::class.java)
            intent.putExtra(ANIMAL_ID, animal.id)
            val pIntent = PendingIntent.getBroadcast(
                context,
                animal.id!!.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            am.cancel(pIntent)
        }
        animalDao.delete(animal)
    }

    fun delAnimal(pos: Int){
        if(animalData.value!=null) {
            delAnimal(animalData.value!![pos])
        }
    }

    fun handEat(animal: Animal){
        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EatReceiver::class.java)
        val animalId = animal.id!!
        intent.putExtra(ANIMAL_ID, animalId)
        val pIntent = PendingIntent.getBroadcast(
            context,
            animalId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        if(!animal.isHand){
            am.cancel(pIntent)
        }

        animal.lastFeed = LocalDateTime.now()
        val nextFeedFormat:LocalDateTime = LocalDateTime.now().toLocalDate().atTime(animal.nextFeed?.toLocalTime())
        when(animal.feedType!!){
            FeedType.AUTO_MULTIPLE_DAY -> {
                if (animal.nextFeed == LocalDateTime.now().with(animal.evening)) {
                    animal.nextFeed = LocalDateTime.now().plusDays(1).with(animal.morning)
                } else {
                    animal.nextFeed = LocalDateTime.now().with(animal.evening)
                }
            }
            else -> animal.nextFeed = nextFeedFormat.plusDays(animal.interval.toLong())
        }


        val history = History().apply {
            info = context.getString(R.string.feeding)
            this.animalId = animal.id!!
            date = animal.lastFeed!!
            htype = HType.EAT
        }
        historyDao.insert(history)
        animalDao.update(animal)
        customMessageData.value = context.getString(R.string.feeding_successful)

        if(!animal.isHand) {
            val timeToAlarm =
                animal.nextFeed!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    am.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        timeToAlarm, pIntent
                    );
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    am.setAlarmClock(AlarmManager.AlarmClockInfo(timeToAlarm, pIntent), pIntent)
                }
                else -> {
                    am.set(AlarmManager.RTC_WAKEUP, timeToAlarm, pIntent)
                }
            }
        }
    }

    fun hide(pos: Int): Animal {
        val an = animalData.value!![pos]
        animalDao.setHidden(true, an.id!!)
        return an
    }

    fun undoDelete(animal: Animal){
        animalDao.setHidden(false, animal.id!!)
    }

    fun showAll(){
        animalDao.showAll(false)
    }

    fun deleteGroup(pos: Int){
        if(groupsData.value!=null) {
            deleteGroup(groupsData.value!![pos])
        }
    }

    fun deleteGroup(item: Group){
        animalDao.setNoGroupByGroupId(item.id!!)
        groupsDao.delete(item)
    }

    fun saveGroup(group: Group){
        if(group.id == null){
            groupsDao.insert(group)
        }else{
            groupsDao.update(group)
        }
    }

    fun saveNewGroupsOrder(map: Map<Long, Int>){
        groupsDao.onSwiped(map)
    }

    private fun fixVersion291(){
        val isFixed = context.getSharedPreferences("conf", Context.MODE_PRIVATE).getBoolean("v291", false)
        if(!isFixed) {
            animalDao.getAllStatic().filter {
                it.isHand
            }.forEach {
                cancelOldAlarm(it.id, context)
            }
            context.getSharedPreferences("conf", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("v291", true)
                .apply()
        }
    }
}