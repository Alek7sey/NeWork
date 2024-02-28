package ru.netology.nework.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.UsersAdapter
import ru.netology.nework.adapter.UsersOnInteractionListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.dto.User
import ru.netology.nework.utils.UserIdArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.UsersViewModel
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class UsersFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: UsersViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentUsersBinding.inflate(inflater, container, false)
        val adapter = UsersAdapter(object : UsersOnInteractionListener {
            override fun onDetailsClick(user: User) {
                findNavController().navigate(R.id.action_usersFragment_to_profileUserFragment, Bundle().apply { userId = user.id })
            }
        })

        binding.usersList.adapter = adapter

        val toolbar = binding.toolbar

        authViewModel.data.observe(viewLifecycleOwner) {

            if (authViewModel.authenticated) {
                toolbar.menu.setGroupVisible(R.id.registered, true)
                toolbar.menu.setGroupVisible(R.id.unregistered, false)
            } else {
                toolbar.menu.setGroupVisible(R.id.unregistered, true)
                toolbar.menu.setGroupVisible(R.id.registered, false)
            }

            toolbar.overflowIcon = resources.getDrawable(R.drawable.ic_account_circle)
            toolbar.setOnMenuItemClickListener { menuItem ->
                if (!authViewModel.authenticated) {
                    when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController().navigate(R.id.action_usersFragment_to_loginFragment)
                            true
                        }

                        else -> false
                    }
                } else {
                    when (menuItem.itemId) {
                        R.id.logout -> {
                            appAuth.clearAuth()
                            findNavController().navigateUp()
                            true
                        }

                        R.id.profil -> {
                            findNavController().navigate(R.id.action_usersFragment_to_profileMyFragment)
                            true
                        }

                        else -> false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.observe(viewLifecycleOwner) {
                    adapter.submitList(it.users)
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it.refreshing
            binding.progress.isVisible = it.refreshing || it.loading
            binding.errorGroup.isVisible = it.error
            if (it.error) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.users_not_loaded_try_again),
                    Snackbar.LENGTH_LONG
                ).setAction(
                    getString(R.string.try_loading_users)
                ) {
                    viewModel.loadUsers()
                }.show()
            }
        }

        binding.retry.setOnClickListener {
            viewModel.loadUsers()
        }

        return binding.root
    }

    companion object {
        var Bundle.userId: Long by UserIdArg
    }
}