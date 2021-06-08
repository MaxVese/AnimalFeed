package ru.sem.animalfeed.ui.base

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.alarm.EatReceiver


open class BaseViewModel : ViewModel() {



    /*private val compositeDisposable = CompositeDisposable()

    protected fun unsubscribeOnDestroy(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }*/

    override fun onCleared() {
        Log.d("BASE VM", "onCleared")
        super.onCleared()
       // compositeDisposable.clear()
    }


    protected fun cancelOldAlarm(id: Long?, context: Context){
        if(id==null) return
        val am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, EatReceiver::class.java)
        intent.putExtra(ANIMAL_ID, id)
        val pIntent = PendingIntent.getBroadcast(context, id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        am.cancel(pIntent)//отменяем то, что было ранее
    }

}