package ru.sem.animalfeed.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import dagger.android.AndroidInjection
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.DEFER_DAY_COUNT
import ru.sem.animalfeed.EXTRA_NOTIFICATION_ID
import ru.sem.animalfeed.R
import ru.sem.animalfeed.db.AppDatabase
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.model.History
import javax.inject.Inject


class DeferReceiver : BroadcastReceiver() {

    companion object{
        const val TAG = "DeferReceiver"
    }

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context);
        Log.d(TAG, "onReceive: defer")
        val notificationManager =
            context!!.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.cancel(intent!!.getIntExtra(EXTRA_NOTIFICATION_ID, 0))

        val animal = appDatabase.getAnimalDao().getById(intent!!.getLongExtra(ANIMAL_ID, -1)) ?: return


        /*val duration: Duration = Duration.between(animal.lastFeed!!, LocalDateTime.now())
        Log.d(TAG, "onReceive: duration lastFeed and now=${duration.toDays()}")
        val days = ChronoUnit.DAYS.between(animal!!.lastFeed!!.plusDays(2), LocalDateTime.now())
        Log.d(TAG, "onReceive: duration lastFeed and now=${days}")*/
        val now = LocalDateTime.now()
        if(now.monthValue <= animal.lastFeed!!.monthValue){
            if(now.dayOfMonth > animal.lastFeed!!.dayOfMonth){
                //если мы попытались отложить на 1 день не в день уведомления
                showToast(context, context.getString(R.string.transfer_expired))
            }else{
                //если переносим прямо сегодня, то удалем запись о окрмлении из дневника
                //меняем дату последнго кормления на lastFeed - interval
                //ставим nextFeed + 1
                val history = appDatabase.getHistoryDao().getLastHistoryByDate(animal.id!!, animal.lastFeed!!)
                if(history!=null){
                    Log.d(TAG, "onReceive: delete last history by id=${history.id}")
                    appDatabase.getHistoryDao().delete(history)
                }
                animal.lastFeed = animal.lastFeed!!.minusDays(animal.interval.toLong())
                animal.nextFeed = LocalDateTime.now().plusDays(DEFER_DAY_COUNT.toLong())
                val historyDefer = History().apply {
                    info = context.getString(R.string.feeding_transfer)
                    date = LocalDateTime.now()
                    htype =HType.EAT
                    animalId = animal.id
                }
                appDatabase.getHistoryDao().insert(historyDefer)
                appDatabase.getAnimalDao().update(animal)
                createAlarm(animal, context)
            }
        }else{
            //если же уже текущий месяц больше месяца кормления, то тоже тост показываем
            showToast(context, context.getString(R.string.transfer_expired))
        }
    }

    private fun showToast(context: Context, text: String){
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    private fun createAlarm(animal: Animal, context: Context){
        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EatReceiver::class.java)
        val animalId = animal.id!!
        intent.putExtra(ANIMAL_ID, animalId)
        val pIntent = PendingIntent.getBroadcast(context, animalId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

        am.cancel(pIntent)//отменяем прошлую напоминалку

        val timeToAlarm = animal.nextFeed!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeToAlarm, pIntent);
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