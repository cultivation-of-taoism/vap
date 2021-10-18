package com.tencent.qgame.animplayer.custom

import com.tencent.qgame.animplayer.AnimPlayer
import com.tencent.qgame.animplayer.AudioPlayer
import com.tencent.qgame.animplayer.HardDecoder
import com.tencent.qgame.animplayer.IAnimView
import com.tencent.qgame.animplayer.util.ALog

class RangeAnimPlayer(animView: IAnimView) : AnimPlayer(animView) {
    fun setRange(sIdx:Int, eIdx:Int) {
        val rangeDecoder = decoder as? RangeHardDecoder
        rangeDecoder?.queueEvent { rangeDecoder.setRange(sIdx, eIdx) }
    }

    override fun prepareDecoder() {
        if (decoder == null) {
            decoder = RangeHardDecoder(this).apply {//Range 修改
                playLoop = this@RangeAnimPlayer.playLoop
                fps = this@RangeAnimPlayer.fps
            }
        }
        if (audioPlayer == null) {
            audioPlayer = AudioPlayer(this).apply {
                playLoop = this@RangeAnimPlayer.playLoop
            }
        }
    }

}