package com.smc.multi_windowplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private var bisectedSizeLayout: BisectedSizeLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)
        bisectedSizeLayout = findViewById(R.id.bisectedSizeLayout)
    }

    @SuppressLint("InflateParams")
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (bisectedSizeLayout!!.childCount < 6) {
                    val view: AudioPlayerView = LayoutInflater.from(this).inflate(R.layout.audio_player_layout, null) as AudioPlayerView
                    view.setData("http://m701.music.126.net/20220915150159/2411b9bc32b8de2999564d12aa0e3d1b/jdyyaac/obj/w5rDlsOJwrLDjj7CmsOj/16885444394/59ec/c999/8a85/e04e6476576cdf54b851fb471235389e.m4a")
                    view.setMediaInfo(
                        "是否",
                        "http://p4.music.126.net/Q_K62E7UHrrHZYUgjMi2vw==/109951167754013928.jpg",
                        "程响"
                    )
                    view.play()
                    bisectedSizeLayout!!.addView(view)
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (bisectedSizeLayout!!.childCount > 0) {
                    bisectedSizeLayout!!.removeViewAt(bisectedSizeLayout!!.childCount - 1)
                }
                return true
            }
            else -> {}
        }
        return super.onKeyDown(keyCode, event)
    }
}