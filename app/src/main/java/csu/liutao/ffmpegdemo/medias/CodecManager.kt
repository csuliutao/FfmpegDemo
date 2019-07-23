package csu.liutao.ffmpegdemo.medias

import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface

class CodecManager(val format: MediaFormat, val callback : MediaCodec.Callback, val surface: Surface? = null, val codeFlag : Int = MediaCodec.CONFIGURE_FLAG_ENCODE){
    private var codec : MediaCodec? = null

    fun start() {
        val type = format.getString(MediaFormat.KEY_MIME)
        if (codeFlag == 0) {
            codec = MediaCodec.createDecoderByType(type)
        } else {
            codec = MediaCodec.createEncoderByType(type)
        }
        codec!!.configure(format, surface, null, codeFlag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            codec!!.setCallback(callback, getHandler(type))
        } else {
            codec!!.setCallback(callback)
        }
        codec!!.start()
    }

    fun getOutputFormat() : MediaFormat{
        return codec!!.outputFormat
    }

    fun stop() {
        codec?.stop()
    }

    fun release() {
        codec?.release()
        codec = null
    }

    private fun getHandler(type : String) : Handler {
        return if (type.startsWith("audio")) Handler(aacThread.looper) else Handler(avcThread.looper)
    }

    companion object {
        private val aacThread = HandlerThread("aacThread")
        private val avcThread = HandlerThread("avcThread")

        init {
            aacThread.start()
            avcThread.start()
        }

        fun releaseThread() {
            aacThread.quitSafely()
            avcThread.quitSafely()
        }
    }
}