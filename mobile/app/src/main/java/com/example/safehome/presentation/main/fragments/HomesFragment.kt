package com.example.safehome.presentation.main.fragments

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.safehome.R
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.safehome.databinding.FragmentHomesBinding
import com.example.safehome.presentation.main.adapter.HomeAdapter
import com.example.safehome.presentation.main.viewModel.HomesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomesFragment : Fragment() {
    private val homesViewModel: HomesViewModel by activityViewModels()
    private lateinit var binding: FragmentHomesBinding
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeHomesState()
        observeErrorMessage()

        with(binding){
            addHomeButton.setOnClickListener {
                showAddHomeDialog()
            }
        }
    }

    private fun observeHomesState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homesViewModel.homesState.collect { homes ->
                    homeAdapter.submitList(homes)
                    binding.emptyTextView.visibility =
                        if (homes.isEmpty())
                            View.VISIBLE
                        else
                            View.GONE
                }
            }
        }
    }

    private fun observeErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homesViewModel.errorMessage.collect { errorMessage ->
                    errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeAdapter(
            onItemClick = { home ->
                val bundle = Bundle().apply {
                    putString("home_id", home.home_id)
                    putString("home_name", home.name)
                    putString("home_address", home.address)
                    putString("home_default_mode_name", home.default_mode_name)
                }
                findNavController().navigate(R.id.action_navigation_homes_to_navigation_sensor, bundle)
            },
            onArchiveClick = { homeId, isArchived ->
                viewLifecycleOwner.lifecycleScope.launch {
                    if (isArchived)
                        homesViewModel.unArchiveHome(homeId)
                    else
                    homesViewModel.archiveHome(homeId)
                }
            },
            onDeleteClick = { homeId ->
                viewLifecycleOwner.lifecycleScope.launch {
                    homesViewModel.deleteHome(homeId)
                }
            }
        )
        binding.homeRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.homeRecyclerView.adapter = homeAdapter
    }

    private fun showAddHomeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_home, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val addressEditText = dialogView.findViewById<EditText>(R.id.addressEditText)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        val addButton = dialogView.findViewById<TextView>(R.id.saveButton)

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()
            .apply {
                show()
                cancelButton.setOnClickListener {
                    dismiss()
                }
                addButton.setOnClickListener {
                    val name = nameEditText.text.toString()
                    val address = addressEditText.text.toString()
                    if (name.isBlank()){
                        Toast.makeText(requireContext(), "Name is empty", Toast.LENGTH_LONG).show()
                    }
                    homesViewModel.addHome(name, address)
                    dismiss()
                }
            }
    }
}