package com.exa.android.loctrace.Fragments.Authentication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.exa.android.loctrace.Fragments.UsersEntryFragment
import com.exa.android.loctrace.Helper.AppConstants
import com.exa.android.loctrace.R
import com.exa.android.loctrace.databinding.FragmentSignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class SignInFragment : Fragment() {

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            signInWithEmail()
        }

        binding.signUpNavigation.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }
    }

    private fun signInWithEmail() {
        val email = binding.emailAddressEt.text.toString()
        val password = binding.passwordEt.text.toString()

        val verifyEmail = AppConstants.verifyEmail(email)
        val verifyPassword = AppConstants.verifyPassword(password)

        if (!verifyEmail.first && !verifyPassword.first) {
            binding.emailTextInputLayout.error = verifyEmail.second.toString()
            binding.passwordTextInputLayout.error = verifyPassword.second.toString()
            return
        } else if (!verifyEmail.first) {
            binding.emailTextInputLayout.error = verifyEmail.second
            return
        } else if (!verifyPassword.first) {
            binding.passwordTextInputLayout.error = verifyPassword.second
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    val userName = binding.usernameAddressEt.text.toString()

                    // Update display name only if it's set during user creation
                    if (currentUser != null && userName.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(userName)
                            .build()

                        currentUser.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateProfileTask ->
                                if (updateProfileTask.isSuccessful) {
                                    Log.d("SignInFragment", "User profile updated")
                                } else {
                                    Log.e("SignInFragment", "Failed to update user profile", updateProfileTask.exception)
                                }
                            }
                    }

                    findNavController().navigate(R.id.action_signInFragment_to_usersEntryFragment)
                } else {
                    Toast.makeText(requireContext(), "Error signing in: ${signInTask.exception?.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null && currentUser.displayName != null) {
            AppConstants.userId = currentUser.displayName
            findNavController().navigate(R.id.action_signInFragment_to_usersEntryFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
