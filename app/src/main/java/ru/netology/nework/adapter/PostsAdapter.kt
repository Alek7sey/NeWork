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
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentTypePost
import ru.netology.nework.dto.Post
import ru.netology.nework.utils.convertServerDateToLocalDate

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onDelete(post: Post) {}
    fun onShare(post: Post) {}
    fun onVideo(post: Post) {}
    fun followTheLink(post: Post) {}
    fun onImage(post: Post) {}
    fun onAudio(post: Post) {}
    fun onDetails(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallBack()) {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {

        context = parent.context

        return PostViewHolder(
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onInteractionListener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    class PostViewHolder(
        private val binding: CardPostBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {

            binding.apply {
                postAuthor.text = post.author
                published.text = convertServerDateToLocalDate(post.published)
                content.text = post.content
                jobPosition.text = post.authorJob
                link.text = post.link
                link.isVisible = !post.link.isNullOrBlank()

                likeBtn.isChecked = post.likedByMe
                likeBtn.text = post.likeOwnerIds?.size.toString()
                likeBtn.setOnClickListener {
                    likeBtn.isChecked = !likeBtn.isChecked
                    onInteractionListener.onLike(post)
                }

                postMenu.isVisible// = post.ownedByMe

                binding.imageAttachment.setOnClickListener {
                    when (post.attachment?.type) {
                        AttachmentTypePost.IMAGE -> onInteractionListener.onImage(post)
                        AttachmentTypePost.AUDIO -> onInteractionListener.onAudio(post)
                        AttachmentTypePost.VIDEO -> onInteractionListener.onVideo(post)
                        else -> {}
                    }
                }

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

                postMenu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.option_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.delete -> {
                                    onInteractionListener.onDelete(post)
                                    true
                                }

                                R.id.edit -> {
                                    onInteractionListener.onEdit(post)
                                    true
                                }

                                else -> false
                            }
                        }
                    }.show()
                }

             //  postMenu.isVisible = post.ownedByMe

                shareBtn.setOnClickListener { onInteractionListener.onShare(post) }
                link.setOnClickListener { onInteractionListener.followTheLink(post) }
                iconPlay.setOnClickListener { onInteractionListener.onVideo(post) }
                root.setOnClickListener { onInteractionListener.onDetails(post) }
                imageAttachment.setOnClickListener { onInteractionListener.onImage(post) }
            }
        }
    }

    class PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
        }
}