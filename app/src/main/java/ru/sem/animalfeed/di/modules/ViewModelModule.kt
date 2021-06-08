package ru.sem.animalfeed.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import ru.sem.animalfeed.ui.animal.AnimalViewModel
import ru.sem.animalfeed.ui.history.HistoryViewModel
import ru.sem.animalfeed.ui.main.MainViewModel


@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    protected abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AnimalViewModel::class)
    protected abstract fun bindAnimalViewModel(animalViewModel: AnimalViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(HistoryViewModel::class)
    protected abstract fun bindHistoryViewModel(historyViewModel: HistoryViewModel): ViewModel

}