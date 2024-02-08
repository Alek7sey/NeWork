package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.EventLikersListAdapter
import ru.netology.nework.databinding.FragmentLikersEventBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class EventLikersFragment: Fragment() {
    private val usersViewModel: UsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentLikersEventBinding.inflate(inflater, container, false)
        val event = requireArguments().getSerializable("eventKey") as Event
        val toolbar = binding.toolbarLikersFull.toolbarLikers

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val adapter = EventLikersListAdapter()
        binding.likersListEvent.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.observe(viewLifecycleOwner) {
                    val likerOwnersIds = event.likeOwnerIds.orEmpty().toSet()
                    if (likerOwnersIds.isNotEmpty()) {
                        likerOwnersIds.forEach { likerOwnerId ->
                            val filterUsers =
                                it.users.filter { it.id == likerOwnerId.toLong() }
                            adapter.submitList(filterUsers)
                        }
                    }
                }
            }
        }

        return binding.root
    }

}