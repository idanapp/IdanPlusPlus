/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.idan.plusplus.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.ClassPresenterSelector;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.PlaybackSeekDataProvider;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.idan.plusplus.Classes.Constants;
import com.example.idan.plusplus.R;
import com.example.idan.plusplus.Support.VideoSupportFragmentExt;
import com.example.idan.plusplus.Support.VideoSupportFragmentGlueHostExt;
import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.App.WebapiSingleton;
import com.example.idan.plusplus.V2.Events.OnAsyncTaskLoadCompletes;
import com.example.idan.plusplus.model.GridItem;
import com.example.idan.plusplus.model.Playlist;
import com.example.idan.plusplus.player.VideoPlayerGlue;
import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SubtitleView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Dns;
import okhttp3.OkHttpClient;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;
import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_ALLOW_NON_IDR_KEYFRAMES;
import static com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory.FLAG_DETECT_ACCESS_UNITS;

//import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

/**
 * Plays selected video, loads playlist and related videos, and delegates playback to {@link
 * VideoPlayerGlue}.
 */
public class PlaybackFragment extends VideoSupportFragmentExt {

    private static final int UPDATE_DELAY = 16;
    private VideoPlayerGlue mPlayerGlue;
    private LeanbackPlayerAdapter mPlayerAdapter;
    private SimpleExoPlayer mPlayer;
    // private SimpleExoPlayer mExoPlayer;
    private TrackSelector mTrackSelector;
    private PlaylistActionListener mPlaylistActionListener;
    private VideoPlayerGlue.OnPlayerStateChangedListener mOnPlayerStateChangedListener;
    private ArrayObjectAdapter mRowsAdapter;
    //private Video mVideo;
    private GridItem mGridItem;
    private Playlist mPlaylist;
    private VideoLoaderCallbacks mVideoLoaderCallbacks;
    private OkHttpClient okHttpClient;
    private boolean loadLiveChannels = true;
    private boolean isScaleUp = true;
    private boolean firstTimeLoad = true;
    private DefaultRenderersFactory mDefaultRenderers;
    private PackageManager packageManager;
    private int oldState;
    private FrameLayout mMediaFrameLayout;
    private Boolean showCast = true;
    private BarVisualizer mVisualizer;
    private int sessionId = 0;
    private SubtitleView subtitleView;
    private ConcatenatingMediaSource mConcatenatingMediaSource;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageManager = getActivity().getPackageManager();
        BackgroundManager mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
        mBackgroundManager.setColor(Color.BLACK);
        mGridItem = getActivity().getIntent().getParcelableExtra(Constants.PARCEL_GRID_ITEM);
        if (mGridItem == null) getActivity().finish();
        loadLiveChannels = getActivity().getIntent().getBooleanExtra(Constants.LOAD_LIVE_CHANNELS,true);
        mPlaylist = new Playlist();
        List<GridItem> videoList = new ArrayList<>();
        if (loadLiveChannels) {
            switch (mGridItem.type) {
                case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
                    videoList = Utils.getStaticIsraelLiveChannelsData(getContext());
                    break;
                case Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL:
                    videoList = Utils.getStaticIsraelRadioData(getContext());
                    break;
                default:
                    break;
            }
        }

        switch (mGridItem.type) {
            case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
            case Constants.TYPE_IPTV_LIVE_CHANNEL:
                isScaleUp = true;
                break;
            default:
                isScaleUp = false;
                break;
        }

