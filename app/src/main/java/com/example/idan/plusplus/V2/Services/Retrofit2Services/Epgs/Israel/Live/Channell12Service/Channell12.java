package com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service;

import android.content.SharedPreferences;

import androidx.fragment.app.FragmentActivity;

import com.example.idan.plusplus.Classes.KeshetAkamiTokenResponse;
import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.app;
import com.example.idan.plusplus.model.GridItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class Channell12 extends BaseAbstractService {

    private GridItem localGridItem;

    public Channell12(SharedPreferences sharedPreferences,OnAsyncTaskLoadCompletes completeCallback) {
        super(app.getsRetrofitServices().getChannell12Service(),sharedPreferences,completeCallback);
    }

    //region Live Channell 12
    public void getLiveChannel12(FragmentActivity fragmentActivity,int rootFragment,GridItem gridItem, OnAsyncTaskLoadCompletes<GridItem> callback) {
        freshStartChannell();
        setCurrentGridItem(gridItem);
        setFragmentActivity(fragmentActivity);
        setFinalVideoLinkCallback(callback);
        startSpinner(fragmentActivity,rootFragment);
        getJsonObjectFromUrl("http://www.mako.co.il/mako-vod-live-tv/VOD-6540b8dcb64fd31006.htm?type=service&device=desktop&strto=true",false)
                .flatMap(this::getLiveChannellNext)
                .flatMap(this::getLiveChannel1Next1)
                .flatMap(this::getLiveChannelNext2)
                .flatMap(t2 -> getFinalFromM3u8(t2,getFinalUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSubscribeString);
    }
    //endregion

    //region Shared Live Methods
    private ObservableSource<String> getLiveChannelNext2(KeshetAkamiTokenResponse ticketRes) {
        String itemCase = ticketRes.caseId;
        if (itemCase.equals("1")) {
            String url = ticketRes.tickets[0].url;
            if (url.startsWith("//"))
                url = "http:" + url;
            String videoUrl = url;
            if (url.indexOf("?") > 0)
                videoUrl += "&" + ticketRes.tickets[0].ticket;
            else
                videoUrl += "?" + ticketRes.tickets[0].ticket;
            setFinalUrl(videoUrl);
        }
        return getHtmlFromUrl(getFinalUrl(),false);
    }

    private ObservableSource<KeshetAkamiTokenResponse> getLiveChannel1Next1(JsonObject t1) {
        if (t1 == null) return Observer::onComplete;
        JsonArray mediaArrayObj = t1.getAsJsonArray("media");
        String itemUrl = null;
        String itemCdn = null;
        for (JsonElement mediaElem : mediaArrayObj) {
            JsonObject mediaObj = mediaElem.getAsJsonObject();
            String itemFormat = mediaObj.get("format").getAsString();
            if (itemFormat.equals("AKAMAI_HLS")) {
                itemUrl = mediaObj.get("url").getAsString();
                itemCdn = mediaObj.get("cdn").getAsString();
                break;
            }
        }
        if (itemUrl == null) {
            for (JsonElement mediaElem : mediaArrayObj) {
                JsonObject mediaObj = mediaElem.getAsJsonObject();
                String itemFormat = mediaObj.get("format").getAsString();
                if (itemFormat.equals("CASTTIME_HLS")) {
                    itemUrl = mediaObj.get("url").getAsString();
                    itemCdn = mediaObj.get("cdn").getAsString();
                    break;
                }
            }
        };
        if (itemUrl == null) return Observer::onComplete;
        String tickectUrl = "http://mass.mako.co.il/ClicksStatistics/entitlementsServicesV2.jsp?et=gt&lp="+itemUrl+"&rv="+itemCdn;
        setFinalUrl(itemUrl);
        return ((IChannell12Service)getService()).getChannel12Ticket(tickectUrl)
                .subscribeOn(Schedulers.io())
                .map(Response::body);
    }

    private ObservableSource<JsonObject> getLiveChannellNext(JsonObject t) {
        String link = "http://www.mako.co.il/AjaxPage?jspName=playlist.jsp&vcmid={1}&videoChannelId={2}&galleryChannelId={1}&isGallery=false&consumer=web_html5&encryption=no";
        if (t != null) {
            JsonObject rootObj = t.get("root").getAsJsonObject();
            if (rootObj.has("video")) {
                JsonObject videoObj = rootObj.getAsJsonObject("video");
                String vcmId = videoObj.get("guid").getAsString();
                String channelId =videoObj.get("chId").getAsString();
                String galleryId = videoObj.get("galleryChId").getAsString();
                link = "http://www.mako.co.il/AjaxPage?jspName=playlist.jsp&vcmid="+vcmId+"&videoChannelId="+channelId+"&galleryChannelId="+galleryId+"&isGallery=false&consumer=web_html5&encryption=no";
            }
        }
        return getJsonObjectFromUrl(link,false);
    }
    //endregion


}
