package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channel11Service;

import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.model.GridItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Channell11 extends BaseAbstractService {

    private int pageNumber = 0;
    private GridItem localGridItem;
    private String baseUrl = "https://www.kan.org.il/";

    public Channell11(SharedPreferences sharedPreferences,OnAsyncTaskLoadCompletes completesCallback) {
        super(sharedPreferences,completesCallback);
    }

    //region Live Channell 11
    public void getLiveChannell11(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl(baseUrl + "live/tv.aspx?stationid=2",false)
                .flatMap(this::getLiveChannellNext)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }

    //endregion

    //region Live Channell 23
    public void getLiveChannell23(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl(baseUrl + "live/tv.aspx?stationid=20",false)
                .flatMap(this::getLiveChannellNext)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }

    //endregion

    //region Live Channell 33
    public void getLiveChannell33(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl("https://www.makan.org.il/live/tv.aspx?stationid=3",false)
                .flatMap(this::getLiveChannellNext)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }
    //endregion

    //region get Live link
    public void getKanLink(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl(gridItem.linkUrl,false)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }
    //endregion

    //region Shared Live Channell Methods
    private ObservableSource<String> getLiveChannel1Next1(String t1) {
        if (t1.isEmpty()) return emptyReturn;
        String firstReg = "bynetURL:\\s*\"(.*?)\"";
        Matcher linkMatcher;
        linkMatcher = Pattern.compile(firstReg,Pattern.DOTALL).matcher(t1);
        if (!linkMatcher.find())
            linkMatcher = Pattern.compile("\"UrlRedirector\":\"(.*?)\"",Pattern.DOTALL).matcher(t1);
        linkMatcher.reset();
        if (linkMatcher.find()) {
            setFinalUrl(linkMatcher.group(1));

        }
        return getHtmlFromUrl(getFinalUrl(),false);
    }

    private ObservableSource<String> getLiveChannellNext(String t) {
        if (t.isEmpty()) return null;
        String regex = "<iframe.*class=\"embedly-embed\".*src=\"(.+?)\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(t);
        String link = Utils.getBaseUrlEmpty();
        if (matcher.find()) {
            link = matcher.group(1);
        }
        return getHtmlFromUrl(link,false);
    }
    //endregion
}
