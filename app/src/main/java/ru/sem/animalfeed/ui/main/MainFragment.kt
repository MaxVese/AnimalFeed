package ru.sem.animalfeed.ui.main

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_groups.*
import kotlinx.android.synthetic.main.fragment_main.*
import ru.sem.animalfeed.*
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.Animal
import ru.sem.animalfeed.ui.base.BaseFragment
import ru.sem.animalfeed.ui.groups.DragAndSwipeItemTouchHelperCallback
import ru.sem.animalfeed.ui.history.HistoryActivity
import ru.sem.animalfeed.utils.ContextMenuRecyclerView.RecyclerContextMenuInfo
import ru.sem.animalfeed.utils.VerticalSpaceItemDecoration
import javax.inject.Inject


class MainFragment : BaseFragment(R.layout.fragment_main), AnimalAdapter.OnAnimalItemClickListener {

    companion object {
        const val TAG = "MainFragment"
        fun newInstance(grpId: Long?): MainFragment{
            val arguments = bundleOf(
                EXTRA_GROUP_ID to grpId
            )
            val result = MainFragment()
            result.arguments = arguments
            return result
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MainViewModel
    var snackbar: Snackbar? = null

    var adapter: AnimalAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater = requireActivity().menuInflater
        menuInflater.inflate(R.menu.animals_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as RecyclerContextMenuInfo
        viewModel.delAnimal(info.position)
        return false
    }

    override fun initialiseViewModel() {
        if(resources.getBoolean(R.bool.is_free)){
            Log.d("mylog","free")
        }else{
            Log.d("mylog","pro")
        }
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        viewModel.animalData.observe(viewLifecycleOwner, Observer {
            if (adapter == null) {
                adapter = AnimalAdapter(requireContext())
                adapter!!.listener = this
            }
            if (rvAnimal.adapter == null) {
                rvAnimal.adapter = adapter
                rvAnimal.addItemDecoration(VerticalSpaceItemDecoration(12))
                registerForContextMenu(rvAnimal)
                /*val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        removeAnimal(viewModel.hide(viewHolder.adapterPosition))
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeHandler)
                itemTouchHelper.attachToRecyclerView(rvAnimal)*/

//                val callback = DragAndSwipeItemTouchHelperCallback(
//                        adapter!!,
//                        requireContext(),
//                        false
//                )
//                val touchHelper = ItemTouchHelper(callback)
//                touchHelper.attachToRecyclerView(rvAnimal)
            }
            Log.d(TAG, "initialiseViewModel: setdata")
            adapter!!.setData(it)
            /*rvAnimal.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy<0 && !btnAddAnimal.isShown)
                        btnAddAnimal.show()
                    else if(dy>0 && btnAddAnimal.isShown)
                        btnAddAnimal.hide()
                }
            })*/
        })
        viewModel.customMessageData.observe(viewLifecycleOwner, Observer {
            showError(it)
        })

        /*viewModel.groupsData.observe(viewLifecycleOwner, Observer {
            val argId= requireArguments().getLong(EXTRA_GROUP_ID, -1)
            Log.d(TAG, "initialiseViewModel: set groupID $argId")
            textView9.text = "grpId=$argId"
            viewModel.setGroupId(if (argId == -1L) null else argId)
        })*/

        val argId= requireArguments().getLong(EXTRA_GROUP_ID, -1)
        viewModel.setGroupId(if (argId == -1L) null else argId)
    }

    private fun removeAnimal(animal: Animal){
        snackbar = Snackbar
            .make(rvAnimal, "${animal.name} ${getString(R.string.removed)}", Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(android.R.string.cancel)) {
                viewModel.undoDelete(animal)
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
                        viewModel.delAnimal(animal)
                    }
                }
            }
        })
        snackbar!!.show()
    }

    override fun onAnimalDeleteClick(animal: Animal, position: Int) {
        viewModel.delAnimal(animal)
    }

//    override fun onDragComplete(firstPos: Long, secondPos: Long) {
//        viewModel.switchPosition(firstPos,secondPos)
//    }

    override fun onAnimalItemClick(animal: Animal, imageView: ImageView) {
        val bundle =  Bundle()
        bundle.putLong(ANIMAL_ID, animal.id!!)
        val transactionTitle =  TRANSACTION_PHOTO+animal.id!!
        val extras = FragmentNavigatorExtras(
            imageView to transactionTitle)
        findNavController().navigate(R.id.action_mainPageFragment_to_animalFragment, bundle)
       // findNavController().navigate(R.id.action_mainFragment_to_animalFragment, bundle, null, extras)
    }

    override fun onAnimalEatClick(animal: Animal?) {
        //сейчас эта функция кормления
        animal.let {
            viewModel.handEat(it!!)
        }
    }

    override fun onHistoryBtnClick(animalId: Long?) {
        val intent = Intent(requireActivity(), HistoryActivity::class.java)
        intent.putExtra(ANIMAL_ID, animalId)
        startActivity(intent)
    }
}
