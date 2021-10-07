package ru.sem.animalfeed.ui.Brood

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.db.dao.AnimalDao
import ru.sem.animalfeed.db.dao.BroodDao
import ru.sem.animalfeed.db.dao.GroupsDao
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.Brood
import ru.sem.animalfeed.ui.base.BaseViewModel
import javax.inject.Inject

class BroodViewModel  @Inject constructor(val broodDao: BroodDao,
                                          val context: Context)
: BaseViewModel() {

    var broodCurrent: MutableLiveData<Brood> = MutableLiveData()
    var brood:Brood? = null

    fun saveBrood(){
        if(brood!!.id==null) {
            broodDao.insert(brood!!)
        }else{
            broodDao.update(brood!!)
        }
    }

    fun initNewBrood(id:Long,pos:Int){
        if(brood==null) {
            brood = broodDao.getById(id) ?: Brood(pos)
        }
        if(broodCurrent.value==null){
            broodCurrent.value = brood
        }
    }

    fun updateName(name:String){
        if(brood != null) {
            brood!!.broodName = name
        }
        if(broodCurrent.value==null){
            broodCurrent.value = brood
        }
    }

    fun updateMalePhoto(fileName: String?){
        if(brood!!.fatherPhoto == fileName){
            return
        }
        brood!!.fatherPhoto = fileName
        broodCurrent.value = brood
    }

    fun updateFemalePhoto(fileName: String?){
        if(brood!!.motherPhoto == fileName){
            return
        }
        brood!!.motherPhoto = fileName
        broodCurrent.value = brood
    }

    fun updateMaleKind(name: String){
        if(brood!!.fatherKind == name){
            return
        }
        brood!!.fatherKind = name
    }

    fun updateFemaleKind(name: String){
        if(brood!!.motherKind == name){
            return
        }
        brood!!.motherKind = name
    }

    fun updateDateBrood(date: LocalDateTime?){
        brood?.masonryDate = date
        broodCurrent.value = brood
    }

    fun updateDateBorn(date: LocalDateTime?){
        brood?.hatchingDate = date
        broodCurrent.value = brood
    }




}