package ru.sem.animalfeed.ui.base

open class BaseUndoViewModel : BaseViewModel() {

    val undoEvent = SingleLiveEvent<String>()

    var undoObject: Any? = null

    fun makeUndoEvent(undoObject: Any, message: String){
        this.undoObject = undoObject
        undoEvent.value = message
    }
}