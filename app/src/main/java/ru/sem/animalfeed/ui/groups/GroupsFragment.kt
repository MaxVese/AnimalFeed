package ru.sem.animalfeed.ui.groups

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_groups.*
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.Group
import ru.sem.animalfeed.ui.base.BaseFragment
import ru.sem.animalfeed.ui.main.MainViewModel
import javax.inject.Inject


class GroupsFragment : BaseFragment(R.layout.fragment_groups), GroupsAdapter.OnGroupItemClickListener {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MainViewModel
    var adapter: GroupsAdapter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true ) {
            override fun handleOnBackPressed() {
                if(adapter!=null) {
                    viewModel.saveNewGroupsOrder(adapter!!.getPositionsNew())
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
        toolbar3.title = getString(R.string.groups)
        btnAddGroup.setOnClickListener {
            showAddDlg(null)
        }
    }



    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)
        viewModel.groupsData.observe(viewLifecycleOwner, Observer {
            if (adapter == null) {
                adapter = GroupsAdapter()
                adapter!!.listener = this
            }
            if (rvGroups.adapter == null) {
                rvGroups.adapter = adapter
                rvGroups.addItemDecoration(
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
                touchHelper.attachToRecyclerView(rvGroups)
            }
            adapter!!.setData(it)
            if (it.isEmpty()) tvEmpty.visibility = View.VISIBLE else tvEmpty.visibility = View.GONE
        })
    }

    /*override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)

        val menuInflater = requireActivity().menuInflater
        menuInflater.inflate(R.menu.animals_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerContextMenuInfo
        viewModel.deleteGroup(info.position)
        return false
    }*/

    private fun showAddDlg(group: Group?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.group_name)
        val viewInflated: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.dlg_add_group,
            null
        )
        val input = viewInflated.findViewById<View>(R.id.edGroupName) as EditText
        input.setText(group?.name ?: "")
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            val newGroup: Group
            if(group==null){
                newGroup = Group(adapter?.itemCount ?: 0, input.text.toString())
            }else{
                newGroup = Group(group.pos, input.text.toString())
                newGroup.id = group.id
            }
            if(viewModel.isGroupExist(newGroup)){
                showError(getString(R.string.this_group_already_exist))
                hideSoftInput()
                return@setPositiveButton
            }
            viewModel.saveGroup(newGroup)
            hideSoftInput()
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    override fun onGroupItemClick(group: Group) {
        showAddDlg(group)
    }

    override fun onDragComplete(items: Map<Long, Int>) {
        viewModel.saveNewGroupsOrder(items)
    }

    override fun onDeleteClicked(group: Group, pos: Int) {
        viewModel.deleteGroup(group)
    }
}