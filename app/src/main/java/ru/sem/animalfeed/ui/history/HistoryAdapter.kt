package ru.sem.animalfeed.ui.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import ru.sem.animalfeed.R
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.model.History
import ru.sem.animalfeed.utils.DateUtilsA


class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>(){

    var listener : OnHistoryItemClickListener? = null

    interface OnHistoryItemClickListener {
        fun onHistoryItemClick(history: History)
        fun onDeleteClicked(history: History, pos: Int)
    }

    class SEDiffUtilCallback(private val oldList: List<History>, private val newList: List<History>) : DiffUtil.Callback() {

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
            val oldItem: History = oldList[oldItemPosition]
            val newItem: History = newList[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            val oldItem: History = oldList[oldItemPosition]
            val newItem: History = newList[newItemPosition]
            return oldItem == newItem
        }
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        init {
            val container = itemView.findViewById<View>(R.id.main_hist)
            container.setOnClickListener{
                if(data!=null) {
                    listener?.onHistoryItemClick(data!![adapterPosition])
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
                        listener?.onDeleteClicked(data!![absoluteAdapterPosition], absoluteAdapterPosition)
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which -> dialog.cancel() }
                    .create().show()
            }

        }
        val tvName = itemView.findViewById<TextView>(R.id.tvHistoryName)
        val tvDate = itemView.findViewById<TextView>(R.id.tvHistoryDate)
        val tvType = itemView.findViewById<TextView>(R.id.tvType)

    }

    private var data: List<History>? = null

    fun setData(dataNew: List<History>) {
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

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.item_history,
            viewGroup, false
        )
        return ViewHolder(v)
    }

    override fun getItemCount() = if(data==null) 0 else data!!.size

    private fun getType(hType: HType?, context: Context): String {
        return when(hType){
            HType.EAT -> context.getString(R.string.feeding)
            HType.LINKA -> context.getString(R.string.molt)
            HType.ZAMER -> context.getString(R.string.froze)
            HType.WEIGHT -> context.getString(R.string.weight)
            else -> context.getString(R.string.another)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = data!![position]
        holder.run {
            when(history.htype){
                HType.ZAMER -> tvName.text = "${holder.itemView.context.getString(R.string.froze_long)} ${history.info} " +
                        "${holder.itemView.context.getString(R.string.cm)}"
                HType.WEIGHT -> tvName.text = "${holder.itemView.context.getString(R.string.weight)} ${history.info} " +
                        "${holder.itemView.context.getString(R.string.gram)}"
                else -> tvName.text = history.info
            }

            tvDate.text = DateUtilsA.formatDT.format(history.date)
            tvType.text = getType(history.htype, holder.itemView.context)
        };
    }
}