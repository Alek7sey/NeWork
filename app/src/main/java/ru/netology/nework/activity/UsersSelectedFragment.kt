package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListenerSelectedUsers
import ru.netology.nework.adapter.UsersSelectedAdapter
import ru.netology.nework.databinding.FragmentUsersSelectedBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.UsersViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UsersSelectedFragment : Fragment() {
    private val usersViewModel: UsersViewModel by activityViewModels()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersSelectedBinding.inflate(inflater, container, false)

        val selectedUsers = mutableListOf<Int>()
//        val mentionedIds = mutableListOf<Int>()
 //       val arg = arguments?.getBoolean("selectedUsers") ?: false

        val adapter = UsersSelectedAdapter(object : OnInteractionListenerSelectedUsers {
            override fun selectUser(user: User) {
                if (selectedUsers.contains(user.id.toInt())) {
                    selectedUsers.remove(user.id.toInt())
                } else {
                    selectedUsers.add(user.id.toInt())
                }
            }
        }, selectedUsers)

        binding.mentionedUsersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.observe(viewLifecycleOwner) {
                    adapter.submitList(it.users)
                }
            }
        }

        binding.mentionedUsersList.adapter = adapter
//        adapter.selectedList.observe(viewLifecycleOwner) {
//            mentionedIds.add(it)
//        }

        val toolbar = binding.toolbarChooseUsers.toolbarSelectedUsers

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.saveChooseUsers -> {
//                    postViewModel.saveMentionedIds(mentionedIds)
//                    postViewModel.save()
                    setFragmentResult("usersFragment", bundleOf("selectedUsers" to gson.toJson(selectedUsers)))
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