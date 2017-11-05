package com.x0.gadge_tf.suicareader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcF
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.x0.gadge_tf.suicareader.nfc.NfcUtil

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onResume() {
        super.onResume()

        try {
            // FeliCa IDm の取得
            val felicaIDm = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
            // FeliCaタグの取得
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

            val nfcF: NfcF = NfcF.get(tag)

            // FeliCa PMmの取得
            val pmm = nfcF.manufacturer
            // SystemCodeの取得
            val systemCode = nfcF.systemCode

            var builder: StringBuilder = StringBuilder()
            for (code in systemCode) {
                builder.append(String.format("%02X", code))
            }
            if (!builder.toString().equals("0003")) {
                toast(this@MainActivity, getString(R.string.system_code_error))
                return
            }

            if (!nfcF.isConnected) {
                nfcF.connect()
            }

            val nfcUtil = NfcUtil()
            val requestMsg = nfcUtil.createMessage(felicaIDm, 10)
            val response = nfcF.transceive(requestMsg)

            nfcF.close()

            nfcUtil.parse(this@MainActivity, response)

            val list = nfcUtil.getList()
            if (list.size > 0) {
                var sb = StringBuilder()
                sb.append(list.get(0).year);
                sb.append("年");
                sb.append(list.get(0).month);
                sb.append("月");
                sb.append("\n");
                sb.append("使用場所：" + list.get(0).term);
                sb.append("\n");
                sb.append("残高：" + list.get(0).remain);
                sb.append("\n");
                sb.append("入場駅：" + list.get(0).inLineName + " " + list.get(0).inStationName);
                sb.append("\n");
                sb.append("退場駅：" + list.get(0).outLineName + " " + list.get(0).outStationName);
                textView.setText(sb.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
