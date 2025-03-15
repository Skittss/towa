package org.skitts.towa

import android.content.Context
import android.media.MediaPlayer
import android.widget.ImageView
import android.widget.LinearLayout


class DictateButtonLayout (
    context: Context
) : LinearLayout(context) {
    init {
        inflate(context, R.layout.dictate_icon_image_layout, this)
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

    }

    fun setup(width: Int, height: Int, color: Int, audioPath: String) {
        val icon = findViewById<ImageView>(R.id.dictate_icon)
        icon.setColorFilter(color)

        icon.layoutParams.width = width
        icon.layoutParams.height = height

        setOnClickListener {
            playMp3(audioPath)
        }
    }

    private fun playMp3(assetFilePath: String) {
        try {
            val mediaPlayer = MediaPlayer()

            val descriptor = context.assets.openFd(assetFilePath)
            mediaPlayer.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length
            )
            descriptor.close()

            mediaPlayer.prepare()
            mediaPlayer.setVolume(1f, 1f)
            mediaPlayer.isLooping = false
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}