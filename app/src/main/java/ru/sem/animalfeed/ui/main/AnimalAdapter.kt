package ru.sem.animalfeed.ui.main

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.squareup.picasso.Picasso
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.MAX_AVA_HEIGHT
import ru.sem.animalfeed.MAX_AVA_WIDTH
import ru.sem.animalfeed.R
import ru.sem.animalfeed.TRANSACTION_PHOTO
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.model.FeedType
import ru.sem.animalfeed.model.Gender
import ru.sem.animalfeed.utils.DateUtilsA
import java.io.File


class AnimalAdapter(var context: Context) : RecyclerView.Adapter<AnimalAdapter.ViewHolder>(){

    var listener : OnAnimalItemClickListener? = null

    interface OnAnimalItemClickListener {
        fun onAnimalItemClick(animal: Animal, imageView: ImageView)
        fun onAnimalEatClick(animal: Animal?)
        fun onHistoryBtnClick(animalId: Long?)
        fun onAnimalDeleteClick(animal: Animal, position: Int)
    }

    companion object {
        const val EXTRA_TITLE="title"
        const val EXTRA_KIND="kind"
        const val EXTRA_IMG="img_main"
        const val EXTRA_GENDER="gender"
        const val EXTRA_DATE="dt"
    }

    class SEDiffUtilCallback(private val oldList: List<Animal>, private val newList: List<Animal>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem: Animal = oldList[oldItemPosition]
            val newItem: Animal = newList[newItemPosition]
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem: Animal = oldList[oldItemPosition]
            val newItem: Animal = newList[newItemPosition]
            return oldItem == newItem
        }

