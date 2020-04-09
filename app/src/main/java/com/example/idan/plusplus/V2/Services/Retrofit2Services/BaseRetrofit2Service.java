package com.example.idan.plusplus.V2.Services.Retrofit2Services;

import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.AppCommon;
import com.example.idan.plusplus.V2.App.WebapiSingleton;

import java.util.LinkedHashMap;

import retrofit2.Retrofit;

public class BaseRetrofit2Service {

    protected static final int GENERAL = 100;
    protected static final int RSS = 101;
    protected static final int XML = 102;

    private Retrofit serviceRetrofit;

    public BaseRetrofit2Service(String baseUrl, Boolean clearCookieJar, Boolean clearAllRequests, Boolean useHttpsOverDns,int type) {
        if (AppCommon.CheckNullOrEmpty(baseUrl))
            throw new NullPointerException("Base url must not be null or empty");
        WebapiSingleton.setUserAgent(Utils.getUserAgent());
        LinkedHashMap<String,String> headers = WebapiSingleton.getHeaders();
        headers.put("User-Agent",WebapiSingleton.getUserAgent());
        WebapiSingleton.setHeaders(headers);
        switch (type) {
            case GENERAL:
                this.serviceRetrofit = WebapiSingleton.initRetrofitWebApi(baseUrl, clearAllRequests, useHttpsOverDns,clearCookieJar,null);
                break;
            case RSS:
                this.serviceRetrofit = WebapiSingleton.initRetrofitRss(baseUrl, clearAllRequests, useHttpsOverDns,clearCookieJar,null);
                break;
            case XML:
                this.serviceRetrofit = WebapiSingleton.initRetrofitXml(baseUrl, clearAllRequests, useHttpsOverDns,clearCookieJar,null);
                break;
            default:
                this.serviceRetrofit = WebapiSingleton.initRetrofitWebApi(baseUrl, clearAllRequests, useHttpsOverDns,clearCookieJar,null);
                break;
        }

    }

    protected Retrofit getServiceRetrofit() {
        return serviceRetrofit;
    }

}

