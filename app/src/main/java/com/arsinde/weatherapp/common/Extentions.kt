package com.arsinde.weatherapp.common

import android.widget.EditText
import java.util.regex.Pattern

fun EditText.myCustomTextWatcher() {
    this.addTextChangedListener(object : AbstractTextWatcher(this) {
        override fun validate(editText: EditText, text: String) {
            editText.removeTextChangedListener(this)
            val str = text.filter {
                it.isValid()
            }
            editText.setText(str)
            editText.setSelection(str.length)
            editText.addTextChangedListener(this)
        }
    })
}
private val pattern = Pattern.compile("[0-9a-zA-Z,.-]")
fun Char.isValid(): Boolean = pattern.matcher(this.toString()).matches()

private val locationPattern = Pattern.compile("^-?[0-9]{1,3}.[0-9]*,-?[0-9]{1,3}.[0-9]*$")
fun String.isLocationValid() = locationPattern.matcher(this).matches()

private val cityPattern = Pattern.compile("^[a-zA-Z]*$")
fun String.isCityValid() = cityPattern.matcher(this).matches()