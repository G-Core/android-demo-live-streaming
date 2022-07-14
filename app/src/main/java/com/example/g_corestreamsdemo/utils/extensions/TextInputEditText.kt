package com.example.g_corestreamsdemo.utils.extensions

import android.text.Editable
import android.text.TextWatcher
import com.google.android.material.textfield.TextInputEditText

interface TextWatcherAfterTextCallback {
    fun afterTextChanged(editable: Editable?)
}

fun TextInputEditText.afterTextChangedListener(callback: TextWatcherAfterTextCallback) {
    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            callback.afterTextChanged(editable)
        }
    })
}