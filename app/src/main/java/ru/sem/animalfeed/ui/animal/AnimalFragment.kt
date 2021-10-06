package ru.sem.animalfeed.ui.animal


import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_animal.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import ru.sem.animalfeed.App
import ru.sem.animalfeed.MAX_AVA_WIDTH
import ru.sem.animalfeed.R
import ru.sem.animalfeed.TRANSACTION_PHOTO
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.model.FeedType
import ru.sem.animalfeed.model.Gender
import ru.sem.animalfeed.ui.appinfo.WhatNewFragment
import ru.sem.animalfeed.ui.base.BaseTakePhotoFragment
import ru.sem.animalfeed.ui.free.FreeVersionFragment
import ru.sem.animalfeed.ui.main.MainViewModel
import ru.sem.animalfeed.utils.DateTimeDialog
import ru.sem.animalfeed.utils.DateUtilsA
import ru.sem.animalfeed.utils.ImageFilePath
import java.io.File
import java.io.IOException
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 */
class AnimalFragment : BaseTakePhotoFragment(R.layout.fragment_animal) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: AnimalViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var dateTimeDialog: DateTimeDialog
    lateinit var groupAdapter: GroupSpinnerAdapter
    private val SEL_MAIN = 1
    private val SEL_GALLERY = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(true, toolbarAnimal)
        btnSave.setOnClickListener{
            if(edName.text!!.isEmpty()||edKind.text!!.isEmpty()||edInterval.text!!.isEmpty()){
                showError(getString(R.string.set_all_fields))
                return@setOnClickListener
            }
            if(spFeedType.selectedItemPosition == FeedType.AUTO_MULTIPLE_DAY.ordinal){
                if(!viewModel.checkMorningBeforeEvening()){
                    showError(getString(R.string.time_mornin_mat_before_evening))
                    return@setOnClickListener
                }
            }
            //viewModel.updateGroup(groupAdapter.getItem(spGroups.selectedItemPosition)?.id)
            if(resources.getBoolean(R.bool.is_free) && viewModel.getAmountAnimals() >= 3 && !requireArguments().containsKey("anim_id")){
                FreeVersionFragment().show(parentFragmentManager, null)
            }else {
                viewModel.saveAnimal()
            }
            hideSoftInput()
            findNavController().navigateUp()
        }
        btnDateBorn.setOnClickListener {
            //chooseDateBurn()
            showDateBornDlg(viewModel.currentAnimal.value?.birthDay)
        }
        btnDate.setOnClickListener{
            dateTimeDialog.showDateDialog(
                viewModel.currentAnimal.value?.lastFeed,
                object : DateTimeDialog.OnDateSelectListener {
                    /*override fun onDateSelect(calendar: Calendar) {
                    viewModel.updateLastFeed(calendar.time)
                }*/
                    override fun onDateSelect(selectedDate: LocalDateTime) {
                        viewModel.updateLastFeed(selectedDate)
                    }
                })
        }

        btnTime.setOnClickListener{
            dateTimeDialog.showTimeDialog(
                viewModel.currentAnimal.value?.lastFeed,
                object : DateTimeDialog.OnDateSelectListener {
                    /*override fun onDateSelect(calendar: Calendar) {
                    viewModel.updateLastFeed(calendar.time)
                }*/
                    override fun onDateSelect(selectedDate: LocalDateTime) {
                        viewModel.updateLastFeed(selectedDate)
                    }
                })
        }

        btnMorning.setOnClickListener{
            dateTimeDialog.showTimeDialog(
                viewModel.currentAnimal.value?.morning,
                object : DateTimeDialog.OnTimeSelectListener {
                    override fun onDateSelect(selectedTime: LocalTime) {
                        viewModel.updateMorning(selectedTime)
                    }
                })
        }

        btnEvening.setOnClickListener{
            dateTimeDialog.showTimeDialog(
                viewModel.currentAnimal.value?.evening,
                object : DateTimeDialog.OnTimeSelectListener {
                    override fun onDateSelect(selectedTime: LocalTime) {
                        viewModel.updateEvening(selectedTime)
                    }
                }, App.DEFAULT_EVENING_UP_HOURS)
        }

        edName.doAfterTextChanged {
            viewModel.updateName(it.toString())
        }
        edKind.doAfterTextChanged {
            viewModel.updateKind(it.toString())
        }
        edInterval.doAfterTextChanged {
            if(it.toString().isNotEmpty()){
                viewModel.updateInterval(it.toString().toInt())
            }
        }
        edNote.doAfterTextChanged {
            viewModel.updateNote(it.toString())
        }
        rbMale.setOnClickListener{
            viewModel.updateGender(Gender.MALE)
        }
        rbFemale.setOnClickListener{
            viewModel.updateGender(Gender.FEMALE)
        }
        rbGenderUnknown.setOnClickListener{
            viewModel.updateGender(Gender.UNKNOWN)
        }

        imgPhoto.setOnClickListener{
            choosePhoto(SEL_MAIN)
        }
    }

    private val onGroupItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            Log.d(TAG, "onItemSelected: groupId=$id")
            viewModel.updateGroup(if(id==-1L) null else id)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private val feedTypeOnSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            tvFeedTypeInfo.text = resources.getStringArray(R.array.feed_type_info)[position]
            val feedType = FeedType.values()[position]
            viewModel.updateFeedType(feedType)
            when(feedType){
                FeedType.AUTO_SINGLE_DAY -> {
                    tiInterval.visibility = View.VISIBLE
                    llMultipleFeed.visibility = View.GONE
                    showLastFeedButtons(true)
                }
                FeedType.AUTO_MULTIPLE_DAY ->{
                    tiInterval.visibility = View.GONE
                    llMultipleFeed.visibility = View.VISIBLE
                    viewModel.updateInterval(1)//кормление два раза в день
                    viewModel.updateLastFeed(LocalDateTime.now())
                    showLastFeedButtons(false)

                }
                FeedType.HAND ->{
                    tiInterval.visibility = View.VISIBLE
                    llMultipleFeed.visibility = View.GONE
                    viewModel.updateInterval(1)
                    showLastFeedButtons(true)
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    private fun getAnimalGender(): Gender? {
        if (rbFemale.isChecked) return Gender.FEMALE
        return if (rbMale.isChecked) Gender.MALE else Gender.UNKNOWN
    }

    override fun onPhotoSelect(photoFileName: String?, selectId: Int) {
        Log.d(TAG, "onPhotoSelect: $photoFileName")
        if(photoFileName==null){
            showError("Ошибка дотупа к файлу $photoFileName", false)
            return
        }
        when(selectId){
            SEL_MAIN -> viewModel.updatePhoto(photoFileName)
            //SEL_GALLERY -> viewModel.addToGallery(photoFileName)
        }
    }

    private fun showLastFeedButtons(show: Boolean){
        if(!show){
            textView.visibility = View.GONE
            btnDate.visibility = View.GONE
            btnTime.visibility = View.GONE
        }else{
            textView.visibility = View.VISIBLE
            btnDate.visibility = View.VISIBLE
            btnTime.visibility = View.VISIBLE
        }
    }

    override fun cameraIntentFileName(): File? {
        val storageDir: File? = requireContext()
            .getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var image: File? = null
        try {
            image = File.createTempFile(
                "cameraP",  /* prefix */
                ".jpg",  /* suffix */
                storageDir /* directory */
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    override fun getAppPictureDir(): File? {
        return requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    }

    override fun initialiseViewModel() {
        viewModel = ViewModelProvider(this, viewModelFactory).get(
            AnimalViewModel::class.java
        )
        mainViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            MainViewModel::class.java
        )
        dateTimeDialog = DateTimeDialog(requireContext());
        viewModel.currentAnimal.observe(viewLifecycleOwner, Observer {
            imgPhoto.transitionName = TRANSACTION_PHOTO + it.id
            edName.setText(it?.name)
            edKind.setText(it?.kind)
            edNote.setText(it?.note)
            toolbarAnimal.title = it?.name ?: getString(R.string.app_name)
            if (it?.lastFeed == null) {
                btnDate.text = LocalDateTime.now().format(DateUtilsA.formatD)
            } else {
                btnDate.text = it.lastFeed!!.format(DateUtilsA.formatD)
            }
            if (it?.birthDay != null) {
                val p = DateUtilsA.calculateTime(it.birthDay!!)
                btnDateBorn.text = "${getString(R.string.age)} ${p.years} ${DateUtilsA.getYearString(p.years)} ${p.months} мес. ${p.days} дней"
            }else{
                btnDateBorn.text = getString(R.string.date_born)
            }

            if (it?.lastFeed == null) {
                btnTime.text = LocalDateTime.now().format(DateUtilsA.formatT)
            } else {
                btnTime.text = it.lastFeed!!.format(DateUtilsA.formatT)
            }
            /*btnDate.text = DateUtilsA.formatD.format(if(it?.lastFeed !=null) it?.lastFeed else Calendar.getInstance().time)
            btnTime.text = DateUtilsA.formatT.format(if(it?.lastFeed !=null) it?.lastFeed else Calendar.getInstance().time)*/
            edInterval.setText(it?.interval?.toString())
            when (it?.gender) {
                Gender.MALE -> rbMale.isChecked = true
                Gender.FEMALE -> rbFemale.isChecked = true
                else -> rbGenderUnknown.isChecked = true
            }

            if (it?.photo != null && it.photo!!.isNotEmpty() && File(it.photo).exists()) {
                val scale = ImageFilePath.getScaleSizeImage(File(it.photo), MAX_AVA_WIDTH)
                Picasso.get().load(File(it.photo))
                    .resize(scale.first, scale.second)
                    .error(R.drawable.ic_no_photo).into(imgPhoto)
            }

            if (this::groupAdapter.isInitialized) {
                spGroups.setSelection(groupAdapter.getPosById(it.groupId))
            }

            btnMorning.text = if(it.morning == null) DateUtilsA.formatT.format(LocalTime.now()) else DateUtilsA.formatT.format(
                it.morning!!)
            btnEvening.text = if(it.evening == null) DateUtilsA.formatT.format(LocalTime.now().plusHours(
                App.DEFAULT_EVENING_UP_HOURS)) else DateUtilsA.formatT.format(
                it.evening!!)
            try {
                spGroups.setSelection(groupAdapter.getPosById(it.groupId))
            }catch (e: Exception){}

            spFeedType.onItemSelectedListener = feedTypeOnSelectedListener
            spGroups.onItemSelectedListener = onGroupItemSelectedListener
            spFeedType.setSelection(it!!.feedType.ordinal, false)

        })
        mainViewModel.groupWithNoGroupData.observe(viewLifecycleOwner, Observer {
            groupAdapter = GroupSpinnerAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                it
            )
            spGroups.adapter = groupAdapter
            viewModel.loadAnimal(requireArguments().getLong("anim_id", -1))
        })
    }

    private fun chooseDateBurn(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.set_age))
            .setItems(R.array.date_born) { dialogInterface, i ->
                when(i){
                    0 -> showDateBornDlg(viewModel.currentAnimal.value?.birthDay)
                    1 -> showAgeDlg(viewModel.currentAnimal.value?.birthDay)
                }
            }
            .show()
    }

    private fun showDateBornDlg(date: LocalDateTime?){
        dateTimeDialog.showDateDialog(
            date,
            object : DateTimeDialog.OnDateSelectListener {
                override fun onDateSelect(selectedDate: LocalDateTime) {
                    viewModel.updateDateBorn(selectedDate)
                }
            })
    }

    private fun showAgeDlg(date: LocalDateTime?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.age)
        val viewInflated: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.dlg_set_age,
            null
        )
        val years = if(date==null){
            0
        } else{
             ChronoUnit.YEARS.between(date, LocalDateTime.now())
        }

        val input = viewInflated.findViewById<View>(R.id.edGroupName) as EditText
        input.setText(years.toString())
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            val dateBorn: LocalDateTime? =
            if(input.text.toString().isNotEmpty()){
                LocalDateTime.now().minusYears(input.text.toString().toLong())
            }else{
               null
            }
            viewModel.updateDateBorn(dateBorn)
            hideSoftInput()
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }
}
