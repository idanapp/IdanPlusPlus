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

package com.example.idan.plusplus.presenter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;
import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.request.RequestOptions;
import com.example.idan.plusplus.Classes.GlideApp;
import com.example.idan.plusplus.R;
import com.example.idan.plusplus.model.GridItem;

/*
 * A GridCardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class GridCardPresenter extends Presenter {
    private int mSelectedBackgroundColor = -1;
    private int mDefaultBackgroundColor = -1;
    private Drawable mDefaultCardImage;
    private Fragment mFragment;
    private Drawable mSelectedBackgroundDrawable;

    public GridCardPresenter(Fragment fragment) {
        mFragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Context mContext = parent.getContext();
        mDefaultBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.default_background);
        mSelectedBackgroundColor = ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        mSelectedBackgroundDrawable = ContextCompat.getDrawable(parent.getContext(),R.drawable.cardbg);
        mDefaultCardImage = parent.getResources().getDrawable(R.drawable.movie, null);

        ImageCardView cardView = new ImageCardView(parent.getContext()) {
            @Override
            public void setSelected(boolean selected) {
                updateCardBackgroundColor(this, selected);
                super.setSelected(selected);
            }
        };

        cardView.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                ((TextView) cardView.findViewById(R.id.content_text)).setMaxLines(5);
                ((TextView) cardView.findViewById(R.id.title_text)).setMaxLines(5);
            }
            else {
                ((TextView) cardView.findViewById(R.id.content_text)).setMaxLines(1);
                ((TextView) cardView.findViewById(R.id.title_text)).setMaxLines(1);
            }
        });
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new GridCardViewHolder(cardView, mContext);
        //return new ViewHolder(cardView);
    }

    private void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? mSelectedBackgroundColor : mDefaultBackgroundColor;
        // Both background colors should be set because the view's
        // background is temporarily visible during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
//        if (selected) {
//            view.setBackground(mSelectedBackgroundDrawable);
//            view.findViewById(R.id.info_field).setBackground(mSelectedBackgroundDrawable);
//
//        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        GridItem gridItem = (GridItem) item;
        GridCardViewHolder cardViewHolder = (GridCardViewHolder) viewHolder;
        cardViewHolder.bind(gridItem);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }

    /**
     * The view holder which will encapsulate all the information related to currently bond video.
     */
    private final class GridCardViewHolder extends ViewHolder {
        private GridItem mGridItem;
        private Context mContext;
        private PopupMenu mPopupMenu;
        private FragmentActivity mFragmentActivity;
        private LifecycleOwner mOwner;
        private RequestOptions mDefaultPlaceHolder;
        private Drawable mDefaultBackground;

        private ImageCardView mCardView;

        GridCardViewHolder(ImageCardView view, Context context) {
            super(view);
            mContext = context;

            mOwner = (LifecycleOwner) mContext;

            mDefaultBackground = mContext.getResources().getDrawable(R.drawable.default_background, null);
            mDefaultPlaceHolder = new RequestOptions().
                    placeholder(mDefaultBackground);

            mCardView = (ImageCardView) GridCardPresenter.GridCardViewHolder.this.view;
            Resources resources = mCardView.getContext().getResources();
            mCardView.setMainImageDimensions(Math.round(
                    resources.getDimensionPixelSize(R.dimen.card_width)),
                    resources.getDimensionPixelSize(R.dimen.card_height));
            mFragmentActivity = (FragmentActivity) context;
        }


        private void bind(GridItem gridItem) {
            mGridItem = gridItem;
            mCardView.setTitleText(mGridItem.title);
            mCardView.setContentText(mGridItem.studio);

            if (mGridItem.imageUrl != null) {
               Resources res = mCardView.getResources();
                int width = res.getDimensionPixelSize(R.dimen.card_width);
                int height = res.getDimensionPixelSize(R.dimen.card_height);
                mCardView.setMainImageDimensions(width, height);
                if (mGridItem.imageUrl.startsWith("http://") || mGridItem.imageUrl.startsWith("https://")) {
                    GlideApp.with(mCardView.getContext())
                            .load(mGridItem.imageUrl)
                            .apply(RequestOptions.errorOf(mDefaultCardImage))
                            .into(mCardView.getMainImageView());
                } else {
                        if (mGridItem.imageUrl.startsWith("file://")) {
                            GlideApp.with(mCardView.getContext())
                                    .load(Uri.parse(mGridItem.imageUrl))
                                    .into(mCardView.getMainImageView());
                        } else if (mGridItem.imageUrl.startsWith("base://")) {
                            byte[] imageBytes = Base64.decode(mGridItem.imageUrl.replace("base://",""),Base64.DEFAULT);
                            GlideApp.with(mCardView.getContext())
                                    .load(imageBytes)
                                    .into(mCardView.getMainImageView());
                        } else {
                            GlideApp.with(mCardView.getContext())
                                    .load(mCardView.getContext().getResources().getIdentifier(mGridItem.imageUrl, "drawable", mCardView.getContext().getPackageName()))
                                    .into(mCardView.getMainImageView());
                        }
                }
            }
        }
    }
}
