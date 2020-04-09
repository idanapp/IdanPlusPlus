package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell13Service;

import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.model.GridItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class Channell13 extends BaseAbstractService {

    private GridItem localGridItem;
    private String baseUrl = "https://13tv.co.il";
    private String brightcoveApi = "https://edge.api.brightcove.com/playback/v1/accounts/1551111274001/videos/";
    private String brightcovePK = "application/json;pk=BCpkADawqM30eqkItS5d08jYUtMkbTKu99oEBllbfUaFKeknXu4iYsh75cRm2huJ_b1-pXEPuvItI-733TqJN1zENi-DcHlt7b4Dv6gCT8T3BS-ampD0XMnORQLoLvjvxD14AseHxvk0esW3";
    private String castTimeApi = "https://13tv-api.oplayer.io/api/getlink/?userId=45E4A9FB-FCE8-88BF-93CC-3650C39DDF28&serverType=web&cdnName=casttime&ch=";

    public Channell13(SharedPreferences sharedPreferences,OnAsyncTaskLoadCompletes completeCallback) {
        super(sharedPreferences,completeCallback);
    }

    //region Live Channell 13
    public void getLiveChannell13(FragmentActivity fragmentActivity, int rootFragment, GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getHtmlFromUrl(baseUrl + "/live/",false)
                .flatMap(this::getLiveChannellNext)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(t2 -> getFinalFromM3u8(t2, getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }
    //endregion


    //region Shared Methods
    private ObservableSource<String> getCastTimeLink(JsonArray t,String defultUrl) {
        try {
            JsonObject firstLink = t.get(0).getAsJsonObject();
            String link = firstLink.get("Link").getAsString();
            setFinalUrl(link);
        } catch (Exception e) {
            setFinalUrl(defultUrl);
        }
        return getHtmlFromUrl(getFinalUrl(),false);

    }

    private ObservableSource<String> getLiveChannel1Next1(Object t1) {
        if (t1 instanceof JsonObject) {
            try {
                String liveUrl = ((JsonObject)t1).get("sources").getAsJsonArray().get(0).getAsJsonObject().get("src").getAsString();
                setFinalUrl(liveUrl);
            } catch (Exception e) {
                setFinalUrl("http://reshet-live-il.ctedgecdn.net/13tv-desktop/r13.m3u8");
            }
        } else if (t1 instanceof JsonArray) {
            return getCastTimeLink((JsonArray)t1,"http://reshet-live-il.ctedgecdn.net/13tv-desktop/r13.m3u8");
        }

        return getHtmlFromUrl(getFinalUrl(),false);
    }

    private ObservableSource<?> getLiveChannellNext(String t) {
        //if (t.isEmpty()) return emptyReturn;
        String regex = "data_query = (.*?)\\.data_query;";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(t);
        String link = "https://besttv1.aoslive.it.best-tv.com/reshetDVR01/testdvr/index.m3u8";
        if (matcher.find()) {
            String dataQuery = matcher.group(1);
            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(dataQuery);
            String provider = json.get("header").getAsJsonObject().get("Live").getAsJsonObject().get("extras").getAsJsonObject().get("live_video_provider").getAsString();// result['Header']['Live']['extras']['live_video_provider']
            String linkLive = "https://besttv1.aoslive.it.best-tv.com/reshetDVR01/testdvr/index.m3u8";
            if (provider.equals("brightcove")) {
                String videoId = json.get("data_query").getAsJsonObject().get("header").getAsJsonObject().get("Live").getAsJsonObject().get("videoId").getAsString();
                linkLive = brightcoveApi + videoId;
                LinkedHashMap<String, String> headers = getHeaders();
                headers.put("Accept", brightcovePK);
                setHeaders(headers);
                link = linkLive;
            } else if (provider.equals("cast_time")) {
                linkLive = castTimeApi + "1";
                return getJsonArrayFromUrl(linkLive,false);
            }
        }
        return getJsonObjectFromUrl(link,false);
    }

    //endregion

}
