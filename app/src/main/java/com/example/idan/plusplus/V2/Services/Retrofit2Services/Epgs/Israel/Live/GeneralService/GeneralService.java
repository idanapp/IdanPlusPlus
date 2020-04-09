package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.GeneralService;



import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.WebapiSingleton;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseRetrofit2Service;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.IBaseService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.reactivex.Observable;
import me.toptas.rssconverter.RssFeed;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

public class GeneralService extends BaseRetrofit2Service implements IBaseService {

    IBaseService generalService;

    public GeneralService(String baseUrl) {
        super(Utils.getBaseUrlEmpty(),true,false,false,GENERAL);
        generalService = WebapiSingleton.getGeneralService(getServiceRetrofit());
    }


    @Override
    public Observable<Response<ResponseBody>> getHtml(String fullUrl) {
        return generalService.getHtml(fullUrl);
    }

    @Override
    public Observable<Response<ResponseBody>> postHtml(String fullUrl, RequestBody body) {
        return generalService.postHtml(fullUrl, body);
    }

    @Override
    public Observable<Response<JsonObject>> getJsonObject(String fullUrl) {
        return generalService.getJsonObject(fullUrl);
    }

    @Override
    public Observable<Response<JsonArray>> getJsonArray(String fullUrl) {
        return generalService.getJsonArray(fullUrl);
    }

    @Override
    public Observable<Response<JsonObject>> postChannelJsonHtml(String fullUrl, RequestBody body) {
        return generalService.postChannelJsonHtml(fullUrl,body);
    }

    @Override
    public Observable<Response<JsonArray>> postJsonArray(String ullUrl, RequestBody body) {
        return generalService.postJsonArray(ullUrl, body) ;
    }

    @Override
    public Observable<Response<JsonPrimitive>> postChannelJsonPremitiveHtml(String fullUrl, RequestBody body) {
        return generalService.postChannelJsonPremitiveHtml(fullUrl, body);
    }

    @Override
    public Observable<Response<RssFeed>> getRssfeed(String fullUrl) {
        return generalService.getRssfeed(fullUrl);
    }
}
