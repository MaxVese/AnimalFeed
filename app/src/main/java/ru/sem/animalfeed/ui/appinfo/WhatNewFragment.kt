package ru.sem.animalfeed.ui.appinfo


import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.sem.animalfeed.R
import ru.sem.animalfeed.ui.main.MainActivity


class WhatNewFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout to use as dialog or embedded fragment
        dialog?.setTitle(getString(R.string.please_read))
        return inflater.inflate(R.layout.fragment_what_new, container, false)
    }

    /** The system calls this only when creating the layout in a dialog.  */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            .setCustomTitle(getCustomText(getString(R.string.please_read)))
            .setPositiveButton(R.string.i_read_carefully, null)
            .setView(R.layout.fragment_what_new)
            //.setMessage(android.R.string.ok)
        return adb.create()
    }

    fun getCustomText(msg: String):TextView{
        val myMsg = TextView(requireContext())
        myMsg.text = msg
        myMsg.gravity = Gravity.CENTER_HORIZONTAL
        myMsg.textSize = 18f
        myMsg.setTextColor(Color.BLACK)
        return myMsg
    }



    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (requireActivity() as MainActivity).onDismissWhatNew()
    }
}