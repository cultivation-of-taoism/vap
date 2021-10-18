package com.tencent.qgame.animplayer.custom

import android.content.Context
import android.util.AttributeSet
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener

class RangeAnimView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AnimView(context, attrs, defStyleAttr) {
    override val player: RangeAnimPlayer = RangeAnimPlayer(this)
    override val animProxyListener by lazy {
        object : IAnimListener {

            override fun onVideoConfigReady(config: AnimConfig): Boolean {
                scaleTypeUtil.videoWidth = config.width
                scaleTypeUtil.videoHeight = config.height
                return animListener?.onVideoConfigReady(config) ?: super.onVideoConfigReady(config)
            }

            override fun onVideoStart() {
                animListener?.onVideoStart()
            }

            override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
                animListener?.onVideoRender(frameIndex, config)
            }

            override fun onVideoComplete() {
                //Range hide()
                animListener?.onVideoComplete()
            }

            override fun onVideoDestroy() {
                hide()
                animListener?.onVideoDestroy()
            }

            override fun onFailed(errorType: Int, errorMsg: String?) {
                animListener?.onFailed(errorType, errorMsg)
            }

        }
    }

    init {
        player.animListener = animProxyListener
    }

    fun setPlayerRange(startIdx:Int, endIdx:Int) {
        player.setRange(startIdx,endIdx)
    }
}