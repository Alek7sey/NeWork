package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event

interface EventsOnInteractionListener {
    fun onLike(event: Event) {}
    fun onParticipate(event: Event) {}
    fun onEdit(event: Event) {}
    fun onDelete(event: Event) {}
    fun onShare(event: Event) {}

}

class EventsAdapter(
    private val onInteractionListener: EventsOnInteractionListener
) : PagingDataAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder(
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onInteractionListener
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class EventViewHolder(
        private val binding: CardEventBinding,
        private val onInteractionListener: EventsOnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                eventAuthor.text = event.author
                eventPublished.text = event.published
                content.text = event.content
                eventLikeBtn.text = event.likeOwnerIds?.size.toString()
                eventType.text = event.type
                eventData.text = event.datetime
                participantsBtn.text = event.participantsIds?.size.toString()

                eventLikeBtn.setOnClickListener {
                    eventLikeBtn.isChecked = !eventLikeBtn.isChecked
                    onInteractionListener.onLike(event)
                }

                participantsBtn.setOnClickListener {
                    participantsBtn.isChecked = !participantsBtn.isChecked
                    onInteractionListener.onParticipate(event)
                }

              //  attachment.isVisible = !event.attachment?.url.isNullOrBlank()

                eventMenu.isVisible// = event.ownedByMe

                val urlAvatar = "${event.authorAvatar}"
                Glide.with(binding.avatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)

                val imageIsVisible =
                    !event.attachment?.url.isNullOrBlank() && (event.attachment?.type?.name.toString() == AttachmentType.IMAGE.toString())
                if (imageIsVisible) {
                    val url = "${event.attachment?.url}"
                    Glide.with(binding.imageAttachment)
                        .load(url)
                        .centerInside()
                        .error(R.drawable.ic_error)
                        .into(binding.imageAttachment)
                    imageAttachment.visibility = View.VISIBLE
                } else {
                    imageAttachment.visibility = View.GONE
                }

                eventMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_event)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.delete -> {
                                    onInteractionListener.onDelete(event)
                                    true
                                }

                                R.id.edit -> {
                                    onInteractionListener.onEdit(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
    }

    class EventDiffCallBack : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}