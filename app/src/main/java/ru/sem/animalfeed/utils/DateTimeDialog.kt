package ru.sem.animalfeed.utils

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.widget.DatePicker
import android.widget.TimePicker
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime


class DateTimeDialog(val context: Context) {

    interface OnDateSelectListener {
        fun onDateSelect(selectedDate: LocalDateTime)
    }

    interface OnTimeSelectListener {
        fun onDateSelect(selectedTime: LocalTime)
    }

    fun showDateDialog(listener: OnDateSelectListener) {
        showDateDialog(null, listener)
    }

    fun showDateDialog(date: LocalDateTime?, listener: OnDateSelectListener) {
        var c = date
        if(c==null) c = LocalDateTime.now()

        DatePickerDialog(
            context,
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

    fun showTimeDialog(listener: OnDateSelectListener) {
        showTimeDialog(null, listener)
    }

    fun showTimeDialog(date: LocalDateTime?, listener: OnDateSelectListener) {

        var c = date
        if(c==null) c = LocalDateTime.now()

        TimePickerDialog(
            context,
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

    fun showTimeDialog(date: LocalTime?, listener: OnTimeSelectListener, diffTimeValue: Long = 0L) {

        var c = date
        if(c==null){
            c = LocalTime.now()
            if(diffTimeValue!=0L) c = c!!.plusHours(diffTimeValue)
        }

        TimePickerDialog(
            context,
            OnTimeSetListener { view: TimePicker?, hourOfDay: Int, minute: Int ->
                val result = LocalTime.of(hourOfDay, minute)
                listener.onDateSelect(result)
            },
            c!!.hour,
            c!!.minute,
            true
        )
            .show()
    }
}