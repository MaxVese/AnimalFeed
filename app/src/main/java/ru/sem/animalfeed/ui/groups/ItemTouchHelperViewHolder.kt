package ru.sem.animalfeed.ui.groups

import android.content.Context

interface ItemTouchHelperViewHolder {

    fun onItemSelected(context: Context)
    fun onItemClear(context: Context)
}