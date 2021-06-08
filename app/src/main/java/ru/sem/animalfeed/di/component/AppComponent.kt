package ru.sem.animalfeed.component

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.support.AndroidSupportInjectionModule
import ru.sem.animalfeed.App
import ru.sem.animalfeed.di.modules.*
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidInjectionModule::class, ViewModelModule::class, FragmentModule::class, ActivityModule::class,
    AppModule::class,  AndroidSupportInjectionModule::class, DBModule::class, ReceiverModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(cardApplication: App)

    //fun getRetrofit():Retrofit
}