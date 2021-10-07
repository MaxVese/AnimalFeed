package ru.sem.animalfeed

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import ru.sem.animalfeed.component.AppComponent
import ru.sem.animalfeed.component.DaggerAppComponent
import javax.inject.Inject


class App : Application(), HasAndroidInjector {
    companion object{
        val TAG = "APP"
        const val DEFAULT_EVENING_UP_HOURS = 8L
    }


    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    lateinit var appComponent: AppComponent

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        appComponent = DaggerAppComponent.builder()
                .application(this)
                .build()
        appComponent.inject(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel: NotificationChannel?
        notificationChannel = notificationManager
            .getNotificationChannel(MAIN_CHANNEL_ID)
        Log.d(TAG, "createNotificationChannels: $notificationChannel")
        if (notificationChannel == null) {
            Log.d(TAG, "createNotificationChannels: $MAIN_CHANNEL_ID")
            val importance = NotificationManager.IMPORTANCE_HIGH
            val chanelAll = NotificationChannel(
                MAIN_CHANNEL_ID,
                MAIN_CHANNEL_NAME,
                importance
            )
            chanelAll.enableVibration(true)
            chanelAll.vibrationPattern = longArrayOf(700, 700, 700)
            notificationManager.createNotificationChannel(chanelAll)
        }
    }
}

const val MAIN_CHANNEL_ID = "main"
const val MAIN_CHANNEL_NAME = "Кормление"
const val ANIMAL_ID = "anim_id"
const val BROOD_ID = "brood_id"
const val EXTRA_ANIMAL_NAME = "anim_name"
const val PEN_INTENT_START_MAIN_REQUEST = 0
const val EXTRA_NOTIFICATION_ID = "not_id"
const val EXTRA_HISTORY_ID = "his_id"
const val EXTRA_H_TYPE= "h_type"
const val DEFER_DAY_COUNT = 1
const val EXTRA_GROUP_ID = "group_id"
const val EXTRA_POWER_DLG = "power_dlg"
const val DEFAULT_REMOVE_UNDO_DURATION = 5000
const val TRANSACTION_PHOTO = "tp_"
const val MAX_AVA_WIDTH = 200
const val MAX_AVA_HEIGHT = 200