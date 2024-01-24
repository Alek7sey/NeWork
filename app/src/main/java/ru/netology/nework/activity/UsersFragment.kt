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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.adapter.UsersOnInteractionListener
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.UsersViewModel

@AndroidEntryPoint
class UsersFragment : Fragment() {

    private val viewModel: UsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        val adapter = UsersAdapter(object : UsersOnInteractionListener {
            override fun onDetailsClick(user: User) {
                //    findNavController().navigate(на детальный фрагмент юзера, Bundle().apply{userArg = user.id})
            }
        })

        binding.usersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.observe(viewLifecycleOwner) {
                    adapter.submitList(it.users)
                }
            }
        }

//        viewModel.data.observe(viewLifecycleOwner) { state ->
//            binding.errorGroup.isVisible = state.empty
//            adapter.submitList(state.users)
//        }

        binding.retry.setOnClickListener {
            viewModel.loadUsers()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadUsers()
        }

        return binding.root
    }

//    companion object {
//        var Bundle.userArg: Long by UserArg
//    }
}