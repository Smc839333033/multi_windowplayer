package com.smc.multi_windowplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.danikula.videocache.HttpProxyCacheServer;

import java.util.List;

/**
 * @Author songminchao@bytedance.com
 * @Date 2022/9/9 18:17
 * Please contact if you have any questions
 */
public class Util {

    private static HttpProxyCacheServer audioProxy;

    public static HttpProxyCacheServer getAudioProxy(Context context) {
        if (audioProxy == null) {
            audioProxy = new HttpProxyCacheServer.Builder(context)
                    .maxCacheSize(1024 * 1024 * 1024) // 缓存大小
                    .fileNameGenerator(url -> {
                        Uri uri = Uri.parse(url);
                        List<String> pathSegList = uri.getPathSegments();
                        String path;
                        if (pathSegList != null && pathSegList.size() > 0) {
                            path = pathSegList.get(pathSegList.size() - 1);
                        } else {
                            path = url;
                        }
                        return path;
                    })
                    .build();
        }
        return audioProxy;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static String changeToTime(long timestamp) {
        timestamp = (timestamp < 60 * 60 * 24 * 1000) && timestamp >= 0 ? timestamp : 0;
        String s;
        long second = timestamp / 1000;
        long hours = second / 3600;
        s = hours + ":";
        long minutes = (second - (hours * 3600)) / 60;
        if (minutes < 10) {
            s += "0" + minutes + ":";
        } else {
            s += minutes + ":";
        }
        long seconds = (second - (hours * 3600) - minutes * 60);
        if (seconds < 10) {
            s += "0" + seconds;
        } else {
            s += seconds;
        }
        return s;
    }
}
