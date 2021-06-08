package ru.sem.animalfeed.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.sem.animalfeed.model.Group

class GroupsPageAdapter(val data:List<Group>, activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val pageIds= data.map { it.hashCode().toLong() }


    override fun getItemId(position: Int): Long {
        return data[position].hashCode().toLong() // make sure notifyDataSetChanged() works
    }

    override fun containsItem(itemId: Long): Boolean {
        return pageIds.contains(itemId)
    }

    override fun getItemCount() =  data.size

    override fun createFragment(position: Int):Fragment {
        return MainFragment.newInstance(data[position].id)
    }

}