package ru.netology.nework.activity

import android.content.Intent
import android.net.Uri
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
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.activity.PostDetailsFragment.Companion.idArg
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostLoadingStateAdapter
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.utils.EditTextArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostsFeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth


    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedPostsBinding.inflate(layoutInflater, container, false)

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
                            findNavController().navigate(R.id.action_postsFeedFragment_to_loginFragment)
                            true
                        }

                        else -> false
                    }
                } else {
                    when (menuItem.itemId) {
                        R.id.logout -> {
                            appAuth.clearAuth()
                            true
                        }

                        R.id.profil -> {
                            findNavController().navigate(R.id.action_postsFeedFragment_to_profileMyFragment)
                            true
                        }

                        else -> false
                    }
                }
            }
        }

        val adapter = PostsAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                if (authViewModel.authenticated) {
                    viewModel.likeById(post)
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
                viewModel.shareById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postsFeedFragment_to_postAddFragment,
                    Bundle().apply {
                        textArg = post.content
//                        postIdArg = post.id
                   //     putString("urlAttach", post.attachment?.url)
                    }
                )
            }

            override fun onDelete(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onImage(post: Post) {
                findNavController().navigate(
                    R.id.action_postsFeedFragment_to_postImageFragment,
                    Bundle().apply { putString("urlAttach", post.attachment?.url) })
            }

            override fun onVideo(post: Post) {
                val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val chooserIntent = Intent.createChooser(videoIntent, "choose_where_open_your_video")
                startActivity(chooserIntent)
            }

            override fun onAudio(post: Post) {
                val audioIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.attachment?.url))
                val chooserIntent = Intent.createChooser(
                    audioIntent,
                    getString(R.string.choose_where_open_your_audio)
                )
                startActivity(chooserIntent)
            }

            override fun onDetails(post: Post) {
                findNavController().navigate(
                    R.id.action_postsFeedFragment_to_detailsPostFragment,
                    Bundle().apply { idArg = post.id })
            }

            override fun followTheLink(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
                startActivity(intent)
            }

        })

        binding.postsList.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() },
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

//        viewModel.state.observe(viewLifecycleOwner) { state ->
//            binding.progress.isVisible = state.loading
//            binding.swipeRefreshLayout.isRefreshing = state.refreshing
//            if (state.error) {
//                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.retry) {
//                        adapter.refresh()
//                    }
//                    .show()
//            }
//        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appAuth.authFlow.collect() {
                    adapter.refresh()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefreshLayout.isRefreshing =
                        state.refresh is LoadState.Loading

//                    val isListEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
//                    binding.groupEmpty.isVisible = isListEmpty
//                    binding.list.isVisible = !isListEmpty
//                    binding.retryBtnEmpty.isVisible = state.source.refresh is LoadState.Error
//
//                    val errorState = state.source.append as? LoadState.Error
//                        ?: state.source.prepend as? LoadState.Error
//                        ?: state.append as? LoadState.Error
//                        ?: state.prepend as? LoadState.Error
//                    errorState?.let {
//                        Toast.makeText(requireActivity(), "Whoops ${it.error}", Toast.LENGTH_LONG).show()
//                    }
                }
            }
        }

        binding.retryBtnEmpty.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            //   if (appAuth.authFlow.value?.token != null) {
            findNavController().navigate(R.id.action_postsFeedFragment_to_postAddFragment)
            //     }

//            context?.let { it1 ->
//                MaterialAlertDialogBuilder(it1)
//                    .setTitle(resources.getString(R.string.fab_click_message))
//                    .setNegativeButton(resources.getString(R.string.login)) { dialog, _ ->
//                        dialog.dismiss()
//                        findNavController().navigate(R.id.action_postsFeedFragment_to_loginFragment)
//                    }
//                    .setPositiveButton(resources.getString(R.string.registration)) { dialog, _ ->
//                        dialog.dismiss()
//                        findNavController().navigate(R.id.action_postsFeedFragment_to_registrationFragment)
//                    }
//                    .show()
//            }
        }
        return binding.root
    }

    companion object {
        var Bundle.textArg by EditTextArg
//        var Bundle.postIdArg by PostIdArg
    }
}