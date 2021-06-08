package ru.sem.animalfeed.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_filter.*
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.utils.DateTimeDialog
import ru.sem.animalfeed.utils.DateUtilsA
import javax.inject.Inject


class FilterFragment : BottomSheetDialogFragment() {

    companion object{
        const val TAG ="AddPlaceFragment"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: HistoryViewModel
    lateinit var dateTimeDialog: DateTimeDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViewModel()
        chAllTime.setOnCheckedChangeListener { cb , isChecked ->
            viewModel.updateAllTime(isChecked)
            btnStart.isEnabled = !isChecked
            btnEnd.isEnabled = !isChecked
            when(isChecked){
                true ->{
                    /*btnEnd.visibility = View.GONE
                    btnStart.visibility = View.GONE
                    textView2.visibility = View.GONE
                    textView3.visibility = View.GONE*/
                }
                false ->{
                    /*btnEnd.visibility = View.VISIBLE
                    btnStart.visibility = View.VISIBLE
                    textView2.visibility = View.VISIBLE
                    textView3.visibility = View.VISIBLE*/
                }
            }
        }
        spType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, pos: Int, id: Long) {
                val hType: HType? = when(pos){
                    1 -> HType.EAT
                    2 -> HType.ZAMER
                    3 -> HType.LINKA
                    4 -> HType.WEIGHT
                    5 -> HType.DEFAULT
                    else -> null
                }
                viewModel.updateHType(hType)
            }
        }

        btnStart.setOnClickListener{
            dateTimeDialog.showDateDialog(viewModel.queryData.value?.startDate, object:
                DateTimeDialog.OnDateSelectListener {
                override fun onDateSelect(selectedDate: LocalDateTime) {
                    viewModel.updateStartDate(selectedDate)
                }
            })
        }
        btnEnd.setOnClickListener{
            dateTimeDialog.showDateDialog(viewModel.queryData.value?.endDate, object:
                DateTimeDialog.OnDateSelectListener {
                override fun onDateSelect(selectedDate: LocalDateTime) {
                    viewModel.updateEndDate(selectedDate)
                }
            })
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v: View = inflater.inflate(R.layout.fragment_filter, null)
        return v
    }

    private fun initialiseViewModel(){
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            HistoryViewModel::class.java
        )
        dateTimeDialog = DateTimeDialog(requireContext());
        viewModel.queryData.observe(viewLifecycleOwner, Observer {
            chAllTime.isChecked = it.allTime
            btnStart.text = DateUtilsA.formatD.format(it.startDate)
            btnEnd.text = DateUtilsA.formatD.format(it.endDate)
            when(it.hType.hType){
                HType.EAT -> spType.setSelection(1)
                HType.ZAMER -> spType.setSelection(2)
                HType.LINKA -> spType.setSelection(3)
                HType.WEIGHT -> spType.setSelection(4)
                HType.DEFAULT -> spType.setSelection(5)
                else -> spType.setSelection(0)
            }
        })
    }
}