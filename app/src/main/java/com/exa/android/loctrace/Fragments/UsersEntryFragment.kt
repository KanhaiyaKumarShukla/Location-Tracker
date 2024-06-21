package com.exa.android.loctrace.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.exa.android.loctrace.Helper.AppConstants
import com.exa.android.loctrace.Helper.AppConstants.userId
import com.exa.android.loctrace.R
import com.exa.android.loctrace.databinding.FragmentUsersEntryBinding

class UsersEntryFragment : Fragment() {

    private var _binding:FragmentUsersEntryBinding?=null
    private  val binding get()=_binding!!
    lateinit var sharedPref: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding=FragmentUsersEntryBinding.inflate(inflater, container, false)
         sharedPref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedRole = sharedPref.getString("user_role", null)

        if (selectedRole == null) {
            // First time user or role not set, show role selection dialog or activity
            showRoleSelectionDialog()
        } else {
            // Role is already selected, navigate directly
            navigateToRoleFragment(selectedRole)
        }
    }

    private fun showRoleSelectionDialog() {
        // Implement your role selection dialog or activity here
        // Store selected role in shared preferences upon selection
        // Example:
        // sharedPref.edit().putString("user_role", "employee").apply()
        binding.adminButton.setOnClickListener{
            sharedPref.edit().putString("user_role", "admin").apply()
             findNavController().navigate(R.id.action_usersEntryFragment_to_employesLocation)
        }
        binding.employeeButton.setOnClickListener{
            sharedPref.edit().putString("user_role", "employee").apply()
            findNavController().navigate(R.id.action_usersEntryFragment_to_employesLocation)
        }
    }

    private fun navigateToRoleFragment(role: String) {
        if (role == "admin") {
            findNavController().navigate(R.id.action_usersEntryFragment_to_employesLocation)
        } else if (role == "employee") {
            findNavController().navigate(R.id.action_usersEntryFragment_to_employesLocation)
        }
    }
}