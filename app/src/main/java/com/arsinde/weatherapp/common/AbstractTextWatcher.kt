package com.arsinde.weatherapp.common

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class AbstractTextWatcher(private val editText: EditText): TextWatcher {
    override fun afterTextChanged(editable: Editable) {
        validate(editText, editText.text.toString())
    }
    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
        println("i: $i; i1: $i1; i2: $i2")
    }
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
    abstract fun validate(editText: EditText, text: String)
}