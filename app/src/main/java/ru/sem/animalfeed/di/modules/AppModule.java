package ru.sem.animalfeed.di.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class AppModule {

    @Provides
    @Singleton
    Context provideContext(Application application) {
        return application;
    }

    /*@Provides
    String provideUrl(Context appContext) {
        return appContext.getSharedPreferences("conf",Context.MODE_PRIVATE)
                .getString("url", App.DEFAULT_API_URL);

    }*/
}
