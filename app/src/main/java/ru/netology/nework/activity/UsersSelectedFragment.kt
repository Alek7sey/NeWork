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
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersSelectedAdapter
import ru.netology.nework.databinding.FragmentUsersSelectedBinding
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class UsersSelectedFragment : Fragment() {
    private val usersViewModel: UsersViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersSelectedBinding.inflate(inflater, container, false)

        val mentionedIds = mutableListOf<Int>()
        val adapter = UsersSelectedAdapter()

        binding.mentionedUsersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.observe(viewLifecycleOwner) {
                    adapter.submitList(it.users)
                }
            }
        }

        adapter.selectedList.observe(viewLifecycleOwner) {
            mentionedIds.add(it)
        }

        val toolbar = binding.toolbarChooseUsers.toolbarSelectedUsers

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveChooseUsers -> {
                    postViewModel.saveMentionedIds(mentionedIds)
//                    postViewModel.save()
                    findNavController().navigateUp()
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}