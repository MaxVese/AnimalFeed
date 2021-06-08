package ru.sem.animalfeed.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import ru.sem.animalfeed.alarm.BootNotifyReceiver
import ru.sem.animalfeed.alarm.DeferReceiver
import ru.sem.animalfeed.alarm.DeleteNotificationReceiver
import ru.sem.animalfeed.alarm.EatReceiver

@Module
abstract class ReceiverModule {

    @ContributesAndroidInjector
    abstract fun contributeEatReceiver(): EatReceiver?

    @ContributesAndroidInjector
    abstract fun contributeBootReceiver(): BootNotifyReceiver?

    @ContributesAndroidInjector
    abstract fun contributeDelReceiver(): DeleteNotificationReceiver?

    @ContributesAndroidInjector
    abstract fun contributeDeferReceiver(): DeferReceiver?
}