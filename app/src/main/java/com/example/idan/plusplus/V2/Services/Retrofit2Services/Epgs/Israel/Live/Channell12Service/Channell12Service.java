package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service;

import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.WebapiSingleton;
import com.example.idan.plusplus.Classes.KeshetAkamiTokenResponse;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseRetrofit2Service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.reactivex.Observable;
import me.toptas.rssconverter.RssFeed;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class Channell12Service extends BaseRetrofit2Service implements IChannell12Service {

    IChannell12Service channell12Service;

    public Channell12Service(String baseUrl) {
        super(Utils.getBaseUrlEmpty(),true,true,false,GENERAL);
        channell12Service= WebapiSingleton.getChannel12Service(getServiceRetrofit());
    }


    @Override
    public Observable<Response<ResponseBody>> getHtml(String fullUrl) {
        return channell12Service.getHtml(fullUrl);
    }

    @Override
    public Observable<Response<ResponseBody>> postHtml(String fullUrl, RequestBody body) {
        return channell12Service.postHtml(fullUrl,body);
    }

    @Override
    public Observable<Response<JsonObject>> getJsonObject(String fullUrl) {
        return channell12Service.getJsonObject(fullUrl);
    }

    @Override
    public Observable<Response<JsonArray>> getJsonArray(String fullUrl) {
        return channell12Service.getJsonArray(fullUrl);
    }

    @Override
    public Observable<Response<JsonObject>> postChannelJsonHtml(String fullUrl, RequestBody body) {
        return channell12Service.postChannelJsonHtml(fullUrl,body);
    }

    @Override
    public Observable<Response<JsonArray>> postJsonArray(String ullUrl, RequestBody body) {
        return channell12Service.postJsonArray(ullUrl,body);
    }

    @Override
    public Observable<Response<JsonPrimitive>> postChannelJsonPremitiveHtml(String fullUrl, RequestBody body) {
        return channell12Service.postChannelJsonPremitiveHtml(fullUrl, body);
    }

    @Override
    public Observable<Response<RssFeed>> getRssfeed(String fullUrl) {
        return channell12Service.getRssfeed(fullUrl);
    }


    @Override
    public Observable<Response<KeshetAkamiTokenResponse>> getChannel12Ticket(String fullUrl) {
        return channell12Service.getChannel12Ticket(fullUrl);
    }
}
