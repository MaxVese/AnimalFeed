package ru.sem.animalfeed.ui.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.AndroidInjection
import ru.sem.animalfeed.ANIMAL_ID
import ru.sem.animalfeed.R
import ru.sem.animalfeed.di.modules.ViewModelFactory
import javax.inject.Inject

class HistoryActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    lateinit var viewModel: HistoryViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        initViewModel()
        NotificationManagerCompat.from(this).cancel(intent.getLongExtra(ANIMAL_ID, 0).toInt())
    }

    private fun initViewModel(){
        /*supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)*/
        viewModel = ViewModelProvider(this, viewModelFactory).get(
            HistoryViewModel::class.java
        )
        viewModel.loadEvent.observe(this, Observer {
            if(it==true){
                viewModel.loadByAnimalId(intent.getLongExtra(ANIMAL_ID, -1))
            }
        })
    }

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return when (id) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/
}
