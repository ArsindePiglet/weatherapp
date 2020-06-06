package com.arsinde.weatherapp.features.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.adapters.SettingsProcessAdapter
import kotlinx.android.synthetic.main.settings_fragment.*

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance() = SettingsFragment()
    }

    private val viewModel: SettingsViewModel by viewModels()
    private val viewLayoutManager by lazy { LinearLayoutManager(context) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.settings_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        btnDiscover.setOnClickListener {
            viewModel.getSystemServices()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        viewModel.processesList.observe(viewLifecycleOwner, Observer {
            val processAdapter = SettingsProcessAdapter(it)
            rvSettings.apply {
                layoutManager = viewLayoutManager
                adapter = processAdapter
            }
        })
    }
}