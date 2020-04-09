package com.example.idan.plusplus.V2.App;

import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service.Channell12Service;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service.IChannell12Service;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.GeneralService.GeneralService;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.IBaseService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    public static final String PROVIDER_GENERAL_SERVICE = "PROVIDER_GENERAL_SERVICE";
    public static final String PROVIDER_CHANNELL_12_SERVICE = "PROVIDER_CHANNELL_12_SERVICE";




    @Provides
    @Singleton
    @Named(PROVIDER_GENERAL_SERVICE)
    static IBaseService getGeneralService() {
        return new GeneralService(Utils.getBaseUrlEmpty());
    }

    @Provides
    @Singleton
    @Named(PROVIDER_CHANNELL_12_SERVICE)
    static IChannell12Service getChannell12Service() {
        return new Channell12Service(Utils.getBaseUrlEmpty());
    }



}
