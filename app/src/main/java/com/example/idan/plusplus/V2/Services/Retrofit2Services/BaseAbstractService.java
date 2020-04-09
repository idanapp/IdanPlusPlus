package com.example.idan.plusplus.V2.Services.Retrofit2Services;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.AppCommon;
import com.example.idan.plusplus.V2.App.WebapiSingleton;
import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.app;
import com.example.idan.plusplus.model.GridItem;
import com.example.idan.plusplus.ui.SpinnerFragment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.LinkedHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

public abstract class BaseAbstractService {
    private SpinnerFragment spinnerFragment = new SpinnerFragment();
    private SharedPreferences sharedPref;
    private FragmentActivity fragmentActivity;
    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private OnAsyncTaskLoadCompletes mCallback;
    private OnAsyncTaskLoadCompletes mVideoLinkCallback;
    private OnAsyncTaskLoadCompletes mFinishedCallback;
    private IBaseService generalService;
    protected Observable<String> emptyReturn = Observable.empty();

    private String finalUrl;
    private GridItem currentGridItem;

    public BaseAbstractService(SharedPreferences sharedPreferences,OnAsyncTaskLoadCompletes finishedCallback) {
        sharedPref = sharedPreferences;
        mFinishedCallback = finishedCallback;
        disposed();
        clearHeaders();
        WebapiSingleton.clearCookieJar();
        generalService = app.getsRetrofitServices().getGeneralService();
        RxJavaPlugins.setErrorHandler(e -> {
            Log.i("IDAN_APP_ERROR",e.getMessage());
            e.printStackTrace();
        });
    }

    public BaseAbstractService(IBaseService service,SharedPreferences sharedPreferences,OnAsyncTaskLoadCompletes finishedCallback) {
        sharedPref = sharedPreferences;
        mFinishedCallback = finishedCallback;
        disposed();
        clearHeaders();
        WebapiSingleton.clearCookieJar();
        generalService = service;
        RxJavaPlugins.setErrorHandler(this::handleError);
    }

    public IBaseService  getService() {return generalService;}
    public void disposed() {
        mCompositeDisposable.clear();
    };

    protected void freshStartChannell() {
        disposed();
        clearHeaders();
        WebapiSingleton.clearCookieJar();
        setFinalUrl(Utils.getBaseUrlEmpty());
        setCurrentGridItem(null);
        setUserAgent(Utils.getUserAgent());
        setCallback(null);
        setFinalVideoLinkCallback(null);
    }

    protected void setUserAgent(String userAgent) {
        LinkedHashMap<String,String> headers = getHeaders();
        headers.put("User-Agent",userAgent);
        setHeaders(headers);
    }

