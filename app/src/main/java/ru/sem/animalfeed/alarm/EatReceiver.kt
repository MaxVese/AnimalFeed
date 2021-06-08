package ru.sem.animalfeed.alarm

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import dagger.android.AndroidInjection
import org.threeten.bp.ZoneId
import ru.sem.animalfeed.*
import ru.sem.animalfeed.db.AppDatabase
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.FeedType
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.model.History
import ru.sem.animalfeed.ui.history.HistoryActivity
import ru.sem.animalfeed.ui.main.MainActivity
import ru.sem.animalfeed.utils.DateUtilsA
import java.io.File
import javax.inject.Inject


class EatReceiver : BroadcastReceiver(){
    companion object{
        private const val TAG = "EatReceiver"
    }

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context);
        Log.d(TAG, "onReceive: time to eat")

        val animal = appDatabase.getAnimalDao().getById(intent!!.getLongExtra(ANIMAL_ID, -1)) ?: return

        //отслыаем уведомление, если нашли животное в БД
        val pm = context!!.getSystemService(POWER_SERVICE) as PowerManager
        val wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AnimalFeed:$TAG")
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
        sendNotification(context!!, animal, MAIN_CHANNEL_ID, animal.id!!.toInt())
        wakeLock.release()

        //добавляем запись в дневник
        val history = History().apply {
            animalId = animal.id
            info = context.getString(R.string.feeding)
            date = animal.nextFeed//сработало уведолени в эту дата(можно сделать текущее время)
            htype = HType.EAT
        }
        appDatabase.getHistoryDao().insert(history)

        //меняем дату последнего кормления и дату запланированного следущего кормления
        when(animal.feedType!!){
            FeedType.AUTO_MULTIPLE_DAY ->{
                animal.lastFeed = animal.nextFeed
                animal.nextFeed = DateUtilsA.calculateNextFeedMorningOrEvening(animal.lastFeed, animal.morning, animal.evening)
            }
            FeedType.AUTO_SINGLE_DAY ->{
                animal.lastFeed = animal.lastFeed!!.plusDays(animal.interval.toLong())
                animal.nextFeed = animal.nextFeed!!.plusDays(animal.interval.toLong())
            }
        }
        appDatabase.getAnimalDao().update(animal)

        //создаем новое запланированное уведомление
        createAlarm(animal, context)
    }

    private fun sendNotification(context: Context, animal: Animal, channelID: String, notifId: Int) {
        Log.d(TAG, "sendNotification")
        var largeIcon: Bitmap? = null
        if(animal.photo!=null && File(animal.photo).exists()) {
            largeIcon = BitmapFactory.decodeFile(animal!!.photo)
        }
        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_notif_default)
            .setContentTitle(animal.name)
            .setContentText(context.getString(R.string.time_to_feed))
            .setSound(
                RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            )
            .setVibrate(longArrayOf(700, 700, 700))
            .setAutoCancel(true)
        if(largeIcon!=null){
            mBuilder.setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(largeIcon)
                .bigLargeIcon(null))
                .setLargeIcon(largeIcon)
        }

        val resultIntent = Intent(context, MainActivity::class.java).apply {
            flags =  Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ANIMAL_ID, animal.id!!)
        }
        val resultPendingIntent = PendingIntent.getActivity(
            context, PEN_INTENT_START_MAIN_REQUEST, resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)

        val deleteIntent = Intent(context, DeleteNotificationReceiver::class.java)
        deleteIntent.putExtra(ANIMAL_ID, animal.id)
        val delPendingIntent = PendingIntent.getBroadcast(context, animal.id!!.toInt(), deleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setDeleteIntent(delPendingIntent)

        val eatActionIntent = Intent(context, HistoryActivity::class.java).apply {
            putExtra(ANIMAL_ID, animal.id!!)
        }

        val eatActionPendingIntent = PendingIntent.getActivity(
            context, animal.id!!.toInt(), eatActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        mBuilder.addAction(R.drawable.ic_keyboard_tab_black_24dp, context.getString(R.string.eated), eatActionPendingIntent)

        if(animal.feedType != FeedType.AUTO_MULTIPLE_DAY) {
            val deferIntent = Intent(context, DeferReceiver::class.java).apply {
                putExtra(EXTRA_NOTIFICATION_ID, notifId)
                putExtra(ANIMAL_ID, animal!!.id)
            }
            val deferPendingIntent = PendingIntent.getBroadcast(
                context, animal.id!!.toInt(), deferIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            mBuilder.addAction(
                R.drawable.ic_keyboard_tab_black_24dp,
                context.getString(R.string.put_off_for_a_day),
                deferPendingIntent
            )
        }

        val notification = mBuilder.build()

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager!!.notify(notifId, notification)
    }

    private fun createAlarm(animal: Animal, context: Context){
        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EatReceiver::class.java)
        val animalId = animal.id!!
        intent.putExtra(ANIMAL_ID, animalId)
        val pIntent = PendingIntent.getBroadcast(context, animalId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

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