package com.exa.android.loctrace.Fragments.Employee

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.exa.android.loctrace.Fragments.Employee.Location.LocationService
import com.exa.android.loctrace.Fragments.Employee.Location.hasLocationPermission
import com.exa.android.loctrace.Helper.PreferencesHelper
import com.exa.android.loctrace.databinding.FragmentEmployesBinding

class EmployesFragment : Fragment() {

    private var _binding: FragmentEmployesBinding? = null
    private val binding get() = _binding!!

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            Toast.makeText(
                requireContext(),
                "Permissions granted. You can start tracking now.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissions are required to start tracking",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEmployesBinding.inflate(inflater, container, false)

        // Set switch state based on SharedPreferences
        val isLocationServiceRunning = PreferencesHelper.isLocationServiceRunning(requireContext())
        binding.locationSwitch.isChecked = isLocationServiceRunning

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            PreferencesHelper.setLocationServiceRunning(requireContext(), isChecked)
            if (context?.hasLocationPermission() == true) {
                if (isChecked) {
                    startLocationService()
                } else {
                    stopLocationService()
                }
            } else {
                requestPermission()
                binding.locationSwitch.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "Location permissions are required to start tracking",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    fun requestPermission() {
        permissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun startLocationService() {
        Intent(requireContext(), LocationService::class.java).apply {
            action = LocationService.ACTION_START
            ContextCompat.startForegroundService(requireContext(), this)
        }
    }

    fun stopLocationService() {
        Intent(requireContext(), LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
            ContextCompat.startForegroundService(requireContext(), this)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
