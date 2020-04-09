package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service;

import com.example.idan.plusplus.Classes.KeshetAkamiTokenResponse;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.IBaseService;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IChannell12Service extends IBaseService {

    @GET
    Observable<Response<KeshetAkamiTokenResponse>> getChannel12Ticket(@Url String fullUrl);
}
