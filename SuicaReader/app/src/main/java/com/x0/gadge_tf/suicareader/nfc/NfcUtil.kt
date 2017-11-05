package com.x0.gadge_tf.suicareader.nfc

import android.content.Context
import com.x0.gadge_tf.suicareader.SuicaHistory
import java.io.ByteArrayOutputStream

/**
 * Created by gadget-tf on 2017/11/05.
 */
class NfcUtil() {
    var mStationList = ArrayList<SuicaHistory>()

    fun createMessage(idm: ByteArray, size: Int): ByteArray {
        var out = ByteArrayOutputStream()

        out.write(0)
        out.write(0x06)
        out.write(idm)
        out.write(1)
        out.write(0x0f)
        out.write(0x09)
        out.write(size)
        for (i in 0..(size - 1)) {
            out.write(0x80)
            out.write(i)
        }

        var msg = out.toByteArray()
        msg[0] = msg.size.toByte()
        return msg
    }

    fun parse(context: Context, response: ByteArray) {
        val res: Int = response[10].toInt()
        if (res != 0x00) {
            throw RuntimeException()
        }

        val size = response[12].toInt()
        for (i in 0..(size - 1)) {
            val suicaHistory = SuicaHistory(context)
            suicaHistory.parse(response, 13 + i * 16)
            mStationList.add(suicaHistory)
        }
    }

    fun getList(): ArrayList<SuicaHistory> {
        return mStationList
    }
}