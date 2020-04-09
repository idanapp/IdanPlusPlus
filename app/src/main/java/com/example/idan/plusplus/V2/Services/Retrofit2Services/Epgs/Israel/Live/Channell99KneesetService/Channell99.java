package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell99KneesetService;

import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.model.GridItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Channell99 extends BaseAbstractService {

    public Channell99(SharedPreferences sharedPreferences) {
        super(sharedPreferences,null);
    }

    //region Live Channell 99 כנסת
    public void getLiveChannell99(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        setFinalUrl("https://w1.013.gostreaming.tv/Knesset/myStream/playlist.m3u8");
        getHtmlFromUrl("https://main.knesset.gov.il/_layouts/15/1037/CustomScripts/KnessetBroadcastLobby.js",false)
        .flatMap(this::getLiveChannellNext)
        .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }

    private ObservableSource<String> getLiveChannellNext(String t) {
        String regex = "pathChannelVideo = ko\\.observable\\(\"(.*?)\"\\);";
        Matcher linkMatcher;
        linkMatcher = Pattern.compile(regex,Pattern.DOTALL).matcher(t);
        if (linkMatcher.find()) {
            setFinalUrl(linkMatcher.group(1));
        }
        return getHtmlFromUrl(getFinalUrl(),false);
    }

    //endregion
}
