package com.example.safehome.presentation.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safehome.R
import com.example.safehome.data.model.NotificationType
import com.example.safehome.databinding.FragmentNotificationBinding
import com.example.safehome.presentation.main.adapter.NotificationAdapter
import com.example.safehome.presentation.main.viewModel.NotificationViewModel
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class NotificationFragment : Fragment() {
    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val notificationViewModel: NotificationViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeNotificationsState()
        initToggle()
    }

    private fun observeNotificationsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificationViewModel.notificationsState.collect { notifications ->
                    notificationAdapter.submitList(notifications.toList())
                }
            }
        }
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter()
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationAdapter
        }
    }

    private fun initToggle() {
        binding.apply {
            toggleUnitButton(generalButton, generalButton, securityButton)

            generalButton.setOnClickListener {
                toggleUnitButton(generalButton, generalButton, securityButton)
                notificationViewModel.setNotificationType(NotificationType.GENERAL)
            }
            securityButton.setOnClickListener {
                toggleUnitButton(securityButton, generalButton, securityButton)
                notificationViewModel.setNotificationType(NotificationType.SECURITY)
            }
        }
    }

    private fun toggleUnitButton(
        clickedButton: MaterialButton,
        generalButton: MaterialButton,
        securityButton: MaterialButton
    ) {
        val buttons = listOf(generalButton, securityButton)

        clickedButton.isEnabled = false
        clickedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary)

        buttons.filter { it != clickedButton }.forEach { button ->
            button.isEnabled = true
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.onPrimaryVariant)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}