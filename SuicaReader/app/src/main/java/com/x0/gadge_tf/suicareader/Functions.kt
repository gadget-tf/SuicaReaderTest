package com.x0.gadge_tf.suicareader

import android.content.Context
import android.widget.Toast

/**
 * Created by gadget-tf on 2017/11/05.
 */
fun toast(context: Context, str: String) {
    Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
}