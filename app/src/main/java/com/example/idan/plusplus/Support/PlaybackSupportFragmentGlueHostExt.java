package com.example.idan.plusplus.Support;

import androidx.leanback.media.PlaybackGlueHost;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.OnActionClickedListener;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.PlaybackRowPresenter;
import androidx.leanback.widget.PlaybackSeekUi;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import android.view.View;

public class PlaybackSupportFragmentGlueHostExt extends PlaybackGlueHost implements PlaybackSeekUi {
    private final PlaybackSupportFragmentExt mFragment;

    public PlaybackSupportFragmentGlueHostExt(PlaybackSupportFragmentExt fragment) {
        this.mFragment = fragment;
    }

    @Override
    public void setControlsOverlayAutoHideEnabled(boolean enabled) {
        mFragment.setControlsOverlayAutoHideEnabled(enabled);
    }

    @Override
    public boolean isControlsOverlayAutoHideEnabled() {
        return mFragment.isControlsOverlayAutoHideEnabled();
    }

    @Override
    public void setOnKeyInterceptListener(View.OnKeyListener onKeyListener) {
        mFragment.setOnKeyInterceptListener(onKeyListener);
    }

    @Override
    public void setOnActionClickedListener(final OnActionClickedListener listener) {
        if (listener == null) {
            mFragment.setOnPlaybackItemViewClickedListener(null);
        } else {
            mFragment.setOnPlaybackItemViewClickedListener(new OnItemViewClickedListener() {
                @Override
                public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                          RowPresenter.ViewHolder rowViewHolder, Row row) {
                    if (item instanceof Action) {
                        listener.onActionClicked((Action) item);
                    }
                }
            });
        }
    }

    @Override
    public void setHostCallback(PlaybackGlueHost.HostCallback callback) {
        mFragment.setHostCallback(callback);
    }

    @Override
    public void notifyPlaybackRowChanged() {
        mFragment.notifyPlaybackRowChanged();
    }

    @Override
    public void setPlaybackRowPresenter(PlaybackRowPresenter presenter) {
        mFragment.setPlaybackRowPresenter(presenter);
    }

    @Override
    public void setPlaybackRow(Row row) {
        mFragment.setPlaybackRow(row);
    }

    @Override
    public void fadeOut() {
        mFragment.fadeOut();
    }

    @Override
    public boolean isControlsOverlayVisible() {
        return mFragment.isControlsOverlayVisible();
    }

    @Override
    public void hideControlsOverlay(boolean runAnimation) {
        mFragment.hideControlsOverlay(runAnimation);
    }

    @Override
    public void showControlsOverlay(boolean runAnimation) {
        mFragment.showControlsOverlay(runAnimation);
    }

    @Override
    public void setPlaybackSeekUiClient(PlaybackSeekUi.Client client) {
        mFragment.setPlaybackSeekUiClient(client);
    }

    final PlaybackGlueHost.PlayerCallback mPlayerCallback =
            new PlaybackGlueHost.PlayerCallback() {
                @Override
                public void onBufferingStateChanged(boolean start) {
                    mFragment.onBufferingStateChanged(start);
                }

                @Override
                public void onError(int errorCode, CharSequence errorMessage) {
                    mFragment.onError(errorCode, errorMessage);
                }

                @Override
                public void onVideoSizeChanged(int videoWidth, int videoHeight) {
                    mFragment.onVideoSizeChanged(videoWidth, videoHeight);
                }
            };

    @Override
    public PlaybackGlueHost.PlayerCallback getPlayerCallback() {
        return mPlayerCallback;
    }
}
