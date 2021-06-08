package ru.sem.animalfeed.ui.history

import android.Manifest
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.leinardi.android.speeddial.SpeedDialView
import kotlinx.android.synthetic.main.fragment_history.*
import ru.sem.animalfeed.*
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.HType
import ru.sem.animalfeed.model.History
import ru.sem.animalfeed.model.ResourceStatus
import ru.sem.animalfeed.ui.base.DateTimePickerFragment
import ru.sem.animalfeed.utils.ContextMenuRecyclerView
import ru.sem.animalfeed.utils.SwipeToDeleteCallback
import javax.inject.Inject


class HistoryFragment : DateTimePickerFragment(R.layout.fragment_history), HistoryAdapter.OnHistoryItemClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: HistoryViewModel
    var adapter: HistoryAdapter? = null
    var snackbar: Snackbar? =null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        showHomeButton(true, toolbar)

        speedDialView.inflate(R.menu.menu_history_speed_deal)
        speedDialView.setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
            when (actionItem.id) {
                R.id.action_another -> editItem(-1, HType.DEFAULT, viewModel.animalId!!)
                R.id.action_eat -> editItem(-1, HType.EAT, viewModel.animalId!!)
                R.id.action_linka -> editItem(-1, HType.LINKA, viewModel.animalId!!)
                R.id.action_size -> editItem(-1, HType.ZAMER, viewModel.animalId!!)
                R.id.action_wight -> editItem(-1, HType.WEIGHT, viewModel.animalId!!)
            }
            false
        })
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)

        // Inflate Menu from xml resource
        val menuInflater = requireActivity().menuInflater
        menuInflater.inflate(R.menu.animals_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerContextMenuInfo
        //Toast.makeText(activity, " User selected something ${info.position}", Toast.LENGTH_LONG).show()
        viewModel.delItem(info.position)
        return false
    }

    private fun editItem(id: Long, hType: HType, animId: Long){
        val args = Bundle().apply {
            putLong(EXTRA_HISTORY_ID, id)
            putInt(EXTRA_H_TYPE, hType.ordinal)
            putLong(ANIMAL_ID, animId)
        }
        findNavController().navigate(R.id.action_historyFragment_to_editHistoryFragment, args)
    }

    override fun onHistoryItemClick(history: History) {
        editItem(history.id!!, history.htype!!, history.animalId!!)
    }

    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            HistoryViewModel::class.java
        )
        viewModel.historyData.observe(viewLifecycleOwner, Observer {
            if(adapter==null){
                adapter = HistoryAdapter()
                adapter!!.listener = this
            }
            if(rvHistory.adapter == null){
                rvHistory.adapter = adapter
                rvHistory.addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
                registerForContextMenu(rvHistory)
                /*val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        //viewModel.delItem(viewHolder.adapterPosition)
                        removeHistory(viewModel.hide(viewHolder.adapterPosition))
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeHandler)
                itemTouchHelper.attachToRecyclerView(rvHistory)*/
            }
            adapter!!.setData(it)
            if(it.isEmpty()) tvEmpty.visibility = View.VISIBLE else tvEmpty.visibility = View.GONE
            /*rvHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy<0 && !speedDialView.isShown)
                        speedDialView.show()
                    else if(dy>0 && speedDialView.isShown)
                        speedDialView.hide()
                }
            })*/
        })
        viewModel.reportData.observe(viewLifecycleOwner, Observer {
            when(it.status){
                ResourceStatus.ERROR -> showError(it.message)
                else -> showError(it.data)
        }
        })
    }

    private fun removeHistory(history: History){
        snackbar = Snackbar
            .make(rvHistory, "${history.info} ${getString(R.string.removed)}", Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(android.R.string.cancel)) {
                viewModel.undoDelete(history)
            }
            .setActionTextColor(Color.MAGENTA)
            .setDuration(DEFAULT_REMOVE_UNDO_DURATION)
            .addCallback( object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    Log.d("MAIN FRAGMENT", "onDismissed: $event")
                    when(event){
                        DISMISS_EVENT_CONSECUTIVE,
                        DISMISS_EVENT_MANUAL,
                        DISMISS_EVENT_SWIPE,
                        DISMISS_EVENT_TIMEOUT  ->{
                            viewModel.delItem(history)
                        }
                    }
                }
            })
        snackbar!!.show()
    }

    override fun onDeleteClicked(history: History, pos: Int) {
        viewModel.delItem(history)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.history_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions : Map<String, Boolean> ->
            // Do something if some permissions granted or denied
            var granted = true
             permissions.entries.forEach {
                // Do checking here
                Log.d("permissions", "${it.key}: ${it.value}")
                granted = it.value
            }
            if(granted) viewModel.exportCSV()
        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_report -> {
                //
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
                true
            }
            R.id.action_filter ->{
                val filterFragment = FilterFragment()
                filterFragment.show((requireActivity() as AppCompatActivity).supportFragmentManager, "filter")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}