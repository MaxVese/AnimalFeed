package ru.sem.animalfeed.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import ru.sem.animalfeed.model.Group

class GPA(val data:List<Group>, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getCount() = data.size


    override fun getItem(position: Int): Fragment {
        return MainFragment.newInstance(data[position].id)
    }

    override fun getPageTitle(position: Int) = data[position].name
}