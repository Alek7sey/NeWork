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
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.adapter.JobsMyAdapter
import ru.netology.nework.adapter.OnInteractionListenerJobsMy
import ru.netology.nework.databinding.FragmentMyJobsBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.viewmodel.JobsViewModel

class JobsMyFragment: Fragment() {

    private val viewModel: JobsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val binding = FragmentMyJobsBinding.inflate(inflater, container, false)

        val adapter = JobsMyAdapter(object : OnInteractionListenerJobsMy {
            override fun followTheLink(job: Job) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.link))
                startActivity(intent)
            }
        })

        binding.myJobsList.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dataMyJob.observe(viewLifecycleOwner) { myJobModel ->
                    adapter.submitList(myJobModel.myJobs)
                }
            }
        }

        viewModel.loadingMyJobState.observe(viewLifecycleOwner) {
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
                    viewModel.loadingMyJobState.observe(viewLifecycleOwner) {
                        viewModel.loadMyJobs()
                    }
                }.show()
            }
        }

        return binding.root
    }
}