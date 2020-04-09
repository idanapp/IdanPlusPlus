package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.UpdateSoftwareService;

import android.content.SharedPreferences;

import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;

import io.reactivex.Observable;

public class UpdateSoftware extends BaseAbstractService {


    public UpdateSoftware(SharedPreferences sharedPreferences, OnAsyncTaskLoadCompletes completesCallback) {
        super(sharedPreferences,completesCallback);
    }

    //region Update software function
    public Observable<String> UpdateSoftware(String url) {
        return getHtmlFromUrl(url,false);
    }
    //endregion




}
