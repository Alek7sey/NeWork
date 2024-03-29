package ru.netology.nework.activity

import android.content.Intent
import android.os.Build
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
import ru.netology.nework.adapter.PostLikersListAdapter
import ru.netology.nework.databinding.FragmentLikersPostBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostLikersFragment : Fragment() {

    private val usersViewModel: UsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentLikersPostBinding.inflate(inflater, container, false)
        val toolbar = binding.toolbarLikersFull.toolbarLikers

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        val intent = Intent(activity, PostLikersFragment::class.java)

        val post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("postKey", Post::class.java)
            requireArguments().getSerializable("postKey", Post::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getSerializable("postKey") as Post
        }

        val adapter = PostLikersListAdapter()
        binding.likersList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.observe(viewLifecycleOwner) {
                    val likeOwnersIds = post?.likeOwnerIds.orEmpty().toSet()
                    if (likeOwnersIds.isNotEmpty()) {
                        likeOwnersIds.forEach { likeOwnerId ->
                            val filterUsers =
                                it.users.filter { it.id == likeOwnerId.toLong() }
                            adapter.submitList(filterUsers)
                        }
                    }
                }
            }
        }
        return binding.root
    }
}