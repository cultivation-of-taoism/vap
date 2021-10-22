package com.tencent.qgame.animplayer.custom

import com.tencent.qgame.animplayer.AnimPlayer
import com.tencent.qgame.animplayer.AudioPlayer
import com.tencent.qgame.animplayer.IAnimView

class RangeAnimPlayer(animView: IAnimView) : AnimPlayer(animView) {
    var startIndex = 0
    var endIndex = 0
    fun setRange(sIdx:Int, eIdx:Int) {
        startIndex = sIdx; endIndex = eIdx
        val rangeDecoder = decoder as? RangeHardDecoder
        rangeDecoder?.queueEvent { rangeDecoder.setRange(sIdx, eIdx) }
    }

    override fun prepareDecoder() {
        if (decoder == null) {
            decoder = RangeHardDecoder(this).apply {//Range 修改
                playLoop = this@RangeAnimPlayer.playLoop
                fps = this@RangeAnimPlayer.fps
                startIdx = startIndex
                endIdx = endIndex
            }
        }
        if (audioPlayer == null) {
            audioPlayer = AudioPlayer(this).apply {
                playLoop = this@RangeAnimPlayer.playLoop
            }
        }
    }

}