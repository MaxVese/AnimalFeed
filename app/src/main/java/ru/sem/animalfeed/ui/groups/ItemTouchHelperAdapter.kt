package ru.sem.animalfeed.ui.groups

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int)

//    fun onItemMoveComplete(fromPosition: Int, toPosition: Int)

    fun onItemDismiss(position: Int)
}