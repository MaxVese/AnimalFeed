package ru.sem.animalfeed.ui.base

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.widget.DatePicker
import android.widget.TimePicker
import org.threeten.bp.LocalDateTime

abstract class DateTimePickerFragment : BaseFragment{

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    interface OnDateSelectListener {
        fun onDateSelect(selectedDate: LocalDateTime)
    }

    protected open fun showDateDialog(listener: OnDateSelectListener) {
        showDateDialog(null, listener)
    }

    protected open fun showDateDialog(date: LocalDateTime?, listener: OnDateSelectListener) {
        var c = date
        if(c==null) c = LocalDateTime.now()
        DatePickerDialog(
            requireActivity(),
            OnDateSetListener { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val result = LocalDateTime.of(year, month+1, dayOfMonth, c!!.hour, c!!.minute)
                listener.onDateSelect(result)
            },
            c!!.year,
            c!!.month.value -1,
            c!!.dayOfMonth
        )
            .show()
    }

    protected open fun showTimeDialog(listener: OnDateSelectListener) {
        showTimeDialog(null, listener)
    }

    protected open fun showTimeDialog(date: LocalDateTime?, listener: OnDateSelectListener) {
        var c = date
        if(c==null) c = LocalDateTime.now()
        TimePickerDialog(
            requireActivity(),
            OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
                val result = LocalDateTime.of(c!!.year, c!!.month, c!!.dayOfMonth, hourOfDay, minute)
                listener.onDateSelect(result)
            },
            c!!.hour,
            c!!.minute,
            true
        )
            .show()
    }
}