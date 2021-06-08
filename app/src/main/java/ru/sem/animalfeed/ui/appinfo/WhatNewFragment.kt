package ru.sem.animalfeed.ui.appinfo


import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        dialog?.setTitle(getString(R.string.what_new_in_version));
        return inflater.inflate(R.layout.fragment_what_new, container, false)
    }

    /** The system calls this only when creating the layout in a dialog.  */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val adb: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            .setTitle(R.string.what_new_in_version).setPositiveButton(android.R.string.ok, null)
            .setView(R.layout.fragment_what_new)
            //.setMessage(android.R.string.ok)
        return adb.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (requireActivity() as MainActivity).onDismissWhatNew()
    }
}