        int currentPosition = 0;
        if (videoList != null && videoList.size() > 0) {
            for (GridItem vid : videoList) {
                mPlaylist.add(vid);
            }
            currentPosition = mPlaylist.getItemIndex(mGridItem);
            mPlaylist.setCurrentPosition(currentPosition);
        }
        mVideoLoaderCallbacks = new VideoLoaderCallbacks(mPlaylist);
    }




    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            subtitleView = getActivity().findViewById(R.id.exo_subtitles);
            mMediaFrameLayout = getActivity().findViewById(R.id.exo_fullscreen_button);
            if (mMediaFrameLayout != null)
                mMediaFrameLayout.setVisibility(View.GONE);

        } catch (Exception e) {
            mMediaFrameLayout = null;
        }

    }





    @Override
    public void onStart() {
        super.onStart();
        //if (Util.SDK_INT > 23 || android.os.Build.MODEL.contains("AFT")) {
        initializePlayer();
        //}
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Works but not well.
        SurfaceView surfaceView = getSurfaceView();
        ViewGroup.LayoutParams p = surfaceView.getLayoutParams();
        p.width = (int)dpToPx(newConfig.screenWidthDp);
        p.height =(int)dpToPx(newConfig.screenHeightDp);
        surfaceView.setLayoutParams(p);

    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (isInPictureInPictureMode) {
            //mUiControls.setVisibility(View.INVISIBLE);
            WebapiSingleton.isInPicInPic = true;
            hideControls(false);
        } else {
            //mUiControls.setVisibility(View.VISIBLE);
            WebapiSingleton.isInPicInPic = false;

        }
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }

    private float dpToPx(float dp){
        Resources r = getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                r.getDisplayMetrics()
        );
        return px;
    }


    /** Pauses the player. */
    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void onPause() {
        super.onPause();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && packageManager
                .hasSystemFeature(
                        PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            if (!getActivity().isInPictureInPictureMode() && mPlayerGlue != null && mPlayerGlue.isPlaying()) {
           /* if (!getActivity().requestVisibleBehind(true)) {
                mPlayerGlue.pause();
            } else {
                getActivity().requestVisibleBehind(false);
            }
            */
                mPlayerGlue.pause();
            }
        } else
        {
            if (mPlayerGlue != null && mPlayerGlue.isPlaying())
                mPlayerGlue.pause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //if (Util.SDK_INT > 23 || android.os.Build.MODEL.contains("AFT")) {
        releasePlayer();
        //}
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
       // Utils.disposedServices();
        if (mVisualizer != null)
            mVisualizer.release();
    }

    private void initializePlayer() {

        okHttpClient = com.example.idan.plusplus.V2.App.WebapiSingleton.getOkHttpClientV2();// WebapiSingleton.okHttpClient;
        DefaultRenderersFactory rf = new DefaultRenderersFactory(getContext());
        rf.setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER);
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        mTrackSelector = new DefaultTrackSelector(getContext(),videoTrackSelectionFactory);
        ((DefaultTrackSelector) mTrackSelector).setParameters(new DefaultTrackSelector.ParametersBuilder(getContext()).setPreferredTextLanguage("he").build()); //new DefaultTrackSelector(getContext(),videoTrackSelectionFactory);
        mPlayer = new SimpleExoPlayer.Builder(getContext(),rf).setTrackSelector(mTrackSelector).build();
        mPlayerAdapter = new LeanbackPlayerAdapter(getActivity(), mPlayer, UPDATE_DELAY);
        mPlaylistActionListener = new PlaylistActionListener(mPlaylist);
        mOnPlayerStateChangedListener = state -> {
            if (oldState == state) return;
            if (state ==  Player.STATE_ENDED) {
                getActivity().onBackPressed();
            } else if (state == Player.STATE_BUFFERING) {
            } else if (state == Player.STATE_READY) {
                if (mVisualizer != null && mGridItem.level < 10) {
                    if (sessionId != mPlayer.getAudioSessionId()) {
                        sessionId = mPlayer.getAudioSessionId();
                        if (sessionId > 0)
                            mVisualizer.setAudioSessionId(sessionId);
                    }
                }
            } else if (state == Player.STATE_IDLE) {

            }
            oldState = state;
        };
        mPlayerGlue = new VideoPlayerGlue(getActivity(),mPlayer, mPlayerAdapter, mPlaylistActionListener,mOnPlayerStateChangedListener);
        mPlayerGlue.setSeekProvider(new PlaybackSeekDataProvider());
        mPlayerGlue.setActivity(getActivity());
        mPlayerGlue.setHost(new VideoSupportFragmentGlueHostExt(this));
        mPlayerGlue.playWhenPrepared();
        hideControls(false);
        setOnKeyInterceptListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int keyCode;
                int keyAction;
                keyCode = keyEvent.getKeyCode();
                keyAction = keyEvent.getAction();
                if (mGridItem == null) return  false;
                if (!isControlsOverlayVisible() && showCast && keyAction == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_P)) {
                    if (mMediaFrameLayout != null)
                        mMediaFrameLayout.setVisibility(View.VISIBLE);
                }
                if (isControlsOverlayVisible() && showCast && keyAction == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    if (mMediaFrameLayout != null)
                        mMediaFrameLayout.setVisibility(View.GONE);
                }
                switch (mGridItem.type) {
                    case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
                    case Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL:
                        if (!isControlsOverlayVisible() && keyAction == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            skipToNext();
                        } else if (!isControlsOverlayVisible() && keyAction == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            skipToPrevious();
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        mRowsAdapter = initializeRelatedVideosRow();
        setAdapter(mRowsAdapter);
        play(mGridItem);
    }




    @Override
    protected void onVideoSizeChanged(int width, int height) {
        if (!isScaleUp) {
            if (mPlayer.getVideoFormat() != null)
                super.onVideoSizeChanged(mPlayer.getVideoFormat().width,mPlayer.getVideoFormat().height);
            else
                super.onVideoSizeChanged(width,height);
        } else {
            super.onVideoSizeChanged(width, height);
        }
        if (isScaleUp && firstTimeLoad) {
            firstTimeLoad = false;
            super.onVideoSizeChanged(Utils.getDisplaySize(getContext()).x,Utils.getDisplaySize(getContext()).y);
        }
    }

    public void hideControls(boolean isAnim) {
        mPlayerGlue.setControlsOverlayAutoHideEnabled(false);
        setControlsOverlayAutoHideEnabled(false);
        hideControlsOverlay(isAnim);
    }

    private void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            mTrackSelector = null;
            mPlayerGlue = null;
            mPlayerAdapter = null;
            mPlaylistActionListener = null;
        }
        WebapiSingleton.clearCookieJar();
        WebapiSingleton.clearHeaders();

    }

    private void play(GridItem gridItem) {
        if (gridItem == null) return;
        switch (gridItem.type) {
            case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
                playFinal(gridItem,null);
                break;
            case Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL:
                playFinalRadio(gridItem,null);
                break;
            default:
                playFinal(gridItem,null);
                break;
        }
    }

    private void playNext(GridItem gridItem) {
        if (gridItem == null) return;
        firstTimeLoad = true;
        if (mPlayerGlue.isPlaying()) {
            mPlayerGlue.pause();
            SystemClock.sleep(100);
        }
        WebapiSingleton.setUserAgent(null);;
        switch (gridItem.type) {
            case Constants.TYPE_ISRAEL_LIVE_CHANNEL:
                playIsraelLiveChannel(gridItem);
                break;
            case Constants.TYPE_ISRAEL_LIVE_RADIO_CHANNEL:
                playIsraelRadioChannel(gridItem);
                break;
            default:
                break;
        }
    }


    private void playIsraelRadioChannel(GridItem gridItem) {
        switch (gridItem.channelTag) {
            case Constants.Channels.Israel.Radio.FM88:
                Utils.getRadioService(null).getRadio88Fm(getActivity(), R.id.playback_fragment_background, gridItem, new OnAsyncTaskLoadCompletes<GridItem>() {
                    @Override
                    public void onAsyncTaskLoadCompletes(GridItem gridItem) {
                        //openRadioPlayBackActivity(activity,gridItem,true);
                        playFinalRadio(gridItem,null);
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
                Utils.getRadioService(null).getRadio(getActivity(), R.id.playback_fragment_background, gridItem, regStr, flags, new OnAsyncTaskLoadCompletes<GridItem>() {
                    @Override
                    public void onAsyncTaskLoadCompletes(GridItem gridItem) {
                        playFinalRadio(gridItem,null);
                        //openPlayBackAcitivty(activity,video,true);
                    }
                });
                break;
            case Constants.Channels.Israel.Radio.FM99:
                gridItem.videoUrl = "http://eco-live.mediacast.co.il/99fm_aac";
                playFinalRadio(gridItem,null);
                break;
            default:
                playFinalRadio(gridItem,WebapiSingleton.getOkHttpClientV2());
                break;
        }
    }

    private void playIsraelLiveChannel(GridItem gridItem) {
        FragmentActivity activity = getActivity();
        int main_fragment = R.id.playback_fragment_background;
        switch (gridItem.channelTag) {
            case Constants.Channels.Israel.Live.CHANNEL_KAN_11:
                Utils.getChannell11(null).getLiveChannell11(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_KESHET_12:
                Utils.getChannell12(null).getLiveChannel12(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_RESHET_13:
                Utils.getChannell13(null).getLiveChannell13(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_23:
                Utils.getChannell11(null).getLiveChannell23(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_33:
                Utils.getChannell11(null).getLiveChannell33(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            case Constants.Channels.Israel.Live.CHANNEL_99:
                Utils.getChannell99().getLiveChannell99(activity,main_fragment,gridItem, gridItem1 -> playFinal(gridItem1,null));
                break;
            default:
                break;
        }
    }

    private void playFinalRadio(GridItem video,OkHttpClient httpClient) {
        mPlayerGlue.setTitle(video.title);
        mPlayerGlue.setSubtitle(video.studio);
        prepareMediaRadioForPlaying(Uri.parse(video.videoUrl),httpClient);
        mPlayerGlue.play();

    }

    private void prepareMediaRadioForPlaying(Uri mediaSourceUri,OkHttpClient httpClient) {
        if (mVisualizer == null)
            mVisualizer = getActivity().findViewById(R.id.blast);
        if (httpClient == null) httpClient = okHttpClient;
        String userAgent = WebapiSingleton.getUserAgent();
        OkHttpDataSourceFactory defaultDataSourceFactory = new OkHttpDataSourceFactory(httpClient, userAgent);
        MediaSource mediaSource1 = new HlsMediaSource.Factory(defaultDataSourceFactory).createMediaSource(mediaSourceUri);
        if (mediaSourceUri.getPath().lastIndexOf(".m3u8") < 0)
            mediaSource1 = new ProgressiveMediaSource.Factory(defaultDataSourceFactory).createMediaSource(mediaSourceUri);

        mPlayer.prepare(mediaSource1);
    }

    private void playFinal(GridItem gridItem,OkHttpClient httpClient) {
        mPlayerGlue.setTitle(gridItem.title);
        mPlayerGlue.setSubtitle(gridItem.description);
        if (gridItem.videoUrl == null) gridItem.videoUrl = "http://";
        try {
            Uri videoUri = Uri.parse(gridItem.videoUrl);
            prepareMediaForPlaying(videoUri,gridItem.type,gridItem.channelTag,httpClient);
        } catch (Exception e) {

        }
        mPlayerGlue.play();
    }

    private void prepareMediaForPlaying(Uri mediaSourceUri, String type,String channelTag, OkHttpClient httpClient) {
        try {
            if (httpClient == null) httpClient = okHttpClient;
            if (httpClient == null) httpClient = WebapiSingleton.getOkHttpClientV2();
            if (httpClient == null) httpClient = com.example.idan.plusplus.V2.App.WebapiSingleton.getOkHttpClientV2();
            httpClient = com.example.idan.plusplus.V2.App.WebapiSingleton.getOkHttpClientV2().newBuilder().dns(Dns.SYSTEM).build();
            String userAgent = WebapiSingleton.getUserAgent();
            userAgent = com.example.idan.plusplus.V2.App.WebapiSingleton.getUserAgent();
            OkHttpDataSourceFactory defaultDataSourceFactory = new OkHttpDataSourceFactory(httpClient, userAgent);
            MediaSource mediaSource1;
            String uriPath = mediaSourceUri.toString();
            if ( (type.equals(Constants.TYPE_IDAN) || uriPath.contains("fvs.io") || uriPath.contains("redirector.googlevideo.com") || uriPath.contains("streamango") || type.equals(Constants.Channels.World.Live.IPTV) || uriPath.contains(".mp4") || uriPath.contains("google.com") || uriPath.contains(".mkv") || uriPath.contains("youtube") || uriPath.contains("m4a")) && !mediaSourceUri.getPath().contains("m3u8")) {
                if (uriPath.startsWith("/")) {
                    DefaultDataSourceFactory defaultDataSourceFactoryNon1 = new DefaultDataSourceFactory(getContext(),userAgent);
                    mediaSource1 = new ProgressiveMediaSource.Factory(defaultDataSourceFactoryNon1).createMediaSource(mediaSourceUri);
                } else
                    mediaSource1 = new ProgressiveMediaSource.Factory(defaultDataSourceFactory).createMediaSource(mediaSourceUri);
            } else if (uriPath.startsWith("rtmp://")) {
                RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();
                mediaSource1 = new ProgressiveMediaSource.Factory(rtmpDataSourceFactory).createMediaSource(mediaSourceUri);
            } else if (type.equals(Constants.TELEMEDIA)) {
                userAgent = Utils.getUserAgent();
                DefaultDataSourceFactory defaultDataSourceFactoryNon1 = new DefaultDataSourceFactory(getContext(),userAgent);
                mediaSource1 = new ProgressiveMediaSource.Factory(defaultDataSourceFactoryNon1).createMediaSource(mediaSourceUri);
            } else {
                DefaultHlsExtractorFactory defaultHlsExtractorFactory = new DefaultHlsExtractorFactory();
                if (type.equals(Constants.TYPE_WORD_LIVE))
                    defaultHlsExtractorFactory = new DefaultHlsExtractorFactory(FLAG_ALLOW_NON_IDR_KEYFRAMES | FLAG_DETECT_ACCESS_UNITS,true);
                if (type.equals(Constants.TYPE_ADULT)) {
                    DefaultDataSourceFactory defaultDataSourceFactoryNon1 = new DefaultDataSourceFactory(getContext(),userAgent);
                    mediaSource1 = new HlsMediaSource.Factory(defaultDataSourceFactoryNon1).setExtractorFactory(defaultHlsExtractorFactory).createMediaSource(mediaSourceUri);
                } else {
                    mediaSource1 = new HlsMediaSource.Factory(defaultDataSourceFactory).setExtractorFactory(defaultHlsExtractorFactory).createMediaSource(mediaSourceUri);
                }


            }
            if (type.equals(Constants.TYPE_MUSIC)) {
                if (mVisualizer == null)
                    mVisualizer = getActivity().findViewById(R.id.blast);
            }

                mConcatenatingMediaSource = new ConcatenatingMediaSource(mediaSource1);
                mPlayer.prepare(mConcatenatingMediaSource);


        } catch (Exception e) {

        }


    }

    private ArrayObjectAdapter initializeRelatedVideosRow() {
        /*
         * To add a new row to the mPlayerAdapter and not lose the controls row that is provided by the
         * glue, we need to compose a new row with the controls row and our related videos row.
         *
         * We start by creating a new {@link ClassPresenterSelector}. Then add the controls row from
         * the media player glue, then add the related videos row.
         */
        ClassPresenterSelector presenterSelector = new ClassPresenterSelector();
        presenterSelector.addClassPresenter(mPlayerGlue.getControlsRow().getClass(), mPlayerGlue.getPlaybackRowPresenter());
        presenterSelector.addClassPresenter(ListRow.class, new ListRowPresenter());
        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(presenterSelector);
        rowsAdapter.add(mPlayerGlue.getControlsRow());
        setOnItemViewClickedListener(new ItemViewClickedListener());
        return rowsAdapter;
    }

    public void skipToNext() {

        mPlayerGlue.next();
    }

    public void skipToPrevious() {
        mPlayerGlue.previous();
    }

    public void rewind() {
        mPlayerGlue.rewind();
    }

    public void fastForward() {
        mPlayerGlue.fastForward();
    }





    /** Opens the video details page when a related video has been clicked. */
    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(
                Presenter.ViewHolder itemViewHolder,
                Object item,
                RowPresenter.ViewHolder rowViewHolder,
                Row row) {


        }
    }

    /** Loads a playlist with videos from a cursor and also updates the related videos cursor. */
    protected class VideoLoaderCallbacks {

        static final int RELATED_VIDEOS_LOADER = 1;
        static final int QUEUE_VIDEOS_LOADER = 2;

        private final Playlist playlist;

        private VideoLoaderCallbacks(Playlist playlist) {
            this.playlist = playlist;
        }
    }

    class PlaylistActionListener implements VideoPlayerGlue.OnActionClickedListener {

        private Playlist mPlaylist;
        private int selectedCaptionIndex = -1;
        private int selectedAudioTrackIndex = -1;

        PlaylistActionListener(Playlist playlist) {
            this.mPlaylist = playlist;
        }

        @Override
        public void onPrevious() {
            switch (mGridItem.channelTag) {
                default:
                    GridItem preVideo = mPlaylist.previous();
                    if (preVideo == null) {
                        mPlaylist.setCurrentPosition(mPlaylist.size());
                        playNext(mPlaylist.previous());
                    } else {
                        playNext(preVideo);
                    }
                    break;
            }
        }

        @Override
        public void onNext() {
            switch (mGridItem.channelTag) {
                default:
                    GridItem nextVideo = mPlaylist.next();
                    if (nextVideo == null) {
                        mPlaylist.setCurrentPosition(-1);
                        playNext(mPlaylist.next());
                    } else {
                        playNext(nextVideo);
                    }
                    break;
            }
        }

        @Override
        public void onScaleUp() {
            isScaleUp = !isScaleUp;
            onVideoSizeChanged(Utils.getDisplaySize(getContext()).x,Utils.getDisplaySize(getContext()).y);
        }

        @Override
        public void onCloseCaption() {
            final Boolean[] isCancel = {false};
            final int[] renderIndex = {C.TRACK_TYPE_TEXT};
            if (selectedCaptionIndex == -1) selectedCaptionIndex = 0;
            DefaultTrackSelector trackSelector = ((DefaultTrackSelector) mTrackSelector);
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo == null) return;
            TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(renderIndex[0]);
            final CharSequence[] selectedKey = {""};
            AtomicReference<Integer> selectedTrack = new AtomicReference<>(0);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setTitle("Please Select subtitle");
            TreeMap<String,Integer> treeMap = new TreeMap<>();
            for (int i=0;i < trackGroupArray.length;i++) {
                TrackGroup trackGroup = trackGroupArray.get(i);
                for (int j=0;j<trackGroup.length;j++) {
                    Format format =  trackGroup.getFormat(j);
                    if (format.language != null) {
                        if (format.label == null)
                            treeMap.put(format.language,i);
                        else
                            treeMap.put(format.label,i);
                    }

                }
            }
            if (treeMap.size() <= 0) return;
            CharSequence[] t = new String[treeMap.size()];
            int ctr=0;
            for (Map.Entry<String,Integer> item:treeMap.entrySet()) {
                t[ctr] =  item.getKey();
                ctr++;
            }
            builder1.setSingleChoiceItems(t, selectedCaptionIndex, (dialog, which) -> {
                selectedCaptionIndex = which;
                selectedKey[0] = t[which];
                dialog.dismiss();
            });
            AlertDialog alert11 = builder1.create();
            alert11.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dlg) {
                    alert11.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR); // set title and message direction to RTL
                }
            });
            alert11.setOnCancelListener(dialogInterface -> {
                isCancel[0] = true;
            });
            alert11.setOnDismissListener(dialogInterface -> {
                if (!isCancel[0]) {
                    selectedTrack.set(treeMap.get(selectedKey[0]));
                    int selectedGroup = selectedTrack.get() == null ? 0 : selectedTrack.get();
                    TrackGroupArray rendererTrackGroups = mappedTrackInfo == null ? null : mappedTrackInfo.getTrackGroups(renderIndex[0]);
                    DefaultTrackSelector.SelectionOverride selectionOverride = new DefaultTrackSelector.SelectionOverride(selectedGroup, 0);
                    trackSelector.setParameters(trackSelector.buildUponParameters().setSelectionOverride(renderIndex[0],rendererTrackGroups, selectionOverride));

                }
            });
            alert11.show();
        }

        @Override
        public void onMoreActions() {
            final Boolean[] isCancel = {false};
            int renderIndex = C.TRACK_TYPE_AUDIO;
            if (selectedAudioTrackIndex == -1) selectedAudioTrackIndex = 0;
            DefaultTrackSelector trackSelector = ((DefaultTrackSelector) mTrackSelector);
            MappingTrackSelector.MappedTrackInfo mappedTrackInfo = trackSelector.getCurrentMappedTrackInfo();
            if (mappedTrackInfo == null) return;
            TrackGroupArray trackGroupArray = mappedTrackInfo.getTrackGroups(renderIndex);
            final CharSequence[] selectedKey = {""};
            AtomicReference<Integer> selectedTrack = new AtomicReference<>(0);
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
            builder1.setTitle("Please Select Audio");
            TreeMap<String,Integer> treeMap = new TreeMap<>();
            for (int i=0;i < trackGroupArray.length;i++) {
                TrackGroup trackGroup = trackGroupArray.get(i);
                for (int j=0;j<trackGroup.length;j++) {
                    Format format =  trackGroup.getFormat(j);
                    if (format.label != null)
                        if (format.label == null)
                            treeMap.put(format.language,i);
                        else
                            treeMap.put(format.label,i);
                }
            }
            if (treeMap.size() <= 0) return;
            CharSequence[] t = new String[treeMap.size()];
            int ctr=0;
            for (Map.Entry<String,Integer> item:treeMap.entrySet()) {
                t[ctr] =  item.getKey();
                ctr++;
            }


            builder1.setSingleChoiceItems(t, selectedAudioTrackIndex, (dialog, which) -> {
                selectedAudioTrackIndex = which;
                selectedKey[0] = t[which];
                dialog.dismiss();
            });
            AlertDialog alert11 = builder1.create();
            alert11.setOnCancelListener(dialogInterface -> {
                isCancel[0] = true;
            });
            alert11.setOnDismissListener(dialogInterface -> {
                if (!isCancel[0]) {
                    selectedTrack.set(treeMap.get(selectedKey[0]));
                    int selectedGroup = selectedTrack.get() == null ? 0 : selectedTrack.get();
                    TrackGroupArray rendererTrackGroups = mappedTrackInfo == null ? null : mappedTrackInfo.getTrackGroups(renderIndex);
                    DefaultTrackSelector.SelectionOverride selectionOverride = new DefaultTrackSelector.SelectionOverride(selectedGroup, 0);
                    trackSelector.setParameters(trackSelector.buildUponParameters().setSelectionOverride(renderIndex,rendererTrackGroups, selectionOverride));
                }
            });
            alert11.show();
        }
    }
}
