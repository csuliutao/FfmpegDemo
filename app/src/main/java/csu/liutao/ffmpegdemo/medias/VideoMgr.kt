package csu.liutao.ffmpegdemo.medias

import android.content.Context
import android.media.Image
import android.media.MediaCodecInfo
import android.media.MediaFormat
import csu.liutao.ffmpegdemo.Utils
import java.io.File
import java.lang.Math.min


class VideoMgr private constructor(){
    private val PATH = "medias"
    private val END_TAG = ".mp4"

    private lateinit var mediaDir :String

    fun initDir(context : Context) {
        mediaDir = context.externalCacheDir.canonicalPath + File.separator + PATH
        val dir = File(mediaDir)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun getAllFiles () : List<File> {
        val dir = File(mediaDir)
        val files = dir.listFiles()
        return files.asList()
    }

    fun getNewFile(): File{
        return Utils.getNewFile(mediaDir, END_TAG)
    }

    fun getH264CodecFromat(width : Int, height: Int) : MediaFormat {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height)
        format.setInteger(MediaFormat.KEY_BIT_RATE,width * height * 3)
        format.setInteger(MediaFormat.KEY_FRAME_RATE, KEY_FRAME_RATE)
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, KEY_COLOR_FORMAT)
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_I_FRAME_INTERVAL)
        format.setInteger(MediaFormat.KEY_ROTATION, KEY_ROTATION)
        return format
    }

    fun imageToNV21(image : Image) : ByteArray{
        val width = image.width
        val height = image.height
        Utils.log("width ="+ width+",height = "+ height)
        val uvSize = width * height / 4
        val ySize = width * height
        val size = ySize + uvSize * 2
        val nv21 = ByteArray(size)
        var curPos = 0

        val yplane = image.planes[0]
        val uplane = image.planes[1]
        val vplane = image.planes[2]

        val yremaing = yplane.buffer.remaining()
        if (ySize == yremaing) {
            curPos = ySize
            yplane.buffer.get(nv21, 0, curPos)

        } else {
            val yStide = yplane.rowStride
            var yPos = 0
            for (row in 0 until height) {
                val curWidth = min(width, yremaing - yPos)
                if (curWidth <= 0) break
                yplane.buffer.position(yPos)
                yplane.buffer.get(nv21, curPos, curWidth)
                yPos += yStide
                curPos += curWidth
            }

            Utils.log("y real size ="+ ySize + ", size =" + curPos)
        }

        if (uplane.pixelStride == 1) {
            vplane.buffer.get(nv21, curPos, uvSize)
            curPos += uvSize

            uplane.buffer.get(nv21, curPos, uvSize)
            curPos += uvSize
        } else {
            val uvRemaing = vplane.buffer.remaining()
            val uvStride = vplane.rowStride

            var uvPos = 0
            for (row in 0 .. height / 2) {
                val curWidth = min(width, uvRemaing - uvPos)
                if (curWidth <= 0) break
                vplane.buffer.position(uvPos)
                vplane.buffer.get(nv21, curPos , curWidth)
                uvPos += uvStride
                curPos += curWidth
            }
        }


        Utils.log("real size ="+ size +", curPos = "+ curPos)

        return nv21
    }


    companion object{
        val instance = VideoMgr()
        val KEY_FRAME_RATE = 30
        val KEY_COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        val KEY_ROTATION = 90
        val KEY_I_FRAME_INTERVAL = 2

        /*val sps = byteArrayOf(0, 0, 0, 1, 103, 100, 0, 40, -84, 52, -59, 1, -32, 17, 31, 120, 11, 80, 16, 16, 31, 0, 0, 3, 3, -23, 0, 0, -22, 96, -108)
        val pps = byteArrayOf(0, 0, 0, 1, 104, -18, 60, -128)*/
    }
}