package ru.sem.animalfeed.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.android.AndroidInjection
import ru.sem.animalfeed.db.AppDatabase
import javax.inject.Inject

class DeleteNotificationReceiver : BroadcastReceiver() {
    companion object{
        const val TAG ="DeleteNotRec"
    }

    @Inject
    lateinit var appDatabase: AppDatabase

    override fun onReceive(context: Context?, intent: Intent?) {
        AndroidInjection.inject(this, context);
        Log.d(TAG, "onReceive: delete notification")
        /*val animal = appDatabase.getAnimalDao().getById(intent!!.getLongExtra(ANIMAL_ID, -1)) ?: return
        val cal = Calendar.getInstance()
        cal.time =animal!!.lastFeed
        cal.add(Calendar.DAY_OF_MONTH, animal!!.interval)
        animal.lastFeed = cal.time
        animal.nextFeed = animal.calcNextFeedDate()
        appDatabase.getAnimalDao().update(animal)*/
    }
}