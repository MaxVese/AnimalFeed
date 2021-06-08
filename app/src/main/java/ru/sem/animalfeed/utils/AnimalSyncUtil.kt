package ru.sem.animalfeed.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.R
import ru.sem.animalfeed.alarm.EatReceiver
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.HistoryDao
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.FeedType
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.model.History

class AnimalSyncUtil(private val animalDao: AnimalDao, private val historyDao: HistoryDao, val context: Context) {

    companion object{
        const val TAG = "AnimalSyncUtil"
    }

    private var isUpdate = false

    fun sync(){
        val animals = animalDao.getAllStatic()
        if(animals==null || animals.isEmpty()){
            Log.d(TAG, "sync: animals is null")
            return
        }

        Log.d(TAG, "sync: try")

        for (animal in animals){
            if(animal.isHand) continue//не трогаем ручное кормление
            when(animal.feedType!!){
                FeedType.AUTO_SINGLE_DAY -> updateSingleFeed(animal)
                FeedType.AUTO_MULTIPLE_DAY -> updateMultipleFeed(animal)
            }
        }
    }

    private fun updateMultipleFeed(animal: Animal){
        val now = LocalDateTime.now()
        isUpdate = false
        //пока следущее время кормления не стане больше предыдущего
        while(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >
            animal.nextFeed!!.atZone(ZoneId.systemDefault())!!.toInstant()!!.toEpochMilli()){
            isUpdate = true
            val history = History().apply {
                animalId = animal.id
                info = context.getString(R.string.feeding)
                date = animal.nextFeed
                htype = HType.EAT
            }
            historyDao.insert(history)

            animal.lastFeed = DateUtilsA.calculateNextFeedMorningOrEvening(animal.lastFeed, animal.morning, animal.evening)
            animal.nextFeed = DateUtilsA.calculateNextFeedMorningOrEvening(animal.lastFeed, animal.morning, animal.evening)
            Log.d(TAG, "sync: ${animal.id}")
        }
        if(isUpdate){
            animalDao.update(animal)
            if(!animal.isHand) {
                createAlarm(animal)
            }
        }
    }

    private fun updateSingleFeed(animal: Animal){
        val now = LocalDateTime.now()
        isUpdate = false
        //пока следущее время кормления не стане больше предыдущего
        while(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() >
            animal.nextFeed!!.atZone(ZoneId.systemDefault())!!.toInstant()!!.toEpochMilli()){
            isUpdate = true
            val history = History().apply {
                animalId = animal.id
                info = "Кормление"
                date = animal.nextFeed
                htype = HType.EAT
            }
            historyDao.insert(history)

            animal.lastFeed = animal.lastFeed!!.plusDays(animal.interval!!.toLong())
            animal.nextFeed = animal.nextFeed!!.plusDays(animal.interval!!.toLong())
            Log.d(TAG, "sync: ${animal.id}")
        }
        if(isUpdate){
            animalDao.update(animal)
            if(!animal.isHand) {
                createAlarm(animal)
            }
        }
    }

    private fun createAlarm(animal: Animal){
        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EatReceiver::class.java)
        val animalId = animal.id!!
        intent.putExtra(ANIMAL_ID, animalId)
        val pIntent = PendingIntent.getBroadcast(context, animalId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        am.cancel(pIntent)

        val timeToAlarm = animal.nextFeed!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                /*am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeToAlarm, pIntent);*/
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeToAlarm, pIntent)
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