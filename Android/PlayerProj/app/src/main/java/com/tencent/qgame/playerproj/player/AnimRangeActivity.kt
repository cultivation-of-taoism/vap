package com.tencent.qgame.playerproj.player

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.tencent.qgame.playerproj.R
import kotlinx.android.synthetic.main.activity_anim_range.*
import java.io.File

class AnimRangeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anim_range)
        initRangeAnimView()
    }

    private fun initRangeAnimView(){
        range_anim.setFps(15)
        range_anim.setPlayerRange(0, 120)
        range_anim.setAnimListener(AnimListener)
        range_anim.setLoop(9999)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DOWNLOADS), "appmhpz_vap.mp4")
        range_anim.startPlay(file)
    }
}