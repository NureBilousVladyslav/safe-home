package com.example.safehome.presentation.main.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.safehome.R
import com.example.safehome.databinding.FragmentProfileBinding
import com.example.safehome.presentation.auth.AuthActivity
import com.example.safehome.presentation.main.viewModel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.getValue
import androidx.core.net.toUri

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeHomesState()
    }

    private fun observeHomesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.userState.collect { user ->
                    with(binding){
                        nameTextView.text = user?.user?.name
                        emailTextView.text = user?.user?.email
                        subscriptionTextView.text = user?.user?.subscription_plan_name?.replaceFirstChar { it.uppercaseChar() }

                        subscriptionConstraintLayout.setOnClickListener {
                            val url = "https://safe-home-frontend-agf4a0cghre0fuhy.northeurope-01.azurewebsites.net/login"
                            val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
                            val customTabsIntent = builder.build()

                            try {
                                customTabsIntent.launchUrl(requireContext(), url.toUri())
                            } catch (e: android.content.ActivityNotFoundException) {
                                Timber.tag("SubscriptionClick").e("Custom Tabs failed: ${e.message}")
                                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                val packageManager = requireActivity().packageManager

                                if (intent.resolveActivity(packageManager) != null) {
                                    startActivity(intent)
                                } else {
                                    Timber.tag("SubscriptionClick").e("No app found to handle Intent")
                                }
                            }
                        }

                        changePswdConstraintLayout.setOnClickListener {
                            findNavController().navigate(R.id.action_navigation_profile_to_changePasswordFragment)
                        }

                        logoutConstraintLayout.setOnClickListener {
                            showLogoutDialog()
                        }
                    }
                }
            }
        }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        val exitButton = dialogView.findViewById<TextView>(R.id.exitButton)

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()
            .apply {
                show()
                cancelButton.setOnClickListener {
                    dismiss()
                }
                exitButton.setOnClickListener {
                    profileViewModel.logout()

                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
    }
}