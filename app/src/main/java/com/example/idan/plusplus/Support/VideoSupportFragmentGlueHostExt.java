package com.example.idan.plusplus.Support;

import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.media.SurfaceHolderGlueHost;
import android.view.SurfaceHolder;

public class VideoSupportFragmentGlueHostExt extends PlaybackSupportFragmentGlueHostExt
        implements SurfaceHolderGlueHost {
    private final VideoSupportFragmentExt mFragment;

    public VideoSupportFragmentGlueHostExt(VideoSupportFragmentExt fragment) {
        super(fragment);
        this.mFragment = fragment;
    }

    /**
     * Sets the {@link android.view.SurfaceHolder.Callback} on the host.
     * {@link PlaybackGlueHost} is assumed to either host the {@link SurfaceHolder} or
     * have a reference to the component hosting it for rendering the video.
     */
    @Override
    public void setSurfaceHolderCallback(SurfaceHolder.Callback callback) {
        mFragment.setSurfaceHolderCallback(callback);
    }
}

