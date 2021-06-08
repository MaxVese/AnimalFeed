package ru.sem.animalfeed.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.sem.animalfeed.ui.history.HistoryActivity
import ru.sem.animalfeed.ui.main.MainActivity


@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    abstract fun contributeMainActivity(): MainActivity?

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    abstract fun contributeHistoryActivity(): HistoryActivity?

}