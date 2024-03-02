package ru.netology.nework.adapter

import android.content.Context
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
import ru.netology.nework.dto.AttachmentTypeEvent
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.TypeStatus
import ru.netology.nework.utils.convertServerDateTimeToLocalDateTime

interface EventsOnInteractionListener {
    fun onLike(event: Event) {}
    fun onParticipate(event: Event) {}
    fun onEdit(event: Event) {}
    fun onDelete(event: Event) {}
    fun onImage(event: Event) {}
    fun onAudio(event: Event) {}
    fun onShare(event: Event) {}
    fun followTheLink(event: Event) {}
    fun onDetails(event: Event) {}

}

class EventsAdapter(
    private val listener: EventsOnInteractionListener,
    private val context: Context
) : PagingDataAdapter<Event, EventsAdapter.EventViewHolder>(EventDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder(
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener, context
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class EventViewHolder(
        private val binding: CardEventBinding,
        private val listener: EventsOnInteractionListener,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event) {
            binding.apply {
                eventAuthorName.text = event.author
                eventPublished.text = convertServerDateTimeToLocalDateTime(event.published)
                jobPosition.text = event.authorJob
                eventType.text = event.type
                eventData.text = convertServerDateTimeToLocalDateTime(event.datetime)
                content.text = event.content

                linkEvent.isVisible = !event.link.isNullOrBlank()
                linkEvent.text = event.link

                eventLikeBtn.isChecked = event.likedByMe
                eventLikeBtn.text = event.likeOwnerIds?.size.toString()
                eventLikeBtn.setOnClickListener {
                    eventLikeBtn.isChecked = !eventLikeBtn.isChecked
                    listener.onLike(event)
                }

                participantsBtn.text = event.participantsIds?.size.toString()
                participantsBtn.setOnClickListener {
                    participantsBtn.isChecked = !participantsBtn.isChecked
                    listener.onParticipate(event)
                }

                val urlAvatar = "${event.authorAvatar}"
                Glide.with(binding.avatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)

                val imageIsVisible =
                    !event.attachment?.url.isNullOrBlank() && (event.attachment?.type?.name.toString() == AttachmentTypeEvent.IMAGE.toString())
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

                when (event.attachment?.type) {
                    AttachmentTypeEvent.AUDIO -> iconPlay.isVisible = true
                    else -> {
                        iconPlay.isVisible = false
                    }
                }

                when (event.type) {
                    TypeStatus.ONLINE.toString() -> {
                        eventType.text =
                            context.getString(R.string.online)
                    }

                    TypeStatus.OFFLINE.toString() -> {
                        eventType.text =
                            context.getString(R.string.offline)
                    }
                }

                eventMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_event)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.deleteEvent -> {
                                    listener.onDelete(event)
                                    true
                                }

                                R.id.editEvent -> {
                                    listener.onEdit(event)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

                eventMenu.isVisible = event.ownedByMe

                linkEvent.setOnClickListener { listener.followTheLink(event) }
                likeShortBtn.setOnClickListener { listener.onLike(event) }
                iconPlay.setOnClickListener { listener.onAudio(event) }
                root.setOnClickListener { listener.onDetails(event) }
                imageAttachment.setOnClickListener { listener.onImage(event) }
                eventShareBtn.setOnClickListener { listener.onShare(event) }
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

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem == newItem
    }
}