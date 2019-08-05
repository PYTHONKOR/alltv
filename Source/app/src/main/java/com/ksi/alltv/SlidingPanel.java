package com.ksi.alltv;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingPanel {

    private Animation translateTopAnim;
    private Animation translateBottomAnim;
    private LinearLayout slidingPanel;
    private TextView infoView, timeView;
    private ImageView favoriteView;
    private Handler mHandler;
    private Runnable mRunnable;

    private boolean isPanelShow = false;

    public SlidingPanel(Activity activity) {

        slidingPanel = (LinearLayout) activity.findViewById(R.id.slidingPanel);
        infoView = (TextView) activity.findViewById(R.id.infoView);
        timeView = (TextView) activity.findViewById(R.id.timeView);
        favoriteView = (ImageView) activity.findViewById(R.id.favoriteView);

        translateTopAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_top);
        translateBottomAnim = AnimationUtils.loadAnimation(activity, R.anim.translate_bottom);

        translateTopAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPanelShow) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPanelShow = false;
                } else {
                    isPanelShow = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        translateBottomAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPanelShow) {
                    slidingPanel.setVisibility(View.INVISIBLE);
                    isPanelShow = false;
                } else {
                    isPanelShow = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                hideSlidingPanel();
            }
        };
    }

    public void showSlidingPanel(int msec) {

        slidingPanel.startAnimation(translateTopAnim);
        slidingPanel.setVisibility(View.VISIBLE);

        mHandler.postDelayed(mRunnable, msec);
    }

    public void hideSlidingPanel() {

        if (!isPanelShow) return;

        slidingPanel.startAnimation(translateBottomAnim);
        slidingPanel.setVisibility(View.GONE);

        mHandler.removeCallbacks(mRunnable);
    }

    public Boolean isPanelShow() {
        return this.isPanelShow;
    }

    public void closeDelayed(int msec) {
        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, msec);
    }

    public void showFavorite() {
        favoriteView.setVisibility(View.VISIBLE);
    }

    public void hideFavorite() {
        favoriteView.setVisibility(View.GONE);
    }

    public void setInfoText(String text) {
        infoView.setText(text);
    }

    public void setTimeText(String text) {
        timeView.setText(text);
    }
}
