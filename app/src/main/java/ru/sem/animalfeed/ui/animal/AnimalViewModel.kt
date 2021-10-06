package ru.sem.animalfeed.ui.animal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import androidx.lifecycle.MutableLiveData
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.App
import ru.sem.animalfeed.alarm.EatReceiver
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.GroupsDao
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.FeedType
import ru.sem.animalfeed.model.Gender
import ru.sem.animalfeed.model.Group
import ru.sem.animalfeed.ui.base.BaseViewModel
import ru.sem.animalfeed.utils.DateUtilsA
import javax.inject.Inject

class AnimalViewModel @Inject constructor(val animalDao: AnimalDao, val context: Context,
                                          private val groupsDao: GroupsDao): BaseViewModel() {

    companion object{
        const val TAG = "AnimalViewModel"
    }

    val currentAnimal: MutableLiveData<Animal> = MutableLiveData()
    var animal: Animal? = null

    val groupData = MutableLiveData<List<Group>>()// = groupsDao.getAllWithNoGroup(context.getString(R.string.no_group))

    private val currentId = MutableLiveData<Long>()

    fun saveAnimal(){
        //currentAnimal.value = animal
        when(animal!!.feedType!!){
            FeedType.AUTO_MULTIPLE_DAY ->{
                val nextFeed =
                    DateUtilsA.calculateNextFeedMorningOrEvening(animal!!.lastFeed, animal!!.morning, animal!!.evening)
                animal!!.nextFeed = nextFeed
            }
            else ->{
                animal!!.nextFeed = animal!!.lastFeed!!.plusDays(animal!!.interval.toLong())
            }
        }
        if(animal!!.id==null){
            val id = animalDao.insert(animal!!)
//            animalDao.setPos(id,id)
            if(!animal!!.isHand) createAlarm(id)
        }else{
            animalDao.update(animal!!)
            if(!animal!!.isHand){
                createAlarm(animal!!.id!!)
            }else{
                cancelOldAlarm(animal!!.id!!, context)
            }
        }
    }

    fun loadAnimal(id: Long){
        currentId.value = id
        if(animal==null) {
            animal = animalDao.getById(id) ?: Animal.createEmpty()
        }
        if(currentAnimal.value==null){
            currentAnimal.value = animal
        }//!!может надо убрать?
    }

    fun getAmountAnimals(): Int{
        return animalDao.getAmount();
    }

    fun updateLastFeed(date: LocalDateTime){
        animal!!.lastFeed = date
        currentAnimal.value = animal
    }

    fun updateMorning(date: LocalTime){
        animal!!.morning = date
        currentAnimal.value = animal
    }

    fun updateEvening(date: LocalTime){
        animal!!.evening = date
        currentAnimal.value = animal
    }

    fun updateDateBorn(date: LocalDateTime?){
        animal?.birthDay = date
        currentAnimal.value = animal
    }

    fun checkMorningBeforeEvening(): Boolean{
        if(animal?.morning == null) animal!!.morning = LocalTime.now()
        if(animal?.evening == null) animal!!.evening = LocalTime.now().plusHours(App.DEFAULT_EVENING_UP_HOURS)

        return animal!!.morning!!.isBefore(animal!!.evening!!)
    }

    fun updateName(name: String){
        if(animal!!.name == name){
            return
        }
        animal!!.name = name
    }

    fun updateGroup(groupId: Long?){
        if(animal!!.groupId == groupId){
            return
        }
        animal!!.groupId = groupId
        currentAnimal.value = animal
    }

    fun updateKind(name: String){
        if(animal!!.kind == name){
            return
        }
        animal!!.kind = name
    }

    fun updateNote(name: String){
        if(animal?.note == name){
            return
        }
        animal?.note = name
    }

    fun updateInterval(interval: Int){
        if(animal!!.interval == interval){
            return
        }
        animal!!.interval = interval
    }

    fun updateGender(gender: Gender){
        if(animal!!.gender == gender){
            return
        }
        animal!!.gender = gender
    }

    fun updateFeedType(feedType: FeedType){
        if(animal!!.feedType == feedType){
            return
        }
        animal!!.feedType = feedType
        updateIsHand(feedType==FeedType.HAND)
    }

    fun updateIsHand(isHand: Boolean){
        if(animal!!.isHand == isHand){
            return
        }
        animal!!.isHand = isHand
    }

    fun updatePhoto(fileName: String?){
        if(animal!!.photo == fileName){
            return
        }
        animal!!.photo = fileName
        currentAnimal.value = animal
    }


    private fun createAlarm(animalId: Long){
        if(animal!=null) {
            val am: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, EatReceiver::class.java)
            intent.putExtra(ANIMAL_ID, animalId)
            val pIntent = PendingIntent.getBroadcast(context, animalId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT)

            /*val cal = Calendar.getInstance()
            cal.time = animal!!.lastFeed!!
            cal.add(Calendar.DAY_OF_MONTH, animal!!.interval)*/

            if(animal!!.id!=null) {
                am.cancel(pIntent)//отменяем то, что было ранее
            }
            if(!animal!!.isHand) {

                val timeToAlarm =
                    animal!!.nextFeed!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

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
    }
}