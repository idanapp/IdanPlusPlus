/*
 * Copyright (c) 2014 The Android Open Source Project
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

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.BackgroundManager;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.OnItemViewSelectedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.PresenterSelector;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;

import com.example.idan.plusplus.BuildConfig;
import com.example.idan.plusplus.Classes.Constants;
import com.example.idan.plusplus.Dialogs.AppUpdateDialogActivity;
import com.example.idan.plusplus.R;
import com.example.idan.plusplus.Utils;
import com.example.idan.plusplus.V2.Tasks.GetUpdateAppAsyncTask;
import com.example.idan.plusplus.model.GridItem;
import com.example.idan.plusplus.presenter.GridCardPresenter;
import com.example.idan.plusplus.presenter.IconHeaderItemPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Timer;


/*
 * Main class to show BrowseFragment with header and rows of videos
 */
public class MainFragment extends BrowseSupportFragment {
    private MainFragment mThis = this;
    private ArrayObjectAdapter mCategoryRowAdapter;
    private Drawable mDefaultBackground;
    private DisplayMetrics mMetrics;
    private Timer mBackgroundTimer;
    private BackgroundManager mBackgroundManager;

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void startUpdateTask()  {
        GetUpdateAppAsyncTask updateAppAsyncTask = new GetUpdateAppAsyncTask(getActivity(), R.id.main_frame, (localVer, serverVer, file) -> {
            if (localVer > 0 && serverVer > 0 && file != null) {
                Intent intent = new Intent(getActivity(), AppUpdateDialogActivity.class);
                intent.putExtra("NewVer", serverVer);
                intent.putExtra("OldVer", localVer);
                intent.putExtra("urlToUpdateFile",file);
                startActivity(intent);
            } else if (localVer != -1 && serverVer != -1 && file != null) {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle(Objects.requireNonNull(getActivity()).getString(R.string.UPDAE_ERROR_MSESSAGE_TITLE));
                builder1.setNeutralButton(getActivity().getString(R.string.BTN_GOTIT), (dialogInterface, i) -> dialogInterface.dismiss());
                builder1.setMessage(getActivity().getString(R.string.UPDAE_ERROR_MSESSAGE_TITLE));
                AlertDialog alert11 = builder1.create();
                alert11.setOnCancelListener(dialogInterface -> {
                    getActivity().finish();
                });
                alert11.setOnDismissListener(dialogInterface -> {
                    getActivity().finish();
                });
                alert11.show();
            }
        });
        if (!BuildConfig.DEBUG) {
            updateAppAsyncTask.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "The app was not allowed to write in your storage", Toast.LENGTH_LONG).show();
                Objects.requireNonNull(getActivity()).finish();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
        prepareEntranceTransition();

        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().add(R.id.main_frame, ((MainActivity)getActivity()).mSpinnerFragment).commit();
        ListRowPresenter listRowPresenter = new ListRowPresenter();
        mCategoryRowAdapter = new ArrayObjectAdapter(listRowPresenter);
        mCategoryRowAdapter.clear();

        loadStaticIsraelLiveChannelData();
        loadStaticIsraelRadioChannelData();

        startEntranceTransition();
        setAdapter(mCategoryRowAdapter);
        getActivity().getSupportFragmentManager().beginTransaction().remove(((MainActivity)getActivity()).mSpinnerFragment).commit();
        switch (Utils.checkAppStart(getContext())) {
            case NORMAL:
                break;
            case FIRST_TIME_VERSION:
                showPopups( getString(R.string.WHAT_IS_NEW_TXT) +
                        getString(R.string.ENGLISH_DISCLAMIER) +
                        getString(R.string.HEBREW_DISCLAMIER));
                break;
            case FIRST_TIME:
                showPopups(getString(R.string.ENGLISH_DISCLAMIER) +
                        getString(R.string.HEBREW_DISCLAMIER));
                break;
            default:
                break;
        }
        startUpdateTask();
    }

    @Override
    public void onDestroy() {
        if (null != mBackgroundTimer) {
            mBackgroundTimer.cancel();
            mBackgroundTimer = null;
        }
        mBackgroundManager = null;
        Utils.disposedServices();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        if (mBackgroundManager != null)
            mBackgroundManager.release();
        super.onStop();
    }

    @Override
    public void onStart() {
        Utils.disposedServices();
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            };
            if (!hasPermissions(getContext(), PERMISSIONS)) {
                requestPermissions(PERMISSIONS,Constants.PERMISSION_REQUEST);
            } else {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().finish();
                }
            }
        } else {
            try {

            } catch (Exception e) {
                e.printStackTrace();
                getActivity().finish();
            }
        }
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private void prepareBackgroundManager() {
        mBackgroundManager = BackgroundManager.getInstance(Objects.requireNonNull(getActivity()));
        mDefaultBackground = getResources().getDrawable(R.drawable.default_background, null);
        mMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    }

    private void setupUIElements() {
        setBadgeDrawable(
                Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.logo_new, null));
        setTitle(getString(R.string.browse_title)); // Badge, when set, takes precedent over title
        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(false);
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.fastlane_background));
        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {
                return new IconHeaderItemPresenter();
            }
        });
    }

    private void setupEventListeners() {
        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());
    }


    //REGION NEW
    private void loadStaticIsraelLiveChannelData() {
        List<GridItem> list = Utils.getStaticIsraelLiveChannelsData(getContext());
        GridCardPresenter gridCardPresenter = new GridCardPresenter(mThis);
        HeaderItem header = new HeaderItem(1,getString(R.string.ISRAEL_LIVE_CHANNELS_CATEGORY));
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(gridCardPresenter);
        listRowAdapter.addAll(0,list);
        mCategoryRowAdapter.add(new ListRow(header, listRowAdapter));
    }

    private void loadStaticIsraelRadioChannelData() {
        List<GridItem> list = Utils.getStaticIsraelRadioData(getContext());
        GridCardPresenter gridCardPresenter = new GridCardPresenter(mThis);
        HeaderItem header = new HeaderItem(0,getString(R.string.ISRAEL_RADIO_CHANNELS_CATEGORY));
        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(gridCardPresenter);
        listRowAdapter.addAll(0,list);
        mCategoryRowAdapter.add(new ListRow(header, listRowAdapter));
    }

    //REGION NEW


    private void showPopups(String msg) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setMessage(Html.fromHtml(msg));
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                R.string.BTN_GOTIT,
                (dialog, id) -> dialog.cancel());

        AlertDialog alert11 = builder1.create();
        alert11.show();
        ((TextView)alert11.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            //showPopups();
            if (item instanceof GridItem) {
                GridItem gridItem = (GridItem)item;
                Utils.gridItemClicked(getActivity(),R.id.main_frame,gridItem);
            }

        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

        }
    }
}