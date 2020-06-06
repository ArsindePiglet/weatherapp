package com.arsinde.weatherapp.features.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.arsinde.weatherapp.R

class TranslateFragment : Fragment() {

    companion object {
        fun newInstance() = TranslateFragment()
    }

    private val vm: TranslateViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.translate_fragment, container, false)
}