package ru.netology.nework.activity

import android.content.Intent
import android.net.Uri
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
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.JobsUserAdapter
import ru.netology.nework.adapter.OnInteractionListenerJobsUser
import ru.netology.nework.databinding.FragmentUserJobsBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.viewmodel.JobsViewModel

@AndroidEntryPoint
class JobsUserFragment : Fragment() {

    private val viewModel: JobsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding = FragmentUserJobsBinding.inflate(inflater, container, false)

        val adapter = JobsUserAdapter(object : OnInteractionListenerJobsUser {
            override fun followTheLink(job: Job) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.link))
                startActivity(intent)
            }
        })

        binding.userJobsList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataUserJob.observe(viewLifecycleOwner) { userJobModel ->
                    adapter.submitList(userJobModel.userJobs)
                }
            }
        }

        viewModel.loadingUserJobState.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = it.refreshing
            binding.progressBar.isVisible = it.refreshing || it.loading
            binding.errorGroup.isVisible = it.error
            if (it.error) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.download_error_try_again),
                    Snackbar.LENGTH_LONG
                ).setAction(
                    getString(R.string.retry)
                ) {
                    viewModel.userIdState.observe(viewLifecycleOwner) { userId ->
                        viewModel.loadUserJobs(userId.toLong())
                    }
                }.show()
            }
        }

        return binding.root
    }
}