package ru.sem.animalfeed.ui.Brood

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.brood_fragment.*
import kotlinx.android.synthetic.main.brood_fragment.btnDateBorn
import kotlinx.android.synthetic.main.brood_fragment.btnSave
import kotlinx.android.synthetic.main.fragment_animal.*
import org.threeten.bp.LocalDateTime
import ru.sem.animalfeed.BROOD_ID
import ru.sem.animalfeed.MAX_AVA_WIDTH
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.ui.base.BaseTakePhotoFragment
import ru.sem.animalfeed.ui.main.MainViewModel
import ru.sem.animalfeed.utils.DateTimeDialog
import ru.sem.animalfeed.utils.DateUtilsA
import ru.sem.animalfeed.utils.ImageFilePath
import java.io.File
import java.io.IOException
import javax.inject.Inject

class BroodFragment : BaseTakePhotoFragment(R.layout.brood_fragment) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: BroodViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var dateTimeDialog: DateTimeDialog
    private val SEL_MALE = 11
    private val SEL_FEMALE = 22


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showHomeButton(true, toolbarBrood)
        btnSave.setOnClickListener {
            if(viewModel.brood?.broodName.equals("")) showAddName()
            else {
                viewModel.saveBrood()
                findNavController().popBackStack()
            }
        }
        imgPhotoMale.setOnClickListener{
            choosePhoto(SEL_MALE)
        }
        imgPhotoFemale.setOnClickListener{
            choosePhoto(SEL_FEMALE)
        }
        edKindMale.doAfterTextChanged {
            viewModel.updateMaleKind(it.toString())
        }
        edKindFemale.doAfterTextChanged {
            viewModel.updateFemaleKind(it.toString())
        }
        btnDateBrood.setOnClickListener {
            showDateBroodDlg(viewModel.broodCurrent.value?.masonryDate)
        }
        btnDateBorn.setOnClickListener {
            showDateBornDlg(viewModel.broodCurrent.value?.hatchingDate)
        }
    }

    private fun showAddName() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.brood_name)
        val viewInflated: View = LayoutInflater.from(requireContext()).inflate(
            R.layout.dlg_add_group,
            null
        )
        val input = viewInflated.findViewById<View>(R.id.edGroupName) as EditText
        input.hint = resources.getString(R.string.brood_name)
        builder.setView(viewInflated)
        builder.setPositiveButton(android.R.string.ok) { dialog, which ->
            viewModel.updateName(input.text.toString())
            hideSoftInput()
        }
        builder.setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    override fun onPhotoSelect(photoFileName: String?, selectId: Int) {
        if(photoFileName==null){
            showError("Ошибка дотупа к файлу $photoFileName", false)
            return
        }
        when(selectId){
            SEL_MALE -> viewModel.updateMalePhoto(photoFileName)
            SEL_FEMALE -> viewModel.updateFemalePhoto(photoFileName)
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
            BroodViewModel::class.java
        )
        mainViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(
            MainViewModel::class.java
        )
        dateTimeDialog = DateTimeDialog(requireContext())
        viewModel.initNewBrood(arguments?.getLong(BROOD_ID,-1) ?: -1,arguments?.getInt("pos",-1)?: 0)
        if(viewModel.brood?.broodName.equals("")) showAddName()
        viewModel.broodCurrent.observe(viewLifecycleOwner, Observer {
            if (it?.motherPhoto != null && it.motherPhoto!!.isNotEmpty() && File(it.motherPhoto).exists()) {
                val scale = ImageFilePath.getScaleSizeImage(File(it.motherPhoto), MAX_AVA_WIDTH)
                Picasso.get().load(File(it.motherPhoto))
                        .resize(scale.first, scale.second)
                        .error(R.drawable.ic_no_photo).into(imgPhotoFemale)
            }
            if (it?.fatherPhoto != null && it.fatherPhoto!!.isNotEmpty() && File(it.fatherPhoto).exists()) {
                val scale = ImageFilePath.getScaleSizeImage(File(it.fatherPhoto), MAX_AVA_WIDTH)
                Picasso.get().load(File(it.fatherPhoto))
                        .resize(scale.first, scale.second)
                        .error(R.drawable.ic_no_photo).into(imgPhotoMale)
            }
            if(it.fatherKind != null) edKindMale.setText(it.fatherKind)
            if(it.motherKind != null) edKindFemale.setText(it.motherKind)
            if (it?.masonryDate != null) {
                btnDateBrood.text = getDateFromLocalDateTime(it.masonryDate)
            }else{
                btnDateBrood.text = getString(R.string.date_brood)
            }
            if (it?.hatchingDate != null) {
                btnDateBorn.text = getDateFromLocalDateTime(it.hatchingDate)
            }else{
                btnDateBorn.text = getString(R.string.date_born)
            }
        })
    }

    fun getDateFromLocalDateTime(date: LocalDateTime?):String?{
        return "${date?.year}-${date?.monthValue}-${date?.dayOfMonth}"
    }

    private fun showDateBroodDlg(date: LocalDateTime?){
        dateTimeDialog.showDateDialog(
                date,
                object : DateTimeDialog.OnDateSelectListener {
                    override fun onDateSelect(selectedDate: LocalDateTime) {
                        viewModel.updateDateBrood(selectedDate)
                    }
                })
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


}