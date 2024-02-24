package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentTypePost
import ru.netology.nework.dto.Post
import ru.netology.nework.utils.convertServerDateToLocalDate

interface OnInteractionListenerUserWall {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
}

class UserWallAdapter(
    private val listener: OnInteractionListenerUserWall
) : PagingDataAdapter<Post, UserWallAdapter.UserWallViewHolder>(UserWallDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserWallViewHolder {
        return UserWallViewHolder(
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: UserWallViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    class UserWallViewHolder(
        private val binding: CardPostBinding,
        private val listener: OnInteractionListenerUserWall
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                postAuthor.text = post.author
                content.text = post.content
                published.text = convertServerDateToLocalDate(post.published)

                likeBtn.isChecked = post.likedByMe
                likeBtn.text = post.likeOwnerIds?.size.toString()
                likeBtn.setOnClickListener {
                    likeBtn.isChecked = !likeBtn.isChecked
                    listener.onLike(post)
                }

                postMenu.visibility = View.GONE

                val urlAvatar = "${post.authorAvatar}"
                Glide.with(binding.avatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)

                if (!post.attachment?.url.isNullOrBlank()) {
                    val url = "${post.attachment?.url}"
                    Glide.with(binding.imageAttachment)
                        .load(url)
                        .centerInside()
                        .error(R.drawable.ic_error)
                        .into(binding.imageAttachment)

                    imageAttachment.isVisible = post.attachment?.type == AttachmentTypePost.IMAGE
                            || post.attachment?.type == AttachmentTypePost.VIDEO
                    iconPlay.isVisible = post.attachment?.type == AttachmentTypePost.VIDEO
                            || post.attachment?.type == AttachmentTypePost.AUDIO
                } else {
                    postAttachmentFrame.visibility = View.GONE
                }

                likeBtn.setOnClickListener { listener.onLike(post) }
                shareBtn.setOnClickListener { listener.onShare(post) }
            }
        }
    }

    class UserWallDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}