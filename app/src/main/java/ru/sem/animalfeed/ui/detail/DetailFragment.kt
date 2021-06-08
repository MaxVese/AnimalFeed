package ru.sem.animalfeed.ui.detail

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.fragment_detail.*
import ru.sem.animalfeed.R
import ru.sem.animalfeed.ui.base.BaseFragment
import ru.sem.animalfeed.ui.history.HistoryAdapter

class DetailFragment : BaseFragment(R.layout.fragment_detail) {

    override fun initialiseViewModel() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(true, toolbar)
        toolbar.title = ""
        rvHistoryDetail.adapter = HistoryAdapter()
    }
}