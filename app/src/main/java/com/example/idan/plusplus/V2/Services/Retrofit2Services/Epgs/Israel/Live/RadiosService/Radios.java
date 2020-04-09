package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.RadiosService;

import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.model.GridItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;

public class Radios extends BaseAbstractService {

    private GridItem localGridItem;

    public Radios(SharedPreferences sharedPreferences, OnAsyncTaskLoadCompletes completesCallback) {
        super(sharedPreferences,completesCallback);
    }

    //region Radio 88FM
    public void getRadio88Fm(FragmentActivity fragmentActivity, int rootFragment, GridItem gridItem,OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        String url = "https://www.kan.org.il/radio/player.aspx?stationid=4";
        getHtmlFromUrl(url,false)
                .flatMap(this::getRadioLink)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .subscribe(onSubscribeString);

    }

    private ObservableSource<String> getRadioLink(String s) {
        String link = "";
        Matcher match = Pattern.compile("<div class=\"player_content\">.*?iframe src=\"(.*?)\"",Pattern.DOTALL).matcher(s);
        if (!match.find())
            match = Pattern.compile("iframeLink\\s*?=\\s*?\"(.*?)\"",Pattern.DOTALL).matcher(s);
        match.reset();
        if (match.find())
           link = match.group(1);
        return getHtmlFromUrl(link,false);
    }

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

    public void getRadio(FragmentActivity fragmentActivity, int rootFragment,GridItem gridItem, String regStr, int flags,OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl(gridItem.linkUrl,false)
                .flatMap((t1)->getRadioNext(t1,flags,regStr))
                .subscribe(onSubscribeString);

    }

    private ObservableSource<String> getRadioNext(String s,int flags,String regexStr) {
        String videoUrl = "";
        Matcher matcher = null;
        if (flags == -1)
            matcher = Pattern.compile(regexStr).matcher(s);
        else
            matcher = Pattern.compile(regexStr,flags).matcher(s);

        if (matcher.find()) {
            Matcher extractedUrls = Patterns.WEB_URL.matcher(matcher.group(0));
            while (extractedUrls.find()) {
                if (extractedUrls.group(0).contains("http://") || extractedUrls.group(0).contains("https://")) {
                    videoUrl = extractedUrls.group(0);
                    break;
                }
            }
        }
        setFinalUrl(videoUrl);
        return Observable.just(videoUrl);
    }
    //endregion



}
