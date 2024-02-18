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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.MyWallAdapter
import ru.netology.nework.adapter.OnInteractionListenerMyWall
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentMyWallBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.viewmodel.MyWallViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class WallMyFragment : Fragment() {
    private val postViewModel: PostViewModel by activityViewModels()
    private val myWallViewModel: MyWallViewModel by activityViewModels()
   // private val jobsViewModel: JobsViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentMyWallBinding.inflate(inflater, container, false)

        val adapter = MyWallAdapter(object : OnInteractionListenerMyWall {
            override fun onLike(post: Post) {
                postViewModel.likeById(post)
            }

            override fun onEdit(post: Post) {
                postViewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                postViewModel.removeById(post.id)
            }
        })

        binding.myWallList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                myWallViewModel.data.collectLatest {
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

        myWallViewModel.myWallPostsState.observe(viewLifecycleOwner) { userPostsState ->
            binding.swipeRefresh.isRefreshing = userPostsState.refreshing
            binding.progressBar.isVisible = userPostsState.loading || userPostsState.refreshing
            binding.errorGroup.isVisible = userPostsState.error
            if (userPostsState.error) {
                Snackbar.make(binding.root, getString(R.string.download_error_try_again), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.retry) {
                       myWallViewModel.loadMyWallPosts()
                    }.show()
            }
        }
        return binding.root
    }
}