    protected void setFragmentActivity(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    private void addCompositeDeisposable(Disposable d) {
        mCompositeDisposable.add(d);
   }

    protected void startSpinner(FragmentActivity fragmentActivity,int mainfragment) {
       if (fragmentActivity == null) return;
        try {
            if (spinnerFragment.isAdded() || mainfragment == -1) return;
            fragmentActivity.getSupportFragmentManager().beginTransaction().add(mainfragment, spinnerFragment).commit();
        } catch (Exception e) {

        }
    }

    private void stopSpinner(FragmentActivity fragmentActivity) {
        if (fragmentActivity == null) return;
        try {
            fragmentActivity.getSupportFragmentManager().beginTransaction().remove(spinnerFragment).commit();
        } catch (Exception e) {

        }
    }

    private void clearHeaders() {
        WebapiSingleton.clearHeaders();
    }

    protected void setHeaders(LinkedHashMap<String,String> headers) {
        WebapiSingleton.setHeaders(headers);
    }

    protected LinkedHashMap<String,String> getHeaders() {
        return WebapiSingleton.getHeaders();
    }

    protected void setFinalUrl(String url) {
        finalUrl = url;
    }

    protected String getFinalUrl() {
        return finalUrl;
    }

    protected  void setCurrentGridItem(GridItem gridItem) {
        currentGridItem = gridItem;
    }

    private GridItem getCurrentGridItem() {
        return currentGridItem;
    }

    protected void setCallback(OnAsyncTaskLoadCompletes callback) {
        mCallback = callback;
    }

    protected void setFinalVideoLinkCallback(OnAsyncTaskLoadCompletes callback) {
        mVideoLinkCallback = callback;
    }

    private void runFinalVideoLinkCallback(Object t) {
        if (mVideoLinkCallback == null) return;
        new Handler(Looper.getMainLooper()).post(() -> mVideoLinkCallback.onAsyncTaskLoadCompletes(t));
    }

    private void runFinishedCallback(Object t) {
        if (mFinishedCallback == null) return;
        new Handler(Looper.getMainLooper()).post(() -> mFinishedCallback.onAsyncTaskLoadCompletes(t));
    }


    protected Observable<String> getHtmlFromUrl(String url,boolean useMainThred) {
        if (!useMainThred)
            return generalService.getHtml(url)
                    .subscribeOn(Schedulers.io())
                    .map(mapResponseBodyToString);

        else
            return generalService.getHtml(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(mapResponseBodyToString);
    }

    protected Observable<JsonObject> getJsonObjectFromUrl(String url, boolean useMainThred) {
        if (!useMainThred)
            return generalService.getJsonObject(url)
                    .subscribeOn(Schedulers.io())
                    .map(mapResponseJsonObject);
        else
            return generalService.getJsonObject(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(mapResponseJsonObject);
    }

    protected Observable<JsonArray> getJsonArrayFromUrl(String url, boolean useMainThred) {
        if (!useMainThred)
            return generalService.getJsonArray(url)
                    .subscribeOn(Schedulers.io())
                    .map(mapResponseJsonArray);
        else
            return generalService.getJsonArray(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(mapResponseJsonArray);
    }

    protected ObservableSource<String> getFinalFromM3u8(String t2,String defaultUrl) throws IOException {
        if (t2.isEmpty()) return Observable.just(defaultUrl);
        String videoQualityHtml = t2;
        String videoUrl = getFinalUrl();
        if (defaultUrl != null && !defaultUrl.isEmpty()) videoUrl = defaultUrl;
        videoUrl = Utils.getStreamQualityLink(videoQualityHtml,videoUrl);
        return Observable.just(videoUrl);
    }

    protected  Observer<String> onSubscribeString = new Observer<String>() {
        @Override
        public void onSubscribe(Disposable d) {
            addCompositeDeisposable(d);
        }

        @Override
        public void onNext(String s) {
            getCurrentGridItem().videoUrl = s;
            runFinalVideoLinkCallback(getCurrentGridItem());
            stopSpinner(fragmentActivity);
        }

        @Override
        public void onError(Throwable e) {
            handleError(e);
        }

        @Override
        public void onComplete() {
            stopSpinner(fragmentActivity);
            runFinishedCallback(null);
        }
    };

    protected void handleError(Throwable e) {
        try {
            if (e instanceof UndeliverableException) {

            } else {
                stopSpinner(fragmentActivity);
                String msg = "Source not working try again or check your internet connection";
                msg = e.getMessage();
                int duration = 10;
                AppCommon.showCenterToast(msg,duration);
                e.printStackTrace();
                Log.i("RXJAVA_DEBUG","ERROR = " + e.getMessage());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Function<Response<ResponseBody>, String> mapResponseBodyToString = responseBodyResponse -> {
        if (responseBodyResponse.code() != 200) return "";
        if (responseBodyResponse.body() == null) return "";
        return responseBodyResponse.body().string();
    };

    private Function<Response<JsonObject>, JsonObject> mapResponseJsonObject = responseBodyResponse -> {
        JsonObject retObj = new JsonObject();
        if (responseBodyResponse.code() != 200) return retObj;
        if (responseBodyResponse.body() == null) return retObj;
        if (responseBodyResponse.body().isJsonNull()) return retObj;
        return responseBodyResponse.body();
    };

    private Function<Response<JsonArray>, JsonArray> mapResponseJsonArray = responseBodyResponse -> {
        JsonArray retObj = new JsonArray();
        if (responseBodyResponse.code() != 200) return retObj;
        if (responseBodyResponse.body() == null) return retObj;
        if (responseBodyResponse.body().isJsonNull()) return retObj;
        return responseBodyResponse.body();
    };

}