        /*override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
            val oldItem: Animal = oldList[oldItemPosition]
            val newItem: Animal = newList[newItemPosition]

            val payload = Bundle()
            if (oldItem.name != newItem.name) {
                payload.putString(EXTRA_TITLE, newItem.name)
            }
            if (oldItem.kind != newItem.kind) {
                payload.putString(EXTRA_KIND, newItem.kind)
            }
            if (oldItem.photo != newItem.photo) {
                payload.putString(EXTRA_IMG, newItem.photo)
            }
            if (oldItem.gender != newItem.gender) {
                newItem.gender?.name?.let { payload.putString(EXTRA_GENDER, it) }
            }

            return if (payload.isEmpty) {
                null // do full binding
            } else {
                payload
            }
        }*/
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        init {
            val container = itemView.findViewById<View>(R.id.containerAnimal)
            val imgMain = itemView.findViewById<ImageView>(R.id.imgMain)
            container.setOnClickListener{
                if(data!=null) {
                    listener?.onAnimalItemClick(data!![layoutPosition], imgMain)
                }
            }
            itemView.isLongClickable = true
            val btnDelete = itemView.findViewById<ImageButton>(R.id.btnDelete)
            val btnHistory = itemView.findViewById<ImageButton>(R.id.btnHistory)
            val swipeReveal = itemView.findViewById<SwipeRevealLayout>(R.id.swipereveal)
            val delete = itemView.findViewById<RelativeLayout>(R.id.delete)

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


            btnDelete.setOnClickListener{
                listener?.onAnimalEatClick(data?.get(layoutPosition))
            }
            btnHistory.setOnClickListener{
                listener?.onHistoryBtnClick(data?.get(layoutPosition)?.id)
            }
            delete.setOnClickListener { v: View? ->
                AlertDialog.Builder(itemView.context)
                    .setMessage(R.string.shure_to_delete)
                    .setOnCancelListener { dialog -> swipeReveal.close(true) }
                    .setPositiveButton(android.R.string.yes) { dialog, which ->
                        dialog.dismiss()
                        swipeReveal.close(true)
                        listener?.onAnimalDeleteClick(data!![layoutPosition], layoutPosition)
                    }
                    .setNegativeButton(android.R.string.no) { dialog, which -> dialog.cancel() }
                    .create().show()
            }
        }
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
        val imgMain = itemView.findViewById<ImageView>(R.id.imgMain)
        val tvKind = itemView.findViewById<TextView>(R.id.tvKind)
        val imgGender = itemView.findViewById<ImageView>(R.id.imgGender)
        val tvFeedType = itemView.findViewById<TextView>(R.id.tvFeedType)
        val tvLastFeed = itemView.findViewById<TextView>(R.id.tvLastFeed)

    }

    private var data: List<Animal>? = null

    fun setData(dataNew: List<Animal>) {
        if (data == null) {
            data = dataNew
            notifyItemRangeInserted(0, dataNew.size)
        } else {
            val result = DiffUtil.calculateDiff(SEDiffUtilCallback(data!!, dataNew))
            data = dataNew
            result.dispatchUpdatesTo(this)
            //data!!.toMutableList().clear()
            //data!!.toMutableList().addAll(dtFiltered)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v: View = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.item_animal,
            viewGroup, false
        )
        return ViewHolder(v)
    }

    override fun getItemCount() = if(data==null) 0 else data!!.size

    private fun getImgGender(gender: Gender?): Int{
        return when (gender) {
            Gender.MALE -> R.drawable.ic_gender_male
            Gender.FEMALE -> R.drawable.ic_gender_female
            else -> R.drawable.ic_gender_unknown
        }
    }

    private fun getFeedDateText(animal: Animal, context: Context):String{
        val now = LocalDate.now()
        val next = animal.nextFeed!!.toLocalDate()
        /*when(animal.isHand){
            false -> tvDate.text = "Напомним ${DateUtilsA.formatD.format(animal.nextFeed)} в ${DateUtilsA.formatT.format(animal.nextFeed)}"
            true -> tvDate.text = "Покормите ${DateUtilsA.formatD.format(animal.nextFeed)} в ${DateUtilsA.formatT.format(animal.nextFeed)}"
        }*/
        val inW = context.getString(R.string.in_word)
        val d= context.getString(R.string.today)
        return if(now.isEqual(next)) "${context.getString(R.string.today)}\n$inW ${DateUtilsA.formatT.format(animal.nextFeed)}"
        else if(now.plusDays(1).isEqual(next)) "${context.getString(R.string.tomorrow)}\n$inW ${DateUtilsA.formatT.format(animal.nextFeed)}"
        //else if(DateUtilsA.isLocalDateInTheSameWeek(now, next)) DateUtilsA.formatD2.format(animal.nextFeed)
        else DateUtilsA.formatDFull.format(animal.nextFeed)
    }

    private fun getFeedTypeString(animal: Animal, context: Context): String {
        return when(animal.feedType){
            FeedType.HAND -> context.getString(R.string.auto_hand)
            FeedType.AUTO_SINGLE_DAY -> context.getString(R.string.auto_s)
            else -> context.getString(R.string.auto_m)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = data!![position]

        holder.run {
            imgMain.transitionName = TRANSACTION_PHOTO+animal.id
            tvName.text = animal.name
            tvDate.text = getFeedDateText(animal, holder.itemView.context)
            /*when(animal.isHand){
                false -> tvDate.text = "Напомним ${DateUtilsA.formatD.format(animal.nextFeed)} в ${DateUtilsA.formatT.format(animal.nextFeed)}"
                true -> tvDate.text = "Покормите ${DateUtilsA.formatD.format(animal.nextFeed)} в ${DateUtilsA.formatT.format(animal.nextFeed)}"
            }*/

            when(animal.feedType!!){
                FeedType.AUTO_MULTIPLE_DAY -> {
                    Log.d("mylog","auto multiple " + animal.lastFeed + " " + LocalDateTime.now().with(animal.morning))
                    if(animal.lastFeed?.withNano(0)?.withSecond(0)?.withMinute(0) == LocalDateTime.now().with(animal.morning)?.withNano(0)?.withSecond(0)?.withMinute(0)){
                        tvLastFeed.visibility = View.VISIBLE
                        tvLastFeed.text = context.getString(R.string.fed_morning)
                    }
                    if(animal.lastFeed?.withNano(0)?.withSecond(0)?.withMinute(0) == LocalDateTime.now().with(animal.evening)?.withNano(0)?.withSecond(0)?.withMinute(0)){
                        tvLastFeed.visibility = View.VISIBLE
                        tvLastFeed.text = context.getString(R.string.fed_evening)
                    }
                }else -> {
                    if(animal.lastFeed?.toLocalDate() == LocalDate.now()){
                        tvLastFeed.visibility = View.VISIBLE
                        tvLastFeed.text = context.getString(R.string.fed_today)
                    }else{
                        tvLastFeed.visibility = View.GONE
                        tvLastFeed.text = ""
                    }
                }
            }


            tvKind.text =animal.kind
            //imgGender.setImageResource(getImgGender(animal.gender))
            if(animal.isHand){
                tvDate.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_bowl, 0)
            } else{
                tvDate.setCompoundDrawablesWithIntrinsicBounds(0, 0,  R.drawable.ic_eat_time, 0)
            }

            tvFeedType.text = getFeedTypeString(animal, itemView.context)
            tvName.setCompoundDrawablesWithIntrinsicBounds(getImgGender(animal.gender), 0, 0, 0);
            animal.photo?.let {
                //val bmImg = BitmapFactory.decodeFile(animal.photo)

                Picasso.get().load(File(animal.photo))
                    .resize(MAX_AVA_WIDTH, MAX_AVA_HEIGHT)
                    .error(R.drawable.ic_no_photo).into(imgMain)
                //imgMain.setImageBitmap(ImageFilePath.getThumbnail(animal.photo))
            } ?: run {
                imgMain.setImageResource(R.drawable.ic_no_photo)
            }

            if(LocalDateTime.now().dayOfMonth == animal.nextFeed!!.dayOfMonth || LocalDateTime.now().isAfter(animal.nextFeed!!)){
                tvDate.setTextColor(Color.RED);
            }else {
                tvDate.setTextColor(Color.parseColor("#999999"));
            }
        };
    }

    /*override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        val bundle = payloads[0] as Bundle
        val newTitle = bundle.getString(EXTRA_TITLE)
        val newKind = bundle.getString(EXTRA_KIND)
        val photo = bundle.getString(EXTRA_IMG)
        val gender = bundle.getString(EXTRA_GENDER)

        if(newTitle!=null) holder.tvName.text = newTitle
        if(newKind!=null) holder.tvKind.text = newKind
        if(photo!=null){
            val bmImg = BitmapFactory.decodeFile(photo)
            holder.imgMain.setImageBitmap(bmImg)
        }
        if(gender!=null){
            holder.tvName.setCompoundDrawablesWithIntrinsicBounds(getImgGender(Gender.valueOf(gender)), 0, 0, 0);
        }
    }*/
}