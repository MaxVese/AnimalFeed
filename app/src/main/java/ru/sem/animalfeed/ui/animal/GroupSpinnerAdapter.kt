package ru.sem.animalfeed.ui.animal

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import ru.sem.animalfeed.model.Group


class GroupSpinnerAdapter(context: Context, resource: Int, val groups: List<Group>) : ArrayAdapter<Group>(
    context,
    resource,
    groups
) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        val item: Group? = getItem(position)
        label.text = item?.name ?: "Unknown group"
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = super.getView(position, convertView, parent) as TextView
        val item: Group? = getItem(position)

        label.text = item?.name ?: "Unknown group"
        return label
    }

    fun getPosById(id: Long?): Int{
        for ((pos, item) in groups.withIndex()){
            if(item.id == id){
                return pos
                break
            }
        }
        return 0
    }

    override fun getItemId(position: Int): Long {
        return groups[position].id ?: -1
    }
}