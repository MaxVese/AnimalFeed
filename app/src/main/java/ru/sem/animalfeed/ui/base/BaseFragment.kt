package ru.sem.animalfeed.ui.base

import android.R
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import androidx.annotation.Nullable
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection


abstract class BaseFragment : Fragment {

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)


    open fun showProgress(show: Boolean){}

    abstract fun initialiseViewModel()

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initialiseViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }


    open fun showError(text: String?, showErrorView: Boolean = false) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    protected fun hideSoftInput() {
        try {
            val view: View? = requireActivity().currentFocus
            val imm: InputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        } catch (ignored: Exception) {
        }
    }

    open fun showHomeButton(show: Boolean, toolbar: Toolbar?) {
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        showHomeButton(show)
    }

    open fun showHomeButton(show: Boolean) {
        val actionBar: ActionBar? = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(show)
        actionBar?.setHomeButtonEnabled(show)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}