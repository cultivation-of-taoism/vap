package com.tencent.qgame.playerproj.player

import android.util.Log
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.custom.RangeAnimView
import com.tencent.qgame.animplayer.inter.IAnimListener

object AnimListener : IAnimListener{
    val TAG = "AnimPlayer.AnimListener"
    override fun onVideoConfigReady(config: AnimConfig): Boolean {
        return super.onVideoConfigReady(config)
    }
    override fun onVideoStart() {
        Log.i(TAG, "onVideoStart")
    }

    override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {

    }

    override fun onVideoComplete() {
        Log.i(TAG, "onVideoComplete")
    }

    override fun onVideoDestroy() {
        Log.i(TAG, "onVideoDestroy")
    }

    override fun onFailed(errorType: Int, errorMsg: String?) {
        Log.i(TAG, "onFailed errorType=$errorType errorMsg=$errorMsg")
    }
}