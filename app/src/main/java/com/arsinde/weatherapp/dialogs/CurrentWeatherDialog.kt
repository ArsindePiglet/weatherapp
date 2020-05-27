package com.arsinde.weatherapp.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.arsinde.weatherapp.R

class CurrentWeatherDialog(private val msg: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(msg)
            .setPositiveButton(getString(R.string.dialog_ok)) { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }
}