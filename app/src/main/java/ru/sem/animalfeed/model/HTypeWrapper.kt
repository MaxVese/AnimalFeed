package ru.sem.animalfeed.model

class HTypeWrapper(var hType: HType?) {

    fun apply(): Array<Int> {
        return if(hType==null) arrayOf(0, 1, 2, 3, 4) else arrayOf(hType!!.ordinal)
    }
}