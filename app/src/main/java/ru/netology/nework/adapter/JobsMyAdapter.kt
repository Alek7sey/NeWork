package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.dto.Job

interface OnInteractionListenerJobsMy {
    fun followTheLink(job: Job) {}
    fun deleteJob(job: Job) {}
}

class JobsMyAdapter(

    private val listener: OnInteractionListenerJobsMy

) : ListAdapter<Job, JobsMyAdapter.JobsMyViewHolder>(DiffCallbackJobsMy()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobsMyViewHolder {
        return JobsMyViewHolder(
            CardJobBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: JobsMyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobsMyViewHolder(
        private val binding: CardJobBinding,
        private val listener: OnInteractionListenerJobsMy
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

                binding.deleteJob.setOnClickListener {
                    listener.deleteJob(job)
                }
            }
        }
    }

    class DiffCallbackJobsMy : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem
    }

}