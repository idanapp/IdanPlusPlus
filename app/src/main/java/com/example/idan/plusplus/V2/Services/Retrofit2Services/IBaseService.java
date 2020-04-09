package com.example.idan.plusplus.V2.Services.Retrofit2Services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import io.reactivex.Observable;
import me.toptas.rssconverter.RssFeed;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface IBaseService {

    @GET
    Observable<Response<ResponseBody>> getHtml(@Url String fullUrl);

    @POST
    Observable<Response<ResponseBody>> postHtml(@Url String fullUrl, @Body RequestBody body);

    @GET
    Observable<Response<JsonObject>> getJsonObject(@Url String fullUrl);

    @GET
    Observable<Response<JsonArray>> getJsonArray(@Url String fullUrl);

    @POST
    Observable<Response<JsonObject>> postChannelJsonHtml(@Url String fullUrl, @Body RequestBody body);

    @POST
    Observable<Response<JsonArray>> postJsonArray(@Url String ullUrl,@Body RequestBody body);

    @POST
    Observable<Response<JsonPrimitive>> postChannelJsonPremitiveHtml(@Url String fullUrl, @Body RequestBody body);

    @GET
    Observable<Response<RssFeed>> getRssfeed(@Url String fullUrl);

}
