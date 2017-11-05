package com.x0.gadge_tf.suicareader

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.SparseArray
import com.x0.gadge_tf.suicareader.database.StationDatabaseHelper

/**
 * Created by gadget-tf on 2015/09/23.
 */
class SuicaHistory(private val mContext: Context) {

    private var termId: Int = 0
    private var procId: Int = 0
    var year: Int = 0
        private set
    var month: Int = 0
        private set
    var day: Int = 0
        private set
    private var kind: String? = null
    var remain: Int = 0
        private set
    private var seqNo: Int = 0
    private var reasion: Int = 0
    private var inLineCode: Int = 0
    private var outLineCode: Int = 0
    private var inStationCode: Int = 0
    private var outStationCode: Int = 0
    var inLineName: String? = null
        private set
    var outLineName: String? = null
        private set
    var inStationName: String? = null
        private set
    var outStationName: String? = null
        private set

    val term: String
        get() = TERM_MAP.get(termId)

    val proc: String
        get() = PROC_MAP.get(procId)

    fun parse(res: ByteArray, offset: Int) {
        init(res, offset)
    }

    private fun init(res: ByteArray, offset: Int) {
        this.termId = res[offset].toInt()
        this.procId = res[offset + 1].toInt()

        val mixInt = toInt(res, offset, 4, 5)
        this.year = mixInt shr 9 and 0x07f
        this.month = mixInt shr 5 and 0x00f
        this.day = mixInt and 0x01f

        if (isBuppan(this.procId)) {
            this.kind = "物販"
        } else if (isBus(this.procId)) {
            this.kind = "バス"
        } else {
            this.kind = if (res[offset + 6] < 0x80) "JR" else "公営/私鉄"
        }
        this.inLineCode = res[offset + 6].toInt()
        this.inStationCode = res[offset + 7].toInt()
        this.outLineCode = res[offset + 8].toInt()
        this.outStationCode = res[offset + 9].toInt()
        this.remain = toInt(res, offset, 11, 10)
        this.seqNo = toInt(res, offset, 12, 13, 14)
        this.reasion = res[offset + 15].toInt()

        val helper = StationDatabaseHelper(mContext)
        val database = helper.getWritableDatabase()
        var cursor = database.query("StationCode", arrayOf("LineName", "StationName"),
                "LineCode = " + inLineCode.toString() + " AND StationCode = " + inStationCode.toString(), null, null, null, null)
        cursor.moveToFirst()
        if (cursor.getCount() > 0) {
            inLineName = cursor.getString(cursor.getColumnIndex("LineName"))
            inStationName = cursor.getString(cursor.getColumnIndex("StationName"))
        }
        cursor.close()

        cursor = database.query("StationCode", arrayOf("LineName", "StationName"),
                "LineCode = " + outLineCode.toString() + " AND StationCode = " + outStationCode.toString(), null, null, null, null)
        cursor.moveToFirst()
        if (cursor.getCount() > 0) {
            outLineName = cursor.getString(cursor.getColumnIndex("LineName"))
            outStationName = cursor.getString(cursor.getColumnIndex("StationName"))
        }
        cursor.close()
        database.close()

        return
    }

    private fun toInt(res: ByteArray, offset: Int, vararg idx: Int): Int {
        var num = 0
        for (i in idx.indices) {
            num = num shl 8
            num += res[offset + idx[i]].toInt() and 0xff
        }
        return num
    }

    private fun isBuppan(procId: Int): Boolean {
        return if (procId == 70 || procId == 73 || procId == 74 || procId == 75 || procId == 198 || procId == 203) {
            true
        } else {
            false
        }
    }

    private fun isBus(procId: Int): Boolean {
        return if (procId == 13 || procId == 15 || procId == 31 || procId == 35) {
            true
        } else {
            false
        }
    }

    companion object {
        private val TERM_MAP = SparseArray<String>()
        private val PROC_MAP = SparseArray<String>()

        init {
            TERM_MAP.put(3, "精算機")
            TERM_MAP.put(4, "携帯型端末")
            TERM_MAP.put(5, "車載端末")
            TERM_MAP.put(7, "券売機")
            TERM_MAP.put(8, "券売機")
            TERM_MAP.put(9, "入金機")
            TERM_MAP.put(18, "券売機")
            TERM_MAP.put(20, "券売機等")
            TERM_MAP.put(21, "券売機等")
            TERM_MAP.put(22, "改札機")
            TERM_MAP.put(23, "簡易改札機")
            TERM_MAP.put(24, "窓口端末")
            TERM_MAP.put(25, "窓口端末")
            TERM_MAP.put(26, "改札端末")
            TERM_MAP.put(27, "携帯電話")
            TERM_MAP.put(28, "乗継精算機")
            TERM_MAP.put(29, "連絡改札機")
            TERM_MAP.put(31, "簡易入金機")
            TERM_MAP.put(70, "VIEW ALTTE")
            TERM_MAP.put(72, "VIEW ALTTE")
            TERM_MAP.put(199, "物販端末")
            TERM_MAP.put(200, "自販機")

            PROC_MAP.put(1, "運賃支払(改札出場)")
            PROC_MAP.put(2, "チャージ")
            PROC_MAP.put(3, "券購(磁気券購入)")
            PROC_MAP.put(4, "精算")
            PROC_MAP.put(5, "精算 (入場精算)")
            PROC_MAP.put(6, "窓出 (改札窓口処理)")
            PROC_MAP.put(7, "新規 (新規発行)")
            PROC_MAP.put(8, "控除 (窓口控除)")
            PROC_MAP.put(13, "バス (PiTaPa系)")
            PROC_MAP.put(15, "バス (IruCa系)")
            PROC_MAP.put(17, "再発 (再発行処理)")
            PROC_MAP.put(19, "支払 (新幹線利用)")
            PROC_MAP.put(20, "入A (入場時オートチャージ)")
            PROC_MAP.put(21, "出A (出場時オートチャージ)")
            PROC_MAP.put(31, "入金 (バスチャージ)")
            PROC_MAP.put(35, "券購 (バス路面電車企画券購入)")
            PROC_MAP.put(70, "物販")
            PROC_MAP.put(72, "特典 (特典チャージ)")
            PROC_MAP.put(73, "入金 (レジ入金)")
            PROC_MAP.put(74, "物販取消")
            PROC_MAP.put(75, "入物 (入場物販)")
            PROC_MAP.put(198, "物現 (現金併用物販)")
            PROC_MAP.put(203, "入物 (入場現金併用物販)")
            PROC_MAP.put(132, "精算 (他社精算)")
            PROC_MAP.put(133, "精算 (他社入場精算)")
        }
    }
}
