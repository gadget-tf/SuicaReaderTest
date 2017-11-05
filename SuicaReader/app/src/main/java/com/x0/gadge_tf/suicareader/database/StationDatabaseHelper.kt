package com.x0.gadge_tf.suicareader.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by gadget-tf on 2017/11/05.
 */
val DB_NAME = "StationCode.db"
val DB_VERSION = 1

class StationDatabaseHelper(context: Context): SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    private var mDatabasePath: String = context.filesDir.parent + "/databases/"
    private var mContext: Context? = null

    init {
        mContext = context
        if (!dbExists()) {
            copyFile()
        }
    }

    override fun onCreate(p0: SQLiteDatabase?) {
    }

    override fun onUpgrade(p0: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (!dbExists()) {
            copyFile()
        }
    }

    private fun dbExists(): Boolean {
        val file = File(mDatabasePath, DB_NAME)
        return file.exists()
    }

    private fun copyFile() {
        val copyDir = File(mDatabasePath)
        if (!copyDir.exists()) {
            copyDir.mkdirs()
        }

        var input: InputStream? = null
        var out: FileOutputStream? = null

        try {
            var buffer = ByteArray(1024, { 0 })
            var size = 0
            input = mContext!!.assets.open(DB_NAME) as InputStream
            out = FileOutputStream(mDatabasePath + DB_NAME)
            while (true) {
                size = input.read(buffer)
                if (size < 0) {
                    break
                }
                out.write(buffer)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            input?.close()
            out?.close()
        }
    }
}