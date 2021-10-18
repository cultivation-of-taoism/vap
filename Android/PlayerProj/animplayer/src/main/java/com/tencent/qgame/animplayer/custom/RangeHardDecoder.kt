package com.tencent.qgame.animplayer.custom

import android.media.MediaCodec
import android.media.MediaExtractor
import com.tencent.qgame.animplayer.AnimPlayer
import com.tencent.qgame.animplayer.Constant
import com.tencent.qgame.animplayer.HardDecoder
import com.tencent.qgame.animplayer.util.ALog
import java.util.ArrayList

class RangeHardDecoder(player: AnimPlayer) : HardDecoder(player) {
    companion object {
        private const val TAG = "${Constant.TAG}.HardDecoder"
    }
    var startIdx: Int = 0
    var endIdx: Int = 0
    var mEventQueue = ArrayList<Runnable>()
    fun queueEvent(r: Runnable) {
        synchronized(this) {
            mEventQueue.add(r)
        }
    }

    fun setRange(startIdx: Int, endIdx: Int) {
        this.startIdx = startIdx;
        this.endIdx = endIdx
    }
    override fun startDecode(extractor: MediaExtractor, decoder: MediaCodec) {
        val TIMEOUT_USEC = 10000L
        var inputChunk = 0
        var outputDone = false
        var inputDone = false
        var frameIndex = 0
        var isLoop = false
        var noTask = false//Range新增
        val decoderInputBuffers = decoder.inputBuffers

        while (!outputDone) {
            if (isStopReq) {
                ALog.i(TAG, "stop decode")
                needDestroy = true//Range新增
                release(decoder, extractor)
                return
            }

            if (!inputDone) {
                val inputBufIndex = decoder.dequeueInputBuffer(TIMEOUT_USEC)
                if (inputBufIndex >= 0) {
                    val inputBuf = decoderInputBuffers[inputBufIndex]
                    val chunkSize = extractor.readSampleData(inputBuf, 0)
                    if (chunkSize < 0) {
                        decoder.queueInputBuffer(inputBufIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                        ALog.d(TAG, "decode EOS")
                    } else {
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufIndex, 0, chunkSize, presentationTimeUs, 0)
                        ALog.d(TAG, "submitted frame $inputChunk to dec, size=$chunkSize")
                        inputChunk++
                        extractor.advance()
                    }
                } else {
                    ALog.d(TAG, "input buffer not available")
                }
            }

            if (!outputDone) {
                //Range新增开始
                if (noTask && processTask()){
                    seek(extractor, decoder)
                    frameIndex = startIdx; noTask = false; inputDone = false
                } //Range新增结束
                val decoderStatus = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC)
                when {
                    decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER -> ALog.d(TAG, "no output from decoder available")
                    decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> ALog.d(TAG, "decoder output buffers changed")
                    decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        outputFormat = decoder.outputFormat
                        outputFormat?.apply {
                            try {
                                // 有可能取到空值，做一层保护
                                val stride = getInteger("stride")
                                val sliceHeight = getInteger("slice-height")
                                if (stride > 0 && sliceHeight > 0) {
                                    alignWidth = stride
                                    alignHeight = sliceHeight
                                }
                            } catch (t: Throwable) {
                                ALog.e(TAG, "$t", t)
                            }
                        }
                        ALog.i(TAG, "decoder output format changed: $outputFormat")
                    }
                    decoderStatus < 0 -> {
                        throw RuntimeException("unexpected result from decoder.dequeueOutputBuffer: $decoderStatus")
                    }
                    else -> {
                        var loop = 0
                        //Range修改开始
                        if (processTask()){
                            seekExtractor(extractor, startIdx)
                            frameIndex = startIdx
                        } else if (frameIndex >= endIdx) {//Range修改结束
                            loop = --playLoop
                            player.playLoop = playLoop // 消耗loop次数 自动恢复后能有正确的loop次数
                            outputDone = playLoop <= 0
                        }
                        val doRender = !outputDone
                        if (doRender) {
                            speedControlUtil.preRender(bufferInfo.presentationTimeUs)
                        }

                        if (needYUV && doRender) {
                            yuvProcess(decoder, decoderStatus)
                        }

                        // release & render
                        decoder.releaseOutputBuffer(decoderStatus, doRender && !needYUV)

                        if (frameIndex == startIdx && !isLoop) {//Range修改
                            onVideoStart()
                        }
                        player.pluginManager.onDecoding(frameIndex)
                        onVideoRender(frameIndex, player.configManager.config)

                        frameIndex++
                        ALog.d(TAG, "decode frameIndex=$frameIndex")
                        if (loop > 0) {
                            ALog.d(TAG, "Reached EOD, looping")
                            player.pluginManager.onLoopStart()
                            seekExtractor(extractor, startIdx)//Range修改
                            inputDone = false
                            decoder.flush()
                            speedControlUtil.reset()
                            frameIndex = startIdx//Range修改
                            isLoop = true
                        }
                        if (outputDone) {
                            //Range修改开始
                            onVideoComplete()
                            outputDone = false
                            noTask = true
                            //Range修改结束
                        }
                    }
                }
            }
        }
    }

    private fun seek(extractor: MediaExtractor, decoder: MediaCodec){
        seekExtractor(extractor, startIdx)
        decoder.flush()
        speedControlUtil.reset()
    }

    private fun processTask(): Boolean {
        val process = mEventQueue.size > 0
        if (process) mEventQueue.removeAt(0).run()
        return process
    }

    private fun seekExtractor(extractor: MediaExtractor, idx: Int) {
        val t = (idx * 1000 * 1000 / fps).toLong()
        extractor.seekTo(t, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
    }

    override fun release(decoder: MediaCodec?, extractor: MediaExtractor?) {
        renderThread.handler?.post {
            render?.clearFrame()
            try {
                ALog.i(TAG, "release")
                decoder?.apply {
                    stop()
                    release()
                }
                extractor?.release()
                glTexture?.release()
                glTexture = null
                speedControlUtil.reset()
                player.pluginManager.onRelease()
                render?.releaseTexture()
            } catch (e: Throwable) {
                ALog.e(TAG, "release e=$e", e)
            }
            isRunning = false
            //Range删除 onVideoComplete()
            if (needDestroy) {
                destroyInner()
            }
        }
    }

    override fun destroy() {
        needDestroy = true
        if (isRunning) {
            stop()
        }
        /*else {
            destroyInner()
        }Range*/
    }
}