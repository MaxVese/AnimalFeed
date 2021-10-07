package ru.sem.animalfeed.ui.Brood

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_brood_list.*
import ru.sem.animalfeed.BROOD_ID
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.Brood
import ru.sem.animalfeed.ui.base.BaseFragment
import ru.sem.animalfeed.ui.groups.DragAndSwipeItemTouchHelperCallback
import ru.sem.animalfeed.ui.main.MainViewModel
import javax.inject.Inject

class BroodListFragment : BaseFragment(R.layout.fragment_brood_list), BroodsAdapter.OnGroupItemClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MainViewModel
    var adapter: BroodsAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                if(adapter!=null) {
                    viewModel.saveNewBroodsOrder(adapter!!.getPositionsNew())
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
                this,  // LifecycleOwner
                callback
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(true, toolbar3)
        toolbar3.title = getString(R.string.broods)
        btnAddBrood.setOnClickListener {
            findNavController().navigate(R.id.action_broodListFragment_to_broodFragment, bundleOf("pos" to (adapter?.itemCount ?: 0).toInt()))
        }
    }

    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.broodData.observe(viewLifecycleOwner, Observer {
            if (adapter == null) {
                adapter = BroodsAdapter()
                adapter!!.listener = this
            }
            if (rvBroods.adapter == null) {
                rvBroods.adapter = adapter
                rvBroods.addItemDecoration(
                    DividerItemDecoration(
                        requireContext(),
                        RecyclerView.VERTICAL
                    )
                )
                //registerForContextMenu(rvGroups)
                /*val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        //removeHistory(viewModel.hide(viewHolder.adapterPosition))
                        viewModel.deleteGroup(viewHolder.adapterPosition)
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeHandler)
                itemTouchHelper.attachToRecyclerView(rvGroups)*/

                val callback = DragAndSwipeItemTouchHelperCallback(
                    adapter!!,
                    requireContext(),
                    false
                )
                val touchHelper = ItemTouchHelper(callback)
                touchHelper.attachToRecyclerView(rvBroods)
            }
            adapter!!.setData(it)
            if (it.isEmpty()) tvEmpty.visibility = View.VISIBLE else tvEmpty.visibility = View.GONE
        })
    }

    override fun onGroupItemClick(brood: Brood) {
        findNavController().navigate(R.id.action_broodListFragment_to_broodFragment, bundleOf(BROOD_ID to brood.id))
    }



    override fun onDragComplete(map: Map<Long, Int>) {
        viewModel.saveNewBroodsOrder(map)
    }


    override fun onDeleteClicked(brood: Brood, pos: Int) {
        viewModel.deleteBrood(brood)
    }

    fun showLog(str:String){
        Log.d("mylog",str)
    }

}