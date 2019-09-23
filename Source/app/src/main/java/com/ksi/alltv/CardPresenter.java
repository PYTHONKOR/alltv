/*
 * MIT License
 *
 * Copyright (c) 2019 PYTHONKOR

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ksi.alltv;

import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ViewGroup;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;


public class CardPresenter extends Presenter {

    private static final String TAG = CardPresenter.class.getSimpleName();

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view's background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        // Log.d(TAG, "onCreateViewHolder");

        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.default_background);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.selected_background);
        /*
         * This template uses a default image in res/drawable, but the general case for Android TV
         * will require your resources in xhdpi. For more information, see
         * https://developer.android.com/training/tv/start/layouts.html#density-resources
         */

        ImageCardView cardView =
                new ImageCardView(parent.getContext()) {
                    @Override
                    public void setSelected(boolean selected) {
                        updateCardBackgroundColor(this, selected);
                        super.setSelected(selected);
                    }
                };

        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

        ChannelData tvCh = (ChannelData) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        //Log.d(TAG, "onBindViewHolder");

        if (tvCh.getStillImageUrl() != null) {

            ArrayList<EPGData> epgData = tvCh.getEPG();
            Date date = new Date();

            Boolean isAdultContent = false;

            for(int i=0; i<epgData.size(); i++) {
                if(date.compareTo(epgData.get(i).getEndTime()) < 0) {
                    tvCh.setProgramName(epgData.get(i).getProgramName());
                    isAdultContent = epgData.get(i).isAdultContent();
                    break;
                }
            }

            if(tvCh.getProgramName().equals("") || isAdultContent)
                cardView.setTitleText(tvCh.getTitle());
            else
                cardView.setTitleText(tvCh.getTitle() + " - " + tvCh.getProgramName());

            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);

            int ret = tvCh.getFavorite();

            if(ret == 0)
                cardView.setBadgeImage(null);
            else if(ret == 1)
                cardView.setBadgeImage(cardView.getResources().getDrawable(R.drawable.star_icon, null));
            else if(ret == 2)
                cardView.setBadgeImage(cardView.getResources().getDrawable(R.drawable.wavve_icon_24, null));
            else if(ret == 3)
                cardView.setBadgeImage(cardView.getResources().getDrawable(R.drawable.tving_icon_24, null));
            else if(ret == 4)
                cardView.setBadgeImage(cardView.getResources().getDrawable(R.drawable.oksusu_icon_24, null));

            Picasso.get().load(tvCh.getStillImageUrl())
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(cardView.getMainImageView());

        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

        // Log.d(TAG, "onUnbindViewHolder");

        ImageCardView cardView = (ImageCardView) viewHolder.view;

        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }

}

