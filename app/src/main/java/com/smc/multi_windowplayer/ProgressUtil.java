package com.smc.multi_windowplayer;


import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author songminchao@bytedance.com
 * @Date 2022/9/8 16:01
 * Please contact if you have any questions
 */
public class ProgressUtil {

    private static final int MUSIC_MESSAGE = 1;

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(6);

    private ScheduledFuture<?> schedule;

    private final MediaPlayer mediaPlayer;

    private final SeekBar seekBar;

    private final TextView progressText;


    private ProgressHandler handler = null;


    private static class ProgressHandler extends Handler {

        private final WeakReference<ProgressUtil> weakReference;

        public ProgressHandler(ProgressUtil progressUtil) {
            this.weakReference = new WeakReference<>(progressUtil);
        }

        @Override
        public void handleMessage(Message msg) {
            ProgressUtil progressUtil = weakReference.get();
            super.handleMessage(msg);
            if (msg.what == MUSIC_MESSAGE) {
                if (null != progressUtil) {
                    int duration = 0, currentPosition = 0;
                    try {
                        duration = progressUtil.mediaPlayer.getDuration();
                        currentPosition = progressUtil.mediaPlayer.getCurrentPosition();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (duration >= 0 && progressUtil.seekBar.getMax() != duration) {
                        progressUtil.seekBar.setMax(duration);
                    }
                    progressUtil.seekBar.setProgress(currentPosition);
                    progressUtil.progressText.setText(String.format("%s/%s", Util.changeToTime(currentPosition), Util.changeToTime(duration)));
                }
            }
        }
    }

    private ProgressUtil(MediaPlayer mediaPlayer, SeekBar seekBar, TextView progressText) {
        this.mediaPlayer = mediaPlayer;
        this.seekBar = seekBar;
        this.progressText = progressText;
    }

    public static ProgressUtil bindPlayerWithProgress(MediaPlayer mediaPlayer, SeekBar seekBar, TextView progressText) {
        ProgressUtil progressUtil = new ProgressUtil(mediaPlayer, seekBar, progressText);
        progressUtil.handler = new ProgressHandler(progressUtil);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
        progressUtil.schedule = scheduledExecutorService.scheduleAtFixedRate(() -> progressUtil.handler.sendEmptyMessage(MUSIC_MESSAGE), 0, 1, TimeUnit.SECONDS);
        return progressUtil;
    }

    public void unbind() {
        handler = null;
        if (schedule != null) {
            schedule.cancel(true);
        }
        schedule = null;
    }
}
