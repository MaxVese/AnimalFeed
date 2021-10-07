package ru.sem.animalfeed.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.fragment_main_page.*
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.ui.base.BaseFragment
import javax.inject.Inject


class MainPageFragment : BaseFragment(R.layout.fragment_main_page) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MainViewModel
    lateinit var adapter: GroupsPageAdapter
    lateinit var adapter2: GPA

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(false, toolbar)
        btnAddAnimal.inflate(R.menu.menu_main_speed_deal)
        pager.offscreenPageLimit = 1
        btnAddAnimal.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.action_add_animal -> findNavController().navigate(R.id.action_mainPageFragment_to_animalFragment)
                R.id.action_add_group -> findNavController().navigate(R.id.action_mainPageFragment_to_groupsFragment)
                R.id.action_add_brood -> findNavController().navigate(R.id.action_mainPageFragment_to_broodListFragment)
            }
            false
        })
        pager.isUserInputEnabled = false
    }

    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.groupWithNoGroupData.observe(viewLifecycleOwner, Observer {
            adapter = GroupsPageAdapter(it, requireActivity())
            //adapter.setData(it)
            pager.adapter = adapter
            TabLayoutMediator(tabsGroup, pager) { tab, position ->
                tab.text = it[position].name
                pager.setCurrentItem(tab.position, true)
            }.attach()
            /*adapter2 = GPA(it, requireActivity().supportFragmentManager)
            pager.adapter = adapter2
            tabsGroup.setupWithViewPager(pager)*/
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }
}