/*
 * Copyright (c) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.idan.plusplus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.ListRow;

import com.example.idan.plusplus.Classes.Constants;
import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.BaseAbstractService;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channel11Service.Channell11;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell12Service.Channell12;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell13Service.Channell13;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.Channell99KneesetService.Channell99;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.RadiosService.Radios;
import com.example.idan.plusplus.V2.Services.Retrofit2Services.Epgs.Israel.Live.UpdateSoftwareService.UpdateSoftware;
import com.example.idan.plusplus.model.GridItem;
import com.example.idan.plusplus.ui.PlaybackActivity;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * A collection of utility methods, all static.
 */
public class Utils {

    private static final int CHANNEL_11 = 11;
    private static final int CHANNEL_23 = 23;
    private static final int CHANNEL_97 = 97;
    private static final int CHANNEL_99 = 99;
    private static final int CHANNEL_RADIO_KAN = 8000;
    private static final int CHANNEL_RADIO_99 = 8001;
    private static final int CHANNELS_QUALITY = -1;
    private static final int CHANNEL_33 = 33;

    private static Context mApplicationContext;

    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException ex) {}
        return -1;
    }

    //region V2 Channel Services
    private static LinkedList<BaseAbstractService> services = new LinkedList<>();

    public static UpdateSoftware getUpdateSoftwareService(Object o) {
        UpdateSoftware service = new UpdateSoftware(null,null);
        services.add(service);
        return service;
    }

    public static Radios getRadioService(OnAsyncTaskLoadCompletes<List<ListRow>> onAsyncTaskLoadCompletes) {
        Radios service = new Radios(null,onAsyncTaskLoadCompletes);
        services.add(service);
        return service;
    }

    public static Channell11 getChannell11(OnAsyncTaskLoadCompletes completeCallback) {
        Channell11 channell11 = new Channell11(null,completeCallback);
        services.add(channell11);
        return channell11;
    }

    public static Channell12 getChannell12(OnAsyncTaskLoadCompletes completeCallback) {
        Channell12 channell12 = new Channell12(null,completeCallback);
        services.add(channell12);
        return channell12;
    }

    public static Channell13 getChannell13(OnAsyncTaskLoadCompletes completeCallback) {
        Channell13 channell13 = new Channell13(null,completeCallback);
        services.add(channell13);
        return channell13;
    }


    public static Channell99 getChannell99() {
        Channell99 channell = new Channell99(null);
        services.add(channell);
        return channell;
    }


    public static void disposedServices() {
        if (services.isEmpty()) return;
        for (BaseAbstractService service :
                services) {
            service.disposed();
        }
        services.clear();
    }

    //endregion

    static {
        System.loadLibrary("keys");
    }

    public native static String getFirstKeyPart();
    public native static String getSecondKeyPart();

    public static Context getAppContext() {
        return  mApplicationContext;
    }

    public static void setAppContext(Context app) {
        mApplicationContext = app;
    }

    //DONE V2
    public static void proccessIsraelLiveChannel(FragmentActivity activity, int main_fragment, GridItem gridItem) {
        switch (gridItem.channelTag) {
            case Constants.Channels.Israel.Live.CHANNEL_KAN_11:
                getChannell11(null).getLiveChannell11(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_KESHET_12:
                getChannell12(null).getLiveChannel12(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_RESHET_13:
                getChannell13(null).getLiveChannell13(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_23:
                getChannell11(null).getLiveChannell23(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_33:
                getChannell11(null).getLiveChannell33(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_99:
                getChannell99().getLiveChannell99(activity,main_fragment,gridItem, gridItem1 -> openPlayBackAcitivty(activity, gridItem1,true));
                break;
            default:
                break;
        }
    }


    public static void proccessIsraelRadioChannel(FragmentActivity activity, int main_fragment, GridItem gridItem) {
        switch (gridItem.channelTag) {
            case Constants.Channels.Israel.Radio.FM88:
                Utils.getRadioService(null).getRadio88Fm(activity, main_fragment, gridItem, new OnAsyncTaskLoadCompletes<GridItem>() {
                    @Override
                    public void onAsyncTaskLoadCompletes(GridItem gridItem) {
                        //openRadioPlayBackActivity(activity,gridItem,true);
                        openPlayBackAcitivty(activity,gridItem,true);
                    }
                });
                break;
            case Constants.Channels.Israel.Radio.FM90:
            case Constants.Channels.Israel.Radio.FM97:
            case Constants.Channels.Israel.Radio.FM100:
            case Constants.Channels.Israel.Radio.FM101:
            case Constants.Channels.Israel.Radio.FM102:
            case Constants.Channels.Israel.Radio.FM102EILAT:
            case Constants.Channels.Israel.Radio.FM103:
            case Constants.Channels.Israel.Radio.FM1045:
            case Constants.Channels.Israel.Radio.FM1075:
                String regStr = Utils.setRegexForRadioStation(gridItem.channelTag);
                int flags = Utils.setFlagsForRadioStation(gridItem.channelTag);
                Utils.getRadioService(null).getRadio(activity, main_fragment, gridItem, regStr, flags, new OnAsyncTaskLoadCompletes<GridItem>() {
                    @Override
                    public void onAsyncTaskLoadCompletes(GridItem gridItem) {
                        openPlayBackAcitivty(activity,gridItem,true);
                    }
                });
                break;
            case Constants.Channels.Israel.Radio.FM99:
                gridItem.videoUrl = "http://eco-live.mediacast.co.il/99fm_aac";
                openPlayBackAcitivty(activity,gridItem,true);
                break;
            default:
                openPlayBackAcitivty(activity,gridItem,true);
                break;
        }
    }


    public static void gridItemClicked(FragmentActivity activity, int main_frame, GridItem gridItem) {
        switch (gridItem.type) {
            case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
                Utils.proccessIsraelLiveChannel(activity,main_frame,gridItem);
                break;
            case Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL:
                Utils.proccessIsraelRadioChannel(activity,main_frame,gridItem);
                break;
            default:
                break;
        }
    }


    public enum AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL;
    }

    private static final String LAST_APP_VERSION = "last_app_version";

    public static AppStart checkAppStart(Context ctx) {
        PackageInfo pInfo;
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(ctx);
        AppStart appStart = AppStart.NORMAL;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
            int lastVersionCode = sharedPreferences
                    .getInt(LAST_APP_VERSION, -1);
            int currentVersionCode = pInfo.versionCode;
            appStart = checkAppStart(currentVersionCode, lastVersionCode);
            // Update version in preferences
            sharedPreferences.edit()
                    .putInt(LAST_APP_VERSION, currentVersionCode).apply();
        } catch (PackageManager.NameNotFoundException ignored) {

        }
        return appStart;
    }

    private static AppStart checkAppStart(int currentVersionCode, int lastVersionCode) {
        if (lastVersionCode == -1) {
            return AppStart.FIRST_TIME;
        } else if (lastVersionCode < currentVersionCode) {
            return AppStart.FIRST_TIME_VERSION;
        } else if (lastVersionCode > currentVersionCode) {
            return AppStart.NORMAL;
        } else {
            return AppStart.NORMAL;
        }
    }

    private static AesCbcWithIntegrity.SecretKeys getStoredKeys() {
        try {
            String firstKeyD =  getBase64String(rot13(getFirstKeyPart()),Base64.DEFAULT);
            String secondKeyD =  getBase64String(rot13(getSecondKeyPart()),Base64.DEFAULT);
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(firstKeyD+secondKeyD);
            return keys;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static boolean isAtLeastVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    public static String getBase64StringNew(String code,int flags) {
        if (code == null || code.equals("")) return null;
        AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = new AesCbcWithIntegrity.CipherTextIvMac(code);
        try {
            String plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, getStoredKeys());
            String cat = new String(Base64.decode(rot13(plainText),flags));
            return cat;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return  null;
    }

    public static String getBase64String(String code,int flags) {
        return new String(Base64.decode(code,flags));
    }

    public static String getStreamQualityLink(String htmlResult,String url) {
        if (htmlResult == null) return url;
        String firstReg = Utils.getChannelRegexFirst(Utils.CHANNELS_QUALITY);
        Matcher resolutions = Pattern.compile(firstReg,Pattern.MULTILINE).matcher(htmlResult);
        ArrayList<String> links = new ArrayList<>();
        List<Integer> list = new ArrayList<Integer>();
        while (resolutions.find()) {
            String bandwithStarter = resolutions.group(0);
            String strToFind = getChannelQualityStrToFind();
            int indexstart =bandwithStarter.indexOf(strToFind);
            String bandwith = bandwithStarter.substring(indexstart+strToFind.length());
            if (bandwith.indexOf(",") > -1)
                bandwith = bandwith.substring(0,bandwith.indexOf(","));
            else if (bandwith.indexOf("\n") > -1)
                bandwith = bandwith.substring(0,bandwith.indexOf("\n"));

            int band = Integer.parseInt(bandwith);
            list.add(band);
            links.add(resolutions.group(0));
        }
        if (links.size() <= 0) return  url;
        int selectedBand = Collections.max(list);
        String tempUrl = url;
        List<String> tempLinks = new LinkedList<>();
        for (String link:links) {
            if (link.contains(String.valueOf(selectedBand))) {
                tempUrl = link;
                tempLinks.add(link);
            }
        }
        //BEST QUALITY for else need to make selection.
        tempUrl = tempLinks.get(0);
        String fileName = tempUrl.substring(tempUrl.lastIndexOf("\n"));
        fileName = fileName.trim();
        fileName = fileName.replace(" ","");
        if (fileName.startsWith("http://") || fileName.startsWith("https://")) {
            return fileName;
        }
        url = url.substring(0,url.lastIndexOf("/")+1);
        url += fileName;
        return url;
    }

    private static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }


    //START REGION NEW

    public static List<GridItem> getStaticIsraelLiveChannelsData(Context ctx) {
        List<GridItem> list = new ArrayList<GridItem>();
        String category = ctx.getString(R.string.ISRAEL_LIVE_CHANNELS_CATEGORY);
        GridItem channelKan = new GridItem.GridItemBuilder()
                .Id(11)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_KAN_11)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_11_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_11_DESC))
                .ImageUrl("newkan")
                .SortOrder(1)
                .build();
        GridItem channelKeshet = new GridItem.GridItemBuilder()
                .Id(12)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_KESHET_12)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_12_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_12_DESC))
                .LinkUrl(getChannel12RealUrl())
                .ImageUrl("newkeshet")
                .SortOrder(2)
                .build();
        GridItem channelReshet = new GridItem.GridItemBuilder()
                .Id(13)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_RESHET_13)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_13_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_13_DESC))
                .LinkUrl(getChannel13RealUrl())
                .ImageUrl("newreshet")
                .SortOrder(3)
                .build();

        GridItem channel23 = new GridItem.GridItemBuilder()
                .Id(23)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_23)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_23_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_23_DESC))
                .ImageUrl("newtv23")
                .SortOrder(6)
                .build();


        GridItem channelMakan = new GridItem.GridItemBuilder()
                .Id(33)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_33)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_MAKAN_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_MAKAN_DESC))
                .LinkUrl("http://www.makan.org.il/live/tv.aspx?stationid=3")
                .ImageUrl("newmakan")
                .SortOrder(9)
                .build();

        GridItem channelKnesset = new GridItem.GridItemBuilder()
                .Id(99)
                .Type(Constants.TYPE_ISRAEL_LIVE_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Live.CHANNEL_99)
                .Category(category)
                .Title(ctx.getString(R.string.CHANNEL_KNESSET_TITLE))
                .Studio(ctx.getString(R.string.CHANNEL_KNESSET_DESC))
                .LinkUrl(getChannel99RealUrl())
                .ImageUrl("newknesset")
                .SortOrder(12)
                .build();

        list.add(channelKan);
        list.add(channelKeshet);
        list.add(channelReshet);
        list.add(channel23);
        list.add(channelMakan);
        list.add(channelKnesset);
        return  list;
    }

    public static List<GridItem> getStaticIsraelRadioData(Context ctx) {
        List<GridItem> list = new ArrayList<GridItem>();
        String category = ctx.getString(R.string.ISRAEL_RADIO_CHANNELS_CATEGORY);
        GridItem channelGlz = new GridItem.GridItemBuilder()
                .Id(5000)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.GLZ)
                .Category(category)
                .Title("גלי צהל")
                .Studio("רדיו גלי צהל")
                .LinkUrl("http://glzwizzlv.bynetcdn.com/glz_mp3?awCollectionId=misc&awEpisodeId=glz")
                .VideoUrl("http://glzwizzlv.bynetcdn.com/glz_mp3?awCollectionId=misc&awEpisodeId=glz")
                .ImageUrl("newglz")
                .SortOrder(5000)
                .build();
        GridItem channelGlGlaz = new GridItem.GridItemBuilder()
                .Id(5001)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.GLGLZ)
                .Category(category)
                .Title("גלגלצ")
                .Studio("רדיו גלגלצ")
                .LinkUrl("http://glzwizzlv.bynetcdn.com/glglz_mp3?awCollectionId=misc&awEpisodeId=glglz")
                .VideoUrl("http://glzwizzlv.bynetcdn.com/glglz_mp3?awCollectionId=misc&awEpisodeId=glglz")
                .ImageUrl("newglglz")
                .SortOrder(5001)
                .build();
        GridItem channel88 = new GridItem.GridItemBuilder()
                .Id(5002)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM88)
                .Category(category)
                .Title("כאן 88")
                .Studio("רדיו כאן 88")
                .LinkUrl("https://www.kan.org.il/live/radio.aspx?stationid=4")
                .ImageUrl("newradio88")
                .SortOrder(5002)
                .build();
        GridItem channel90fm = new GridItem.GridItemBuilder()
                .Id(5003)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM90)
                .Category(category)
                .Title("תשעים 90FM")
                .Studio("רדיו תשעים 90FM")
                .LinkUrl("http://live1.co.il/90fm/")
                .ImageUrl("newradio90fm")
                .SortOrder(5003)
                .build();
        GridItem channel97fm = new GridItem.GridItemBuilder()
                .Id(5004)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM97)
                .Category(category)
                .Title("דרום 97FM")
                .Studio("רדיו דרום 97FM")
                .LinkUrl("https://www.ytn.co.il/radiodarom/97fm/")
                .ImageUrl("newradio97fm")
                .SortOrder(5004)
                .build();
        GridItem channel99fm = new GridItem.GridItemBuilder()
                .Id(5005)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM99)
                .Category(category)
                .Title("אקו 99FM")
                .Studio("רדיו אקו 99FM")
                .LinkUrl("http://eco99fm.maariv.co.il/Chart/LiveBrodcast.aspx")
                .ImageUrl("newradio99fm")
                .SortOrder(5005)
                .build();
        GridItem channel100fm = new GridItem.GridItemBuilder()
                .Id(5006)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM100)
                .Category(category)
                .Title("רדיוס 100FM")
                .Studio("רדיו רדיוס 100FM")
                .LinkUrl("http://www.100fm.co.il/Player_new/ind.html")
                .ImageUrl("newradio100fm")
                .SortOrder(5006)
                .build();
        GridItem channel101fm = new GridItem.GridItemBuilder()
                .Id(5007)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM101)
                .Category(category)
                .Title("ירושלים 101FM")
                .Studio("רדיו ירושלים 101FM")
                .LinkUrl("http://live1.co.il/jrs101fm/")
                .ImageUrl("newradio101fm")
                .SortOrder(5007)
                .build();
        GridItem channel102fm = new GridItem.GridItemBuilder()
                .Id(5008)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM102)
                .Category(category)
                .Title("תל אביב 102FM")
                .Studio("רדיו תל אביב 102FM")
                .LinkUrl("http://102fm.co.il/scripts/scripts.min.js")
                .ImageUrl("newradio102fm")
                .SortOrder(5008)
                .build();
        GridItem channel102Eilatfm = new GridItem.GridItemBuilder()
                .Id(5009)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM102EILAT)
                .Category(category)
                .Title("קול הים האדום 102FM")
                .Studio("רדיו קול הים האדום 102FM")
                .LinkUrl("https://www.fm102.co.il/LiveBroadcast")
                .ImageUrl("newradio102fmeilat")
                .SortOrder(5009)
                .build();
        GridItem channel103fm = new GridItem.GridItemBuilder()
                .Id(5010)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM103)
                .Category(category)
                .Title("ללא הפסקה 103FM")
                .Studio("רדיו ללא הפסקה 103FM")
                .LinkUrl("http://103fm.maariv.co.il/include/OnLineView.aspx")
                .ImageUrl("newradio103fm")
                .SortOrder(5010)
                .build();

        GridItem channel1045fm = new GridItem.GridItemBuilder()
                .Id(5011)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM1045)
                .Category(category)
                .Title("104.5FM רדיו צפון")
                .Studio("104.5FM רדיו צפון")
                .LinkUrl("http://1045fm.maariv.co.il/include/OnLineView.aspx")
                .ImageUrl("newradio1045fm")
                .SortOrder(5011)
                .build();

        GridItem channel1075fm = new GridItem.GridItemBuilder()
                .Id(5011)
                .Type(Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL)
                .ChannelTag(Constants.Channels.Israel.Radio.FM1075)
                .Category(category)
                .Title("107.5FM רדיו חיפה")
                .Studio("107.5FM רדיו חיפה")
                .LinkUrl("http://www.1075.fm/%d7%a0%d7%92%d7%9f/")
                .ImageUrl("newradio1075fm")
                .SortOrder(5011)
                .build();



        list.add(channelGlz);
        list.add(channelGlGlaz);
        list.add(channel88);
        list.add(channel90fm);
        list.add(channel97fm);
        list.add(channel99fm);
        list.add(channel100fm);
        list.add(channel101fm);
        list.add(channel102Eilatfm);
        list.add(channel102fm);
        list.add(channel103fm);
        list.add(channel1045fm);
        list.add(channel1075fm);

        return  list;

    }

    //END REGION NEW

    public static String setRegexForRadioStation(String tag) {
        switch (tag) {
            case Constants.Channels.Israel.Radio.FM90:
            case Constants.Channels.Israel.Radio.FM101:
            case Constants.Channels.Israel.Radio.FM102EILAT:
            case Constants.Channels.Israel.Radio.FM1075:
                return "mp3:\\s*\"(.*?)\"";
            case Constants.Channels.Israel.Radio.FM97:
                return "<video.*src=\"(.*?)\"";
            case Constants.Channels.Israel.Radio.FM100:
                return "m4a:\\s*\"(.*?)\"";
            case Constants.Channels.Israel.Radio.FM102:
                return "radioPlayerStream=\\{.*?\\:\"(.*?)\"\\}";
            case Constants.Channels.Israel.Radio.FM103:
            case Constants.Channels.Israel.Radio.FM1045:
                return "data-file=\"(.*?)\"";
            default:
                return "";
        }
    }

    public static int setFlagsForRadioStation(String tag) {
        switch (tag) {
            default:
                return -1;
        }
    }

    private static String getChannelQualityStrToFind() {
        return getBase64StringNew("q3S/YLmJMXTWsmN0fObVjQ==:UNWOEIQ+f58va9phQz64uzfj5aAeKi1oPQ+TdAJWmS8=:64hIxFhQ2z9Sws4HoHVQhoiSxv7uS4rDU9AEAno6ERc=",Base64.NO_WRAP);
    }

    public static String getUpdateVersionUrl() {
        return getBase64StringNew("ILrtnaVw7LQKWQoQa18GUg==:AaUGChtJF0Vo1jHgwAEqe554mzp4aXOfCsdVzlohw4A=:z/CNcwfT0Rr68lPGzX2lJxt6PXMh0YpykPsXgI0rCZU=",Base64.NO_WRAP);
        //return getBase64StringNew("tstBVsiu+hou0lUH8hOMVw==:KogCz7MJRaduOWfhkJTimIy9lHTVrlbHprLZAyORYhA=:jlqk5f/80Hak7TB9PIe1RfvgArbhiIKC1q8meRe/ffMV1O2Q/BF6c+DvW9FpGfa1",Base64.NO_WRAP);
    }

    public static String getAppUrl() {
        return getBase64StringNew("4WYiZNjTVVCJY+KBNQH1EQ==:iKNeSmpPs09/NUm9+VYRrB2w7FKFhbfxQknDLL4abDw=:SezefOoaf/COoeia0hDBlgP1JdkpGYtOKnwF4IrBNM9MKZIXw9s8DJ2Ye0x4ZR9T",Base64.NO_WRAP);
    }




















    public static void openPlayBackAcitivty(FragmentActivity activity,GridItem gridItem, boolean loadLiveChannels) {
        Intent intent = new Intent(activity, PlaybackActivity.class);
        intent.putExtra(Constants.PARCEL_GRID_ITEM, gridItem);
        intent.putExtra(Constants.LOAD_LIVE_CHANNELS,loadLiveChannels);
        activity.startActivity(intent);
    }


    private static String getChannel12RealUrl() {
        return getBase64StringNew("R7dRYHwvSQ7a32VL6F7atg==:hsZLVp6AYKzpRDBTbmpuZkrqvuQrGL4H2fcteAFb7f4=:zhCFhOuRXKHGacIiHwori+Ownavyv8cRpkLjkZsr94QLREVXEZNAj0wDMzt1zDPOV4fgch3YvHA9oc89Vw1casmDDGF7Z106HxX8fNKtLKR4b/SjVsUROzLL+5MRN5+5gdYTeqF05HX6QCVvMMhG5Q==",Base64.NO_WRAP);
    }

    private static String getChannel13RealUrl() {
        return getBase64StringNew("k4/MbtVw7SI/Lt3VhxG/Qg==:AldSu0zXAhkSLVfEv+zPDHBcSJu5H9zaMEmH5ylh2f0=:sGb2WBVq0RcBz1MltYvitAiWXegjh7pfRHn2eOPx/VjX4rKZkjGtWns6OYwJ7vYUQXIoK0Z9tXJ0d1YFaY4KI2+mt90C9FCNeYtawT6JrcUm0HBd5Lwdhdts+sUGfO8a71EQyJcXBLBuTMTXy/fXcg==",Base64.NO_WRAP);
    }

    private static String getChannel99RealUrl() {
        return getBase64StringNew("9wb51kcRbsHX1dYxowwd9A==:c+BTYVWwD8A6HZl7gdpWvKnJ7EN1jcC+bSVBYzwmd2g=:92Ch1gYJEXgbXCBDrD2Hnjrt2385TUY1byOGq08OIjD9WW406n5dSqzqZQPXNuEnvbb+xBMnUMut0q/7mqhI5sbL/3F4Fit8HgFdWdvFnfVhtaXmoTW3IqnOB1LHsUn4",Base64.NO_WRAP);
    }

    public static String getChannelRegexFirst(int channel) {
        switch (channel) {
             case CHANNEL_11:
                return  getBase64StringNew("NF0V9o/zaX3ZomO3KPTqHg==:wU3JWXf7Ah5iCbWti22XvZYvwShHvqYx9pStMpPMpo8=:W23sGk92MV4wLCNcQLFu9Drk/1uEYCtKoYukn7cVFYQozrU9Sl5CN0DOcw/5ZXlkNN+5CPlcr9EVNWr8vDk82A==",Base64.NO_WRAP);
            case CHANNEL_33:
                return  "id=\"playerFrame\".*?src=\"(.*?)\"";
            case CHANNEL_23:
                return getBase64StringNew("l7IrcpKJSntF0fP6kTf5fQ==:+VVDtUPxEnl2H9Uk68YRAWuU99rblyrXA29yILI0Bqw=:+GpuUpZl9v9fXxnGHyKVFiVpUHaBm/BtPiU27WEp2BF3pXPI07uUqdCpJm1Qsu9A14ECbusxLUEwxtQV3S68cw==",Base64.NO_WRAP);
            case CHANNEL_97:
                return getBase64StringNew("BfMitwa4C/X0oT45jvmlNw==:oNTCrLNF6SB57aEdbDWKMs9m67382DEkjHCc4Bc2H/E=:b+H4jZqdJkkCmZtlUxzeMq6VoI2PZxRnkO/S2YZy4Ee6QVKCQ5iwt/jo5/7ujHO4p+y/+ipnGOblUG3oQwZUEg==",Base64.NO_WRAP);
            case CHANNEL_99:
                return getBase64StringNew("yQweL8zNQKK+KLuh0tPGiQ==:B6mlh8H8uyI/p3h9Ej0VWso7b+zb9gQPyGEVIa1Sd1w=:BQd2BNrRF+7TfECUE9qcAg5zJv3Fj/cFky5mlhbll+4=",Base64.NO_WRAP);
            case CHANNEL_RADIO_KAN:
                return getBase64StringNew("UUggAIoNuLqrR8XyFxkgvg==:ZudoIiJHvE2ZGl3OQ6LFS1dWaC2MFxfOLLwArK7Mam0=:Ihm3OKBlQK5yyV5KatBXIwU355dpCLsTgwC8whDs3OhO+seB0iAsAXlkTE+MscMh",Base64.NO_WRAP);
            case CHANNEL_RADIO_99:
                return getBase64StringNew("6sVsSXh8GGhifaXkkMKUvA==:ey/83tsI/kJQJtTn6S6dqUtYd7lkUXtiqPl0COyEj38=:qOXXQcs65I32RMMBqr+HZw64FksW22dC3Rca0X0vKhczZ76ifdJiMqsovO8IbNFAigHMvJjHoTr/iuORDDn7lg==",Base64.NO_WRAP);
            case CHANNELS_QUALITY:
                return getBase64StringNew("F1+3N7HZYK75Ayf6W8yn9Q==:yt86Rq8ZR6IIUWeYIBoH9YTBuBEJhOX+hUrko9b68Kw=:qi9CW25dN6/GfLwmRhvZa47sXJbI0y2mov2D2TTvBHBOAxTcDbjpvS9iKbO4JETSZvuA3VgVtny5yUeTjOfMSjXs9d0dtMfgqNfmwR59EnU=",Base64.NO_WRAP);
            default:
                return  null;
        }
    }

    public static String getBaseUrlEmpty() {
        return getBase64StringNew("5Zqivh0CEcB9jSRIPMOD5w==:QwBogKiSmaBmTknRtAErJhwsxYNyB8fGacgrijAEFEw=:ImeE5QQH5yl7UJWl2bzsWDdv40XGzwGEQ7xO+cUVLZM=",Base64.NO_WRAP);
    }

    /*
     * Making sure public utility methods remain static
     */
    private Utils() {
    }




    /**
     * Returns the screen/display size.
     */
    public static Point getDisplaySize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        // You can get the height & width like such:
        // int width = size.x;
        // int height = size.y;
        return size;
    }

    public static SecretKey generateKey(String password)
    {
        return  new SecretKeySpec(password.getBytes(), "DES");
    }

    public static String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher;
        cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }


    public static String getUserAgent() {
        final int random = new Random().nextInt(userAgents.length);
        return userAgents[random];
    }
    private static String[] userAgents =new String[]{
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/600.8.9 (KHTML, like Gecko) Version/8.0.8 Safari/600.8.9",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/600.7.12 (KHTML, like Gecko) Version/8.0.7 Safari/600.7.12",
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240",
            "Mozilla/5.0 (Windows NT 6.1; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Windows NT 5.1; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/600.8.9 (KHTML, like Gecko) Version/7.1.8 Safari/537.85.17",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/600.5.17 (KHTML, like Gecko) Version/8.0.5 Safari/600.5.17",
            "Mozilla/5.0 (X11; Linux x86_64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:39.0) Gecko/20100101 Firefox/39.0",
            "Mozilla/5.0 (Windows NT 6.1; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/600.6.3 (KHTML, like Gecko) Version/8.0.6 Safari/600.6.3",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/44.0.2403.89 Chrome/44.0.2403.89 Safari/537.36",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11) AppleWebKit/601.1.56 (KHTML, like Gecko) Version/9.0 Safari/601.1.56",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11) AppleWebKit/601.1.50 (KHTML, like Gecko) Version/9.0 Safari/601.1.50",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.125 Safari/537.36",
            "Mozilla/5.0 (X11; Fedora; Linux x86_64; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.3.18 (KHTML, like Gecko) Version/8.0.3 Safari/600.3.18",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/43.0.2357.130 Chrome/43.0.2357.130 Safari/537.36",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0; Trident/5.0;  Trident/5.0)",
            "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0;  Trident/5.0)",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.155 Safari/537.36 OPR/31.0.1889.174",
            "Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:39.0) Gecko/20100101 Firefox/39.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:40.0) Gecko/20100101 Firefox/40.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.1; rv:38.0) Gecko/20100101 Firefox/38.0",
            "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0",
            "Mozilla/5.0 (X11; Linux x86_64; rv:38.0) Gecko/20100101 Firefox/38.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.78.2 (KHTML, like Gecko) Version/6.1.6 Safari/537.78.2",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_5) AppleWebKit/600.7.12 (KHTML, like Gecko) Version/7.1.7 Safari/537.85.16",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:38.0) Gecko/20100101 Firefox/38.0",
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.157 Safari/537.36",
            "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.85 Safari/537.36",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36"
    };
}
