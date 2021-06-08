package ru.sem.animalfeed.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.android.AndroidInjection
import ru.sem.animalfeed.db.AppDatabase
import ru.sem.animalfeed.utils.AnimalSyncUtil
import javax.inject.Inject

class BootNotifyReceiver : BroadcastReceiver() {

    companion object{
        private val TAG = "BootNotifyReceiver"
    }

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context);
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val animalSyncUtil = AnimalSyncUtil(appDatabase.getAnimalDao(), appDatabase.getHistoryDao(), context)
            try {
                animalSyncUtil.sync()
            }catch (e: Exception){
                Log.e(TAG, "error sync when boot completed", e)
            }
        }
    }
}