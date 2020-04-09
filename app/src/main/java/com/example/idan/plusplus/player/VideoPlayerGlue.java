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

package com.example.idan.plusplus.player;

import android.app.PictureInPictureParams;
import android.content.Context;
import android.os.Build;

import androidx.fragment.app.FragmentActivity;
import androidx.leanback.media.PlaybackTransportControlGlue;
import androidx.leanback.widget.Action;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.PlaybackControlsRow;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.leanback.LeanbackPlayerAdapter;

import java.util.concurrent.TimeUnit;

//import android.support.v17.leanback.media.PlaybackTransportControlGlue;

/**
 * Manages customizing the actions in the {@link PlaybackControlsRow}. Adds and manages the
 * following actions to the primary and secondary controls:
 *
 * <ul>
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RepeatAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsDownAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.ThumbsUpAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipPreviousAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.SkipNextAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.FastForwardAction}
 *   <li>{@link androidx.leanback.widget.PlaybackControlsRow.RewindAction}
 * </ul>
 *
 * Note that the superclass, {@link PlaybackTransportControlGlue}, manages the playback controls
 * row.
 */
public class VideoPlayerGlue extends PlaybackTransportControlGlue<LeanbackPlayerAdapter> {

    private static final long TEN_SECONDS = TimeUnit.SECONDS.toMillis(10);
    public FragmentActivity activity;


    public void setActivity(FragmentActivity activity) {
        this.activity = activity;
    }

    /** Listens for when skip to next and previous actions have been dispatched. */
    public interface OnActionClickedListener {

        /** Skip to the previous item in the queue. */
        void onPrevious();

        /** Skip to the next item in the queue. */
        void onNext();

        void onScaleUp();

        void onCloseCaption();

        void onMoreActions();
    }

    public interface OnPlayerStateChangedListener {
        void onPlayStateChanged(int state);
    }

    private final OnActionClickedListener mActionListener;
    private final OnPlayerStateChangedListener mOnPlayerStateChangedListener;
    private final SimpleExoPlayer mPlayer;
    private PlaybackControlsRow.SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.SkipNextAction mSkipNextAction;
    private PlaybackControlsRow.FastForwardAction mFastForwardAction;
    private PlaybackControlsRow.RewindAction mRewindAction;
    private PlaybackControlsRow.PictureInPictureAction mPipAction;
    private PlaybackControlsRow.HighQualityAction mHighQuality;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptionAction;

    public VideoPlayerGlue(
            Context context,
            SimpleExoPlayer player,
            LeanbackPlayerAdapter playerAdapter,
            OnActionClickedListener actionListener,
            OnPlayerStateChangedListener onPlayerStateChangedListener) {
        super(context, playerAdapter);

        mActionListener = actionListener;
        mOnPlayerStateChangedListener = onPlayerStateChangedListener;
        mPlayer = player;

        mSkipPreviousAction = new PlaybackControlsRow.SkipPreviousAction(context);
        mSkipNextAction = new PlaybackControlsRow.SkipNextAction(context);
        mFastForwardAction = new PlaybackControlsRow.FastForwardAction(context);
        mRewindAction = new PlaybackControlsRow.RewindAction(context);
        mPipAction = new PlaybackControlsRow.PictureInPictureAction(context);
        mHighQuality = new PlaybackControlsRow.HighQualityAction(context);
        mClosedCaptionAction = new PlaybackControlsRow.ClosedCaptioningAction(context);
    }

    @Override
    protected void onPlayStateChanged() {
        mOnPlayerStateChangedListener.onPlayStateChanged(mPlayer.getPlaybackState());
        super.onPlayStateChanged();
    }

    @Override
    protected void onCreatePrimaryActions(ArrayObjectAdapter adapter) {
        // Order matters, super.onCreatePrimaryActions() will create the play / pause action.
        // Will display as follows:
        // play/pause, previous, rewind, fast forward, next
        //   > /||      |<        <<        >>         >|
        super.onCreatePrimaryActions(adapter);
        adapter.add(mSkipPreviousAction);
        adapter.add(mRewindAction);
        adapter.add(mFastForwardAction);
        adapter.add(mSkipNextAction);
        adapter.add(mPipAction);
        adapter.add(mHighQuality);
        //adapter.add(mClosedCaptionAction);
        //adapter.add(mCastAction);
    }

    @Override
    protected void onCreateSecondaryActions(ArrayObjectAdapter adapter) {
        super.onCreateSecondaryActions(adapter);
    }

    @Override
    public void onActionClicked(Action action) {
        if (shouldDispatchAction(action)) {
            dispatchAction(action);
            return;
        }
        // Super class handles play/pause and delegates to abstract methods next()/previous().
        super.onActionClicked(action);
    }

    // Should dispatch actions that the super class does not supply callbacks for.
    private boolean shouldDispatchAction(Action action) {
        return action == mRewindAction
                || action == mFastForwardAction
                || action == mPipAction
                || action == mHighQuality;
    }

    private void dispatchAction(Action action) {
        // Primary actions are handled manually.
        if (action == mRewindAction) {
            rewind();
        } else if (action == mFastForwardAction) {
            fastForward();
        } else if (action == mPipAction) {
            showPip();
        } else if(action == mHighQuality) {
            onScaleUp();
        } else if (action == mClosedCaptionAction) {
            onCloseCaption();
        } else if (action instanceof PlaybackControlsRow.MultiAction) {
            PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
            multiAction.nextIndex();
            // Notify adapter of action changes to handle secondary actions, such as, thumbs up/down
            // and repeat.
            notifyActionChanged(
                    multiAction,
                    (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter());
        }
    }

    private void notifyActionChanged(
            PlaybackControlsRow.MultiAction action, ArrayObjectAdapter adapter) {
        if (adapter != null) {
            int index = adapter.indexOf(action);
            if (index >= 0) {
                adapter.notifyArrayItemRangeChanged(index, 1);
            }
        }
    }

    @Override
    public void next() {
        mActionListener.onNext();
    }

    @Override
    public void previous() {
        mActionListener.onPrevious();
    }

    public void onScaleUp() {
        mActionListener.onScaleUp();
    }

    public void onCloseCaption() {mActionListener.onCloseCaption();}

    public void onMoreActions() {mActionListener.onMoreActions();}
    //public void onCast() {mActionListener.onCast(); }

    /** Skips backwards 10 seconds. */
    public void rewind() {
        long newPosition = getCurrentPosition() - TEN_SECONDS;
        newPosition = (newPosition < 0) ? 0 : newPosition;
        getPlayerAdapter().seekTo(newPosition);
    }

    /** Skips forward 10 seconds. */
    public void fastForward() {
        if (getDuration() > -1) {
            long newPosition = getCurrentPosition() + TEN_SECONDS;
            newPosition = (newPosition > getDuration()) ? getDuration() : newPosition;
            getPlayerAdapter().seekTo(newPosition);
        }
    }

    private void showPip() {
        if (this.activity == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //int videoWidth = mPlayer.getVideoFormat().width;
            //int videoHeight = mPlayer.getVideoFormat().height;
            PictureInPictureParams params = null;
                params = new PictureInPictureParams.Builder()
                        //.setAspectRatio(new Rational(videoWidth, videoHeight))
                        .build();
            this.activity.enterPictureInPictureMode(params);
        }
    }


}
