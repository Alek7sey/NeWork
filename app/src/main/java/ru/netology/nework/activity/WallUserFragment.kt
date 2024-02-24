package ru.netology.nework.activity

import android.content.Intent
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListenerUserWall
import ru.netology.nework.adapter.UserWallAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentUserWallBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.JobsViewModel
import ru.netology.nework.viewmodel.PostViewModel
import ru.netology.nework.viewmodel.UserWallViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WallUserFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val userWallViewModel: UserWallViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentUserWallBinding.inflate(inflater, container, false)

        val adapter = UserWallAdapter(object : OnInteractionListenerUserWall {
            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    postViewModel.likeById(post)
                } else {
                    findNavController().navigate(R.id.action_postsFeedFragment_to_loginFragment)
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.share_post))
                startActivity(shareIntent)
                postViewModel.shareById(post.id)
            }
        })

        binding.userList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userWallViewModel.data.collectLatest {
                    adapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appAuth.authFlow.collect {
                    adapter.refresh()
                }
            }
        }

        userWallViewModel.userWallPostsState.observe(viewLifecycleOwner) { userPostsState ->
            binding.swipeRefresh.isRefreshing = userPostsState.refreshing
            binding.progressBar.isVisible = userPostsState.loading || userPostsState.refreshing
            binding.errorGroup.isVisible = userPostsState.error
            if (userPostsState.error) {
                Snackbar.make(binding.root, getString(R.string.download_error_try_again), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                        postViewModel.postData.observe(viewLifecycleOwner) { postModel ->
                            jobsViewModel.userIdState.observe(viewLifecycleOwner) { userId ->
                                postModel.posts.find { it.authorId == userId.toLong() }.let { post ->
                                    userWallViewModel.loadUserWallPosts(post?.authorId!!)
                                }
                            }
                        }
                    }.show()
            }
        }
        return binding.root
    }
}