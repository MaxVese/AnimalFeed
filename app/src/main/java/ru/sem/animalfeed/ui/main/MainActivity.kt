package ru.sem.animalfeed.ui.main

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.thelittlefireman.appkillermanager.managers.KillerManager
import dagger.android.AndroidInjection
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.EXTRA_POWER_DLG
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import ru.sem.animalfeed.ui.appinfo.WhatNewFragment
import javax.inject.Inject


class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    companion object{
        const val TAG = "MainActivity"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: MainViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViewModel()
        if(resources.getBoolean(R.bool.is_free)){
            initAdView()
        }
        //findNavController(R.id.landfill_nav_host_fragment).addOnDestinationChangedListener(this)
    }

    private fun initViewModel(){
        viewModel = ViewModelProvider(this, viewModelFactory).get(
            MainViewModel::class.java
        )
        viewModel.powerDlgData.observe(this, Observer {
            showPowerDialog(false)
        })
        viewModel.newDlgData.observe(this, Observer {
            val whatNewFragment = WhatNewFragment()
            whatNewFragment.show(supportFragmentManager, null)
        })

        viewModel.groupsData.observe(this, Observer {
            //groupsPageAdapter = GroupsPageAdapter(it, this)
        })
    }

    private fun initAdView(){
        MobileAds.initialize(this) {}
        val mAdView: AdView = findViewById(R.id.adView)
        mAdView.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    fun onDismissWhatNew(){
        Log.d(TAG, "onDismissWhatNew")
        viewModel.checkNeedShowPowerDlg()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        Log.d("MAIN", "onDestinationChanged: ${destination.id}")
        when(destination.id){

            R.id.mainFragment -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.setHomeButtonEnabled(false)
                //tabs.visibility = View.VISIBLE
            }
            else ->{
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.setHomeButtonEnabled(true)
                //tabs.visibility = View.GONE
            }
        }
    }

    private fun showPowerDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.background_work))
        builder.setCancelable(false)
        builder.setMessage(getString(R.string.background_work_text_body))
        builder.setPositiveButton(getString(R.string.allow),
            DialogInterface.OnClickListener { dialog, id ->
                getSharedPreferences("conf", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(EXTRA_POWER_DLG, true)
                    .apply()
                KillerManager.doActionPowerSaving(this);
                //dialog.cancel()
            })

        builder.setNeutralButton(getString(R.string.dont_ask_again),
            DialogInterface.OnClickListener { dialog, id ->
                getSharedPreferences("conf", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(EXTRA_POWER_DLG, true)
                    .apply()
                //dialog.cancel();
            })

        builder.setNegativeButton(getString(android.R.string.cancel), null)
        builder.create().show()
    }

    private fun showPowerDialog(checkFirst: Boolean){
        var isPower = getSharedPreferences("conf", Context.MODE_PRIVATE)
                .getBoolean(EXTRA_POWER_DLG, false)

        if(!isPower) showPowerDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return when (id) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_power -> {
                showPowerDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }*/

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent ${intent?.getLongExtra(ANIMAL_ID, -1)}")
    }

    override fun onResume() {
        super.onResume()
        val animalId = intent.getLongExtra(ANIMAL_ID, -1)
        Log.d(TAG, "onResume: $animalId")
    }
}
