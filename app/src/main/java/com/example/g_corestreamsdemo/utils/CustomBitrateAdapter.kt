package com.example.g_corestreamsdemo.utils

import android.net.TrafficStats
import android.util.Log
import com.pedro.rtplibrary.util.BitrateAdapter

class CustomBitrateAdapter(private val listener: Listener) : BitrateAdapter(listener) {

    private var maxBitrate: Int = 0
    private var count: Int = 0

    override fun setMaxBitrate(bitrate: Int) {
        maxBitrate = bitrate
    }

    private val minBitrateIncreasePeriod = 5    // increase the bitrate every 5th iteration of the adaptBitrate() method
    private val maxBitrateIncreasePeriod = 60   // increase the bitrate every 60th iteration of the adaptBitrate() method
    private var bitrateIncreasePeriod = minBitrateIncreasePeriod    // how often do we try to increase the bitrate

    override fun adaptBitrate(actualBitrate: Long) {
        Log.d(TAG, "period: $bitrateIncreasePeriod")
        Log.d(TAG, "actualBitrate: ${actualBitrate / 1024} Kb/s")

        val actualUploadSpeed = calculateUploadSpeed()
        Log.e(TAG, "uploadSpeed: ${actualUploadSpeed / 1024} Kb/s")

        if (actualUploadSpeed <= actualBitrate) {
            decreaseBitrate(actualUploadSpeed)
            count = 0
        } else if (count >= bitrateIncreasePeriod) {
            increaseBitrate(actualUploadSpeed)
            count = 0
        }

        count ++
    }

    private val uId = android.os.Process.myUid()
    private var prevTxBytes = TrafficStats.getUidTxBytes(uId)
    private var timeStamp = System.currentTimeMillis()
    /**
     * Calculates the actual upload speed in bit/s
     */
    private fun calculateUploadSpeed(): Float {
        val currentTxBytes = TrafficStats.getUidTxBytes(uId)
        val uploadBits = (currentTxBytes - prevTxBytes) * 8
        val timeDiff = System.currentTimeMillis() - timeStamp

        prevTxBytes = currentTxBytes
        timeStamp = System.currentTimeMillis()

        return uploadBits / (timeDiff / 1000f)
    }

    // Focusing on this flag, we determine how often we will try to increase the bitrate
    private var isBadNetwork = false
    /**
     * Decreases the bitrate by 10% of the actual upload speed
     */
    private fun decreaseBitrate(actualUploadSpeed: Float) {
        listener.onBitrateAdapted((actualUploadSpeed * 0.9).toInt())
        isBadNetwork = true

        // We increase the bitrate increase period, because not enough network bandwidth
        bitrateIncreasePeriod *= 2
        if (bitrateIncreasePeriod > maxBitrateIncreasePeriod) {
            bitrateIncreasePeriod = maxBitrateIncreasePeriod
        }
    }

    private fun increaseBitrate(actualUploadSpeed: Float) {
        if (!isBadNetwork) {
            // We lower the bitrate increase period, because sufficient network bandwidth
            bitrateIncreasePeriod /= 2
            if (bitrateIncreasePeriod < minBitrateIncreasePeriod){
                bitrateIncreasePeriod = minBitrateIncreasePeriod
            }
        }

        val adaptedBitrate = if (actualUploadSpeed > maxBitrate) {
            maxBitrate
        } else {
            // Than closer the upload speed is to the maximum bitrate,
            // the lower the value we increase it
            (actualUploadSpeed + (maxBitrate - actualUploadSpeed) * 0.15).toInt()
        }
        listener.onBitrateAdapted(adaptedBitrate)
        isBadNetwork = false
    }

    companion object {
        private val TAG = CustomBitrateAdapter::class.simpleName
    }

}