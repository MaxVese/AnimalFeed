package ru.sem.animalfeed.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.sem.animalfeed.ui.Brood.BroodFragment
import ru.sem.animalfeed.ui.Brood.BroodListFragment
import ru.sem.animalfeed.ui.animal.AnimalFragment
import ru.sem.animalfeed.ui.detail.DetailFragment
import ru.sem.animalfeed.ui.groups.GroupsFragment
import ru.sem.animalfeed.ui.history.EditHistoryFragment
import ru.sem.animalfeed.ui.history.FilterFragment
import ru.sem.animalfeed.ui.history.HistoryFragment
import ru.sem.animalfeed.ui.main.MainFragment
import ru.sem.animalfeed.ui.main.MainPageFragment


@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment


    @ContributesAndroidInjector
    abstract fun contributeAnimalFragment(): AnimalFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeEditHistoryFragment(): EditHistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeFilterFragment(): FilterFragment

    @ContributesAndroidInjector
    abstract fun contributeDetailFragment(): DetailFragment

    @ContributesAndroidInjector
    abstract fun contributeGroupsFragment(): GroupsFragment

    @ContributesAndroidInjector
    abstract fun contributeMainPageFragment(): MainPageFragment

    @ContributesAndroidInjector
    abstract fun contributeBroodListFragment(): BroodListFragment

    @ContributesAndroidInjector
    abstract fun contributeBroodFragment(): BroodFragment
}