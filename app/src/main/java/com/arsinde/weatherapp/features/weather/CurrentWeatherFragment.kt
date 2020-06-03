package com.arsinde.weatherapp.features.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.arsinde.weatherapp.R
import com.arsinde.weatherapp.common.myCustomTextWatcher
import com.arsinde.weatherapp.dialogs.CurrentWeatherDialog
import kotlinx.android.synthetic.main.current_weather_fragment.*

class CurrentWeatherFragment : Fragment() {

    companion object {
        private val TAG: String = CurrentWeatherFragment::javaClass.name
        fun newInstance() =
            CurrentWeatherFragment()
    }

    private val viewModel: CurrentWeatherViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.current_weather_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etLocation.myCustomTextWatcher()
        btnShowDialog.setOnClickListener {
            etLocation.text?.let {
                viewModel.fetchWeather(it.toString())
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        viewModel.currentWeather.observe(viewLifecycleOwner, Observer {
            val dialog = CurrentWeatherDialog(it)
            dialog.show(childFragmentManager,
                TAG
            )
            tvError.visibility = View.GONE
        })
        viewModel.errorMsg.observe(viewLifecycleOwner, Observer {
            tvError.text = it
            tvError.visibility = View.VISIBLE
        })
    }
}
