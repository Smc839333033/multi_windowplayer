package com.smc.multi_windowplayer;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.danikula.videocache.HttpProxyCacheServer;


/**
 * @Author songminchao@bytedance.com
 * Please contact if you have any questions
 */
public class AudioPlayerView extends FrameLayout implements View.OnClickListener {

    enum PlayState {
        PLAYING,
        PAUSED_PLAYBACK,
        STOPPED
    }

    private final String TAG = this.getClass().getName();

    private String url;

    private final MediaPlayer mediaPlayer = new MediaPlayer();

    private ProgressUtil progressUtil;

    private ImageView imageView;

    private ObjectAnimator rotateAnimation;

    private ImageView playButton;

    private TextView progressText;

    private ImageView closeWindow;

    private PlayState playState = PlayState.STOPPED;

    public AudioPlayerView(@NonNull Context context) {
        super(context);
    }

    public AudioPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initListener();
        initView();
        initAnimate();
    }

    private void initListener() {
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            playState = PlayState.STOPPED;
            rotateAnimation.end();
            return true;
        });
        mediaPlayer.setOnCompletionListener(mp -> {
            playState = PlayState.STOPPED;
            rotateAnimation.end();
        });
    }

    private void initAnimate() {
        rotateAnimation = ObjectAnimator.ofFloat(imageView, "rotation", 0, 360);
        rotateAnimation.setRepeatCount(Integer.MAX_VALUE - 1);
        rotateAnimation.setDuration(20000);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    private void initView() {
        SeekBar seekBar = findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        seekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        progressText = findViewById(R.id.progress);

        progressUtil = ProgressUtil.bindPlayerWithProgress(mediaPlayer, seekBar, progressText);

        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(this);

        closeWindow = findViewById(R.id.close);
        closeWindow.setOnClickListener(this);

        imageView = findViewById(R.id.imageView);
        Glide.with(getContext()).load(R.mipmap.image).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

    public void setData(String url) {
        this.url = url;
    }


    public void play() {
        Log.d(TAG, "play");
        HttpProxyCacheServer proxy = Util.getAudioProxy(getContext());
        String uriStr = "";
        if (url != null) {
            uriStr = proxy.getProxyUrl(url);
        }
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(uriStr);
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnPreparedListener(mp -> {
            Log.d(TAG, "play mediaPlayer.start()");
            mediaPlayer.start();
            rotateAnimation.start();
            playButton.setImageResource(R.drawable.ic_pause);

        });
        playState = PlayState.PLAYING;
    }

    public void resume() {
        rotateAnimation.resume();
        mediaPlayer.start();
        playState = PlayState.PLAYING;
        playButton.setImageResource(R.drawable.ic_pause);

    }

    public void pause() {
        rotateAnimation.pause();
        mediaPlayer.pause();
        playState = PlayState.PAUSED_PLAYBACK;
        playButton.setImageResource(R.drawable.ic_play);
    }

    public void stop() {
        Log.d(TAG, "stop");
        mediaPlayer.stop();
        rotateAnimation.end();
        playState = PlayState.STOPPED;
        playButton.setImageResource(R.drawable.ic_play);
    }


    public void setMediaInfo(String title, String imageUrl, String artist) {
        ((TextView) findViewById(R.id.title)).setText(title + " - " + artist);
        if (imageUrl != null) {
            Glide.with(getContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).addListener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    Palette.Swatch dominantSwatch = Palette.from(Util.drawableToBitmap(resource)).generate().getDominantSwatch();
                    if (dominantSwatch != null) {
                        setBackgroundColor(dominantSwatch.getRgb());
                        ((TextView) findViewById(R.id.title)).setTextColor(dominantSwatch.getTitleTextColor());
                        playButton.setColorFilter(dominantSwatch.getBodyTextColor());
                        closeWindow.setColorFilter(dominantSwatch.getBodyTextColor());
                        progressText.setTextColor(dominantSwatch.getBodyTextColor());
                        SeekBar seekBar = findViewById(R.id.seekBar);
                        seekBar.getProgressDrawable().setColorFilter(dominantSwatch.getBodyTextColor(), PorterDuff.Mode.SRC_IN);
                        seekBar.getThumb().setColorFilter(dominantSwatch.getTitleTextColor(), PorterDuff.Mode.SRC_IN);
                    }
                    return false;
                }
            }).into(imageView);
        }
    }

    @Override
    public void destroyDrawingCache() {
        Log.d("VideoPlayerView", "VideoPlayerView destroyDrawingCache");
        super.destroyDrawingCache();
        mediaPlayer.stop();
        mediaPlayer.release();
        progressUtil.unbind();
        progressUtil = null;
        rotateAnimation.cancel();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.playButton) {
            if (playState == PlayState.PLAYING) {
                pause();
            } else if (playState == PlayState.PAUSED_PLAYBACK) {
                resume();
            } else {
                play();
            }
        } else if (id == R.id.close) {

        }
    }
}
