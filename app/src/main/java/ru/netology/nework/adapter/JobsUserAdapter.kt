package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardJobUserBinding
import ru.netology.nework.dto.Job

interface OnInteractionListenerJobsUser {
    fun followTheLink(job: Job) {}
}

class JobsUserAdapter(
    private val listener: OnInteractionListenerJobsUser
) : ListAdapter<Job, JobsUserAdapter.JobsUserViewHolder>(DiffCallbackJobsUser()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsUserViewHolder {
        return JobsUserViewHolder(
            CardJobUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: JobsUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobsUserViewHolder(
        private val binding: CardJobUserBinding,
        private val listener: OnInteractionListenerJobsUser
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: Job) {
            binding.apply {
                jobName.text = job.name
                startDate.text = job.start
                endDate.text = job.finish
                jobPosition.text = job.position

                jobLink.isVisible = job.link.isNullOrBlank()
                jobLink.text = job.link

                binding.jobLink.setOnClickListener {
                    listener.followTheLink(job)
                }
            }
        }
    }

    class DiffCallbackJobsUser : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem
    }
}