package ru.sem.animalfeed.ui.base;


import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.eazypermissions.common.model.PermissionResult
import com.eazypermissions.livedatapermission.PermissionManager
import ru.sem.animalfeed.BuildConfig
import ru.sem.animalfeed.R
import ru.sem.animalfeed.utils.ImageFilePath
import java.io.File
import java.io.IOException


abstract class BaseTakePhotoFragment: BaseFragment, PermissionManager.PermissionObserver {

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)


    companion object{
        const val TAG = "TakePhoto"
    }

    protected val permAll = arrayOf(Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
    )

    protected val permGallery = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    protected val REQUEST_PERM_PHOTO = 6
    protected val REQUEST_PERM_GALLERY = 7
    protected var currentPhotoFileName = ""
    private val REQUEST_TAKE_PHOTO = 4
    private val REQUEST_TAKE_GALLERY = 5
    private var selectID = 0

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (currentPhotoFileName != null) {
            outState.putString("currentPhotoFile", currentPhotoFileName)
            outState.putInt("selectId", selectID)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            try {
                currentPhotoFileName = savedInstanceState.getString("currentPhotoFile")!!
                selectID = savedInstanceState.getInt("selectId", 0)
            } catch (ignored: Exception) {
            }
        }
    }

    abstract fun onPhotoSelect(photoFileName: String?, selectId: Int)

    abstract fun cameraIntentFileName(): File?

    abstract fun getAppPictureDir(): File?

    override fun setupObserver(permissionResultLiveData: LiveData<PermissionResult>) {
        permissionResultLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is PermissionResult.PermissionGranted -> {
                    when(it.requestCode){
                        REQUEST_PERM_PHOTO -> {
                            dispatchTakePictureIntent(cameraIntentFileName())
                        }
                        REQUEST_PERM_GALLERY -> {
                            galleryIntent()
                        }
                    }
                }
                is PermissionResult.PermissionDenied -> {
                    Toast.makeText(requireContext(), "Необходимые права не получены", Toast.LENGTH_SHORT).show()
                }
                is PermissionResult.ShowRational -> {
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                        .setMessage("Для получения изображений, необходимы разрешения")
                        .setTitle("Загрузка изображений")
                        .setNegativeButton("Отмена") { dialog, _ ->
                            dialog.dismiss()
                        }
                    when (it.requestCode) {
                        REQUEST_PERM_PHOTO -> {
                            alertDialogBuilder
                                .setPositiveButton("OK") { _, _ ->
                                    PermissionManager.requestPermissions(
                                        this,
                                        REQUEST_PERM_PHOTO,
                                        *permAll
                                    )
                                }.create().show()
                        }
                        REQUEST_PERM_GALLERY -> {
                            alertDialogBuilder
                                .setPositiveButton("OK") { _, _ ->
                                    PermissionManager.requestPermissions(
                                        this,
                                        REQUEST_PERM_GALLERY,
                                        *permAll
                                    )
                                }.create().show()
                        }
                    }
                }
                is PermissionResult.PermissionDeniedPermanently -> {
                    Toast.makeText(requireContext(), "Необходимые права не получены", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    protected fun choosePhoto(selectId: Int){
        this.selectID = selectId
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.choose_image)
            .setItems(R.array.take_photo) { dialogInterface, i ->
                when(i){
                    0 -> PermissionManager.requestPermissions(this, REQUEST_PERM_GALLERY, *permGallery)
                    1 -> PermissionManager.requestPermissions(this, REQUEST_PERM_PHOTO, *permAll)
                }
            }
            .show()
    }

    protected open fun grantUriPerm(intent: Intent, uri: Uri?) {
        val resInfoList = requireContext().packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            requireContext().grantUriPermission(
                packageName, uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    protected open fun galleryIntent() { //AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Выбирете фото"), REQUEST_TAKE_GALLERY)
    }

    private fun dispatchTakePictureIntent(newCameraFile: File?) {
        if (newCameraFile == null) {
            return
        }
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) { // Create the File where the photo should go
            val photoFile: File = newCameraFile //presenter.createImageFile();
            currentPhotoFileName = photoFile.absolutePath
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID +".fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_GALLERY && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            currentPhotoFileName = ImageFilePath.getLocalPath(context, uri)
            Log.d(TAG, "onActivityResult: gallery $currentPhotoFileName")
            try {
                val dest = ImageFilePath.copyToAppDir(
                    File(currentPhotoFileName),
                    getAppPictureDir()!!.absolutePath,
                    false,
                    requireContext()
                )
                onPhotoSelect(dest.absolutePath, this.selectID)
            } catch (e: IOException) {
                e.printStackTrace()
                showError("Ошибка создания файла", false)
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: camera $currentPhotoFileName")
            var dest: File? = null //getCopiedFile(new File(currentPhotoFileName));
            try { /*dest = ImageFilePath
                        .resizeImage(new File(currentPhotoFileName), App.MAX_WIDTH_IMAGE);*/
                dest = File(currentPhotoFileName)
                Log.d(TAG, "onActivityResult: dest=" + dest.absolutePath)
                currentPhotoFileName = dest.absolutePath
                //ImageFilePath.resizeImage(File(currentPhotoFileName), ImageFilePath.DEFAULT_IMAGE_WIDTH, requireContext())
                onPhotoSelect(currentPhotoFileName, this.selectID)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Toast.makeText(activity, "Ошибка обработки файла", Toast.LENGTH_SHORT).show()
                return
            }
        }
    }
}