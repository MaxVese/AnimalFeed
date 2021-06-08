package ru.sem.animalfeed.ui.history

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_edit_history.*
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.EXTRA_HISTORY_ID
import ru.sem.animalfeed.EXTRA_H_TYPE
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.ui.base.DateTimePickerFragment
import ru.sem.animalfeed.utils.DateUtilsA
import ru.sem.animalfeed.utils.DecimalDigitsInputFilter
import javax.inject.Inject


class EditHistoryFragment : DateTimePickerFragment(R.layout.fragment_edit_history) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: HistoryViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(true, toolbar2)
        btnDate.setOnClickListener{
            showDateDialog(viewModel.historyCurrentData.value?.date, object: OnDateSelectListener{
                /*override fun onDateSelect(calendar: Calendar?) {
                    viewModel.updateDate(calendar!!.time)
                }*/
                override fun onDateSelect(selectedDate: LocalDateTime) {
                    viewModel.updateDate(selectedDate)
                }
            })
        }

        btnTime.setOnClickListener{
            showTimeDialog(viewModel.historyCurrentData.value?.date, object: OnDateSelectListener{
                /*override fun onDateSelect(calendar: Calendar?) {
                    viewModel.updateDate(calendar!!.time)
                }*/
                override fun onDateSelect(selectedDate: LocalDateTime) {
                    viewModel.updateDate(selectedDate)
                }
            })
        }

        edEditH.doAfterTextChanged {
            if(it.toString().isNotEmpty()){
                viewModel.updateCurrentHistInfo(it.toString())
            }
        }
        btnSave.setOnClickListener{
            viewModel.saveCurrentHistory()
            hideSoftInput()
            findNavController().navigateUp()
        }
    }

    private fun initViews(hType: HType){
        when(hType){
            HType.EAT -> {
                tiEditH.hint = getString(R.string.what_did_you_feed)
                edEditH.inputType = InputType.TYPE_CLASS_TEXT
            }
            HType.LINKA -> {
                tiEditH.hint = getString(R.string.information)
                edEditH.setText(getString(R.string.molt))
                edEditH.inputType = InputType.TYPE_CLASS_TEXT
            }
            HType.ZAMER ->{
                tiEditH.hint = getString(R.string.enter_value_in_cm)
                edEditH.inputType =InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                edEditH.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(4, 2))
            }

            HType.WEIGHT ->{
                tiEditH.hint = getString(R.string.enter_weight_in_grams)
                edEditH.inputType =InputType.TYPE_CLASS_NUMBER
                //edEditH.filters = arrayOf<InputFilter>(DecimalDigitsInputFilter(6, 0))
            }
            else ->{
                tiEditH.hint = getString(R.string.information)
                edEditH.inputType = InputType.TYPE_CLASS_TEXT
            }
        }
    }

    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            HistoryViewModel::class.java
        )
        viewModel.historyCurrentData.observe(viewLifecycleOwner, Observer {
            it.let {
                initViews(it!!.htype!!)
                edEditH.setText(it.info)
                btnDate.text = DateUtilsA.formatD.format(it.date)
                btnTime.text = DateUtilsA.formatT.format(it.date)
                val hType = HType.values()[requireArguments().getInt(EXTRA_H_TYPE, 0)]
                initViews(hType)
            }
        })
        viewModel.loadById(requireArguments().getLong(EXTRA_HISTORY_ID, -1), HType.values()[requireArguments().getInt(
            EXTRA_H_TYPE,0)], requireArguments().getLong(ANIMAL_ID, 0))
    }
}