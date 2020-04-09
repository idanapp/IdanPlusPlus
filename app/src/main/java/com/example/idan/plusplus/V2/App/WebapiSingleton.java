package com.example.idan.plusplus.V2.App;

import android.util.Log;

import com.example.idan.plusplus.BuildConfig;
import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service.IChannell12Service;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.IBaseService;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.toptas.rssconverter.RssConverterFactory;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;


public class WebapiSingleton {

    public static CookieJar cookieJar;
    public static HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();
    public static boolean isInPicInPic;
    private static IBaseService generalService;
    private static IChannell12Service chanell12Service;
    public static boolean isTv;


    private static void initCookieJar(Boolean clearCookies) {
        if (clearCookies)
            clearCookieJar();
        cookieJar = new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                HttpUrl url1 = new HttpUrl.Builder()
                        .scheme("http")
                        .host("127.0.0.1")
                        .build();
//                if (cookies != null) {
//                    for (Cookie cookie : cookies) {
//                        Log.i("IDAN_APP_COOKIE_S", cookie.toString());
//                    }
//                }
                cookieStore.put(url1, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                HttpUrl url1 = new HttpUrl.Builder()
                        .scheme("http")
                        .host("127.0.0.1")
                        .build();
                List<Cookie> cookies = cookieStore.get(url1);
//                if (cookies != null) {
//                    for (Cookie cookie : cookies) {
//                        Log.i("IDAN_APP_COOKIE_L", cookie.toString());
//                    }
//                }
                return cookies != null ? cookies : new ArrayList<>();
            }
        };
    }

    public static void clearCookieJar()  {
        cookieStore.clear();
    }

    private static LinkedHashMap<String, String> headers = new LinkedHashMap<>();
    private static String userAgent;
    private static OkHttpClient okHttpClientV2;
    private static void clearUserAgent() { userAgent = null;}



    private static Dns getDns(boolean useDnsOverHttps) {
        Dns dns = Dns.SYSTEM;
        return dns;
    }
    private static void clearAllRequests(Boolean clearAllRequests) {
        if (okHttpClientV2 != null && clearAllRequests)
            okHttpClientV2.dispatcher().cancelAll();
    }
    private static OkHttpClient.Builder setHttpClientBuilder(Dns dns, ClearableCookieJar cookieJar1) {
        OkHttpClient.Builder httpClientBuilder;
        if (dns != null)
            httpClientBuilder = new OkHttpClient.Builder().dns(dns).cookieJar(cookieJar);
        else
            httpClientBuilder = new OkHttpClient.Builder().cookieJar(cookieJar);
        return httpClientBuilder;
    }
    private static Interceptor baseInterceptor = chain -> {
        Request original = chain.request();
        Request.Builder requestBuilder = original.newBuilder();
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> item:headers.entrySet()) {
                requestBuilder.header(item.getKey(),item.getValue());
            }
        }
        Request request = requestBuilder.method(original.method(),original.body()).build();
        if (BuildConfig.DEBUG)
            Log.i("IDAN_APP_REQ",request.url().toString());
        Response response = chain.proceed(request);
        if (BuildConfig.DEBUG)
            Log.i("IDAN_APP_RES",String.valueOf(response.code()));
        return response;
    };

    public static Retrofit initRetrofitWebApi(String baseurl, Boolean clearAllRequests, Boolean useDnsOverHttps, Boolean clearCookies, Interceptor interceptor) {
        if (baseurl == null) throw new NullPointerException("base url cannot be null");
        clearAllRequests(clearAllRequests);
        initCookieJar(clearCookies);
        Dns dns = getDns(useDnsOverHttps);
        OkHttpClient.Builder httpClientBuilder = setHttpClientBuilder(dns,null);
        httpClientBuilder.addInterceptor(baseInterceptor);
        Gson gson = new GsonBuilder().setLenient().create();
        okHttpClientV2 = httpClientBuilder.build();
        return new Retrofit.Builder()
                .baseUrl(baseurl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientV2)
                .build();


    }

    public static Retrofit initRetrofitRss(String baseurl, Boolean clearAllRequests, Boolean useDnsOverHttps, Boolean clearCookies, Interceptor interceptor) {
        if (baseurl == null) throw new NullPointerException("base url cannot be null");
        clearAllRequests(clearAllRequests);
        initCookieJar(clearCookies);
        Dns dns = getDns(useDnsOverHttps);
        OkHttpClient.Builder httpClientBuilder = setHttpClientBuilder(dns,null);
        httpClientBuilder.addInterceptor(baseInterceptor);
        Gson gson = new GsonBuilder().setLenient().create();
        okHttpClientV2 = httpClientBuilder.build();
        return new Retrofit.Builder()
                .baseUrl(baseurl)
                .addConverterFactory(RssConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientV2)
                .build();
    }

    public static Retrofit initRetrofitXml(String baseurl, Boolean clearAllRequests, Boolean useDnsOverHttps, Boolean clearCookies, Interceptor interceptor) {
        if (baseurl == null) throw new NullPointerException("base url cannot be null");
        clearAllRequests(clearAllRequests);
        initCookieJar(clearCookies);
        Dns dns = getDns(useDnsOverHttps);
        OkHttpClient.Builder httpClientBuilder = setHttpClientBuilder(dns,null);
        httpClientBuilder.addInterceptor(baseInterceptor);
        Gson gson = new GsonBuilder().setLenient().create();
        okHttpClientV2 = httpClientBuilder.build();
        return new Retrofit.Builder()
                .baseUrl(baseurl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientV2)
                .build();
    }



    private WebapiSingleton(){

    }

    public static LinkedHashMap<String, String> getHeaders() {
        return headers;
    }

    public static void setHeaders(LinkedHashMap<String, String> headers) {
        WebapiSingleton.headers = headers;
    }

    public static String getUserAgent() {
        String userAgent =headers.get("User-Agent");
        return userAgent == null ? Utils.getUserAgent() : userAgent;
       // return userAgent;
    }

    public static void setUserAgent(String userAgent) {
        WebapiSingleton.userAgent = userAgent;
    }

    public static OkHttpClient getOkHttpClientV2() {
        return okHttpClientV2;
    }

    public static void clearHeaders() {
        if (headers != null)
            headers.clear();
    }


    public static IChannell12Service getChannel12Service(Retrofit serviceRetrofit) {
        if(chanell12Service == null){
            synchronized (WebapiSingleton.class) {
                if(chanell12Service == null){
                    chanell12Service = serviceRetrofit.create(IChannell12Service.class);
                    return chanell12Service;
                }
            }
        }
        return chanell12Service;
    }

    public static IBaseService getGeneralService(Retrofit serviceRetrofit) {
        if(generalService == null){
            synchronized (WebapiSingleton.class) {
                if(generalService == null){
                    generalService = serviceRetrofit.create(IBaseService.class);
                    return generalService;
                }
            }
        }
        return generalService;
    }

    public static void addCookieToStore(HttpUrl url,Cookie cookie) {
        List<Cookie> cookieList = cookieStore.get(url);
        List<Cookie> newList = new LinkedList<>();
        if (cookieList == null) cookieList = new LinkedList<>();
        for (Cookie c:cookieList) {
            newList.add(c);
        }
        newList.add(cookie);
        cookieStore.put(url,newList);
    }
}
