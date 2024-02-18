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
import ru.netology.nework.adapter.EventsAdapter
import ru.netology.nework.adapter.EventsOnInteractionListener
import ru.netology.nework.adapter.PostLoadingStateAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.utils.EventIdArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.EventsViewModel
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class EventsFeedFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: EventsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedEventsBinding.inflate(layoutInflater, container, false)
        val adapter = EventsAdapter(object : EventsOnInteractionListener {

            override fun onLike(event: Event) {
                if (authViewModel.authenticated) {
                    viewModel.likeById(event)
                } else {
                    findNavController().navigate(R.id.action_eventsFeedFragment_to_loginFragment)
                }
            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.share_event))
                startActivity(shareIntent)
                viewModel.shareById(event.id)
            }

            override fun onEdit(event: Event) {
                viewModel.edit(event)
                findNavController().navigate(
                    R.id.action_eventsFeedFragment_to_eventAddFragment,
                    Bundle().apply {
                        eventIdArg = event.id
                    }
                )
            }

            override fun onDelete(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onImage(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFeedFragment_to_eventImageFragment,
                    Bundle().apply { putString("urlAttach", event.attachment?.url) })
            }

            override fun onAudio(event: Event) {
                val audioIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.attachment?.url))
                val chooserIntent = Intent.createChooser(
                    audioIntent,
                    getString(R.string.choose_where_open_your_audio)
                )
                startActivity(chooserIntent)
            }

            override fun onDetails(event: Event) {
                findNavController().navigate(
                    R.id.action_eventsFeedFragment_to_eventDetailsFragment,
                    Bundle().apply { eventIdArg = event.id })
            }

        }, requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest(adapter::submitData)
            }
        }

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
                    binding.swipeRefreshLayout.isRefreshing = state.refresh is LoadState.Loading
//                    val isListEmpty = state.refresh is LoadState.NotLoading && adapter.itemCount == 0
//                    binding.groupEmpty.isVisible = isListEmpty
                }
            }
        }

        binding.eventsList.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() },
        )

        binding.retryBtnEmpty.setOnClickListener {
            viewModel.loadEvents()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            //       if (appAuth.authFlow.value != null) {
            findNavController().navigate(R.id.action_eventsFeedFragment_to_eventAddFragment)
            //      }

//            context?.let { it1 ->
//                MaterialAlertDialogBuilder(it1)
//                    .setTitle(resources.getString(R.string.fab_click_message))
//                    .setNegativeButton(resources.getString(R.string.login)) { dialog, _ ->
//                        dialog.dismiss()
//                        findNavController().navigate(R.id.action_eventsFeedFragment_to_loginFragment)
//                    }
//                    .setPositiveButton(resources.getString(R.string.registration)) { dialog, _ ->
//                        dialog.dismiss()
//                        findNavController().navigate(R.id.action_eventsFeedFragment_to_registrationFragment)
//                    }
//                    .show()
//            }
        }
        return binding.root
    }

    companion object {
        var Bundle.eventIdArg: Long by EventIdArg
    }
}