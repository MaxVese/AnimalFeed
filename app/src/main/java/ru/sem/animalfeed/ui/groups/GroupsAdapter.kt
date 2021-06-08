package ru.sem.animalfeed.ui.groups

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import ru.sem.animalfeed.R
import ru.sem.animalfeed.model.Group
import java.util.*


class GroupsAdapter : RecyclerView.Adapter<GroupsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    var listener : OnGroupItemClickListener? = null

    interface OnGroupItemClickListener {
        fun onGroupItemClick(group: Group)

        fun onDragComplete(map: Map<Long, Int>)

        fun onDeleteClicked(group: Group, pos: Int)
    }

    class SEDiffUtilCallback(private val oldList: List<Group>, private val newList: List<Group>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return this.oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            val oldItem: Group = oldList[oldItemPosition]
            val newItem: Group = newList[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            val oldItem: Group = oldList[oldItemPosition]
            val newItem: Group = newList[newItemPosition]
            return oldItem == newItem
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder{

        init {
            val container = itemView.findViewById<View>(R.id.containerGroup)
            container.setOnClickListener{
                if(data!=null) {
                    listener?.onGroupItemClick(data!![adapterPosition])
                }
            }
            itemView.isLongClickable = true

            val delete = itemView.findViewById<RelativeLayout>(R.id.delete)

            val swipeReveal = itemView.findViewById<SwipeRevealLayout>(R.id.swipereveal)
            swipeReveal.setSwipeListener(object : SwipeRevealLayout.SimpleSwipeListener() {
                override fun onOpened(view: SwipeRevealLayout) {
                    super.onOpened(view)
                    delete.isEnabled = true
                }

                override fun onClosed(view: SwipeRevealLayout) {
                    super.onClosed(view)
                    delete.isEnabled = false
                }
            })
            delete.setOnClickListener { v: View? ->
                AlertDialog.Builder(itemView.context)
                    .setMessage(R.string.shure_to_delete)
                    .setOnCancelListener { dialog -> swipeReveal.close(true) }
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        dialog.dismiss()
                        swipeReveal.close(true)
                        listener?.onDeleteClicked(data!![adapterPosition], adapterPosition)
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which -> dialog.cancel() }
                    .create().show()
            }
        }
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgDrag = itemView.findViewById<ImageView>(R.id.imgDrag)
        val container = itemView.findViewById<View>(R.id.containerGroup)
        //val delete = itemView.findViewById<RelativeLayout>(R.id.delete)

        override fun onItemSelected(context: Context) {
            container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
            tvName.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            imgDrag.setColorFilter(
                ContextCompat.getColor(context, android.R.color.white),
                PorterDuff.Mode.SRC_IN
            )
        }

        override fun onItemClear(context: Context) {
            container.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            imgDrag.setColorFilter(
                ContextCompat.getColor(context, R.color.grey2),
                PorterDuff.Mode.SRC_IN
            )
            tvName.setTextColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    private var data: List<Group>? = null

    fun setData(dataNew: List<Group>) {
        if (data == null) {
            data = dataNew
            notifyItemRangeInserted(0, dataNew.size)
        } else {
            val result = DiffUtil.calculateDiff(
                SEDiffUtilCallback(data!!, dataNew)
            )
            data = dataNew
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): GroupsAdapter.ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.item_group,
            viewGroup, false
        )
        return ViewHolder(v)
    }

    override fun getItemCount() = if(data==null) 0 else data!!.size

    override fun onBindViewHolder(holder: GroupsAdapter.ViewHolder, position: Int) {
        val item = data!![position]
        holder.run {
            tvName.text = item.name
        }
    }

    public fun getPositionsNew(): Map<Long, Int>{
        val map = HashMap<Long, Int>()
        for ((pos, item) in data!!.withIndex()){
            map[item.id!!] = pos
        }
        return map
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(data, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(data, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        /*val map = HashMap<Long, Int>()
        for ((pos, item) in data!!.withIndex()){
            map[item.id!!] = pos
        }
        listener?.onDragComplete(map)*/
        //return true
    }

    override fun onItemDismiss(position: Int) {

    }
}