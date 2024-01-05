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
import ru.netology.nework.BuildConfig
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onDelete(post: Post) {}
    fun onShare(post: Post) {}
//    fun onRunVideo(post: Post) {}
//    fun onViewPost(post: Post) {}
//    fun onSend(post: Post) {}
//    fun onImage(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
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
                content.text = post.content
                published.text = post.published
                likeBtn.isChecked = post.likedByMe
                likeBtn.text = post.likeOwnerIds?.size.toString()

                likeBtn.setOnClickListener {
                    likeBtn.isChecked = !likeBtn.isChecked
                    onInteractionListener.onLike(post)
                }

                imageAttachment.isVisible = !post.attachment?.url.isNullOrBlank()

                postMenu.isVisible = post.ownedByMe

                val urlAvatar = "${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}"
                Glide.with(binding.avatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)

                if (post.attachment != null) {
                    val url = "${BuildConfig.BASE_URL}/media/${post.attachment.url}"
                    Glide.with(binding.imageAttachment)
                        .load(url)
                        .centerInside()
                        .error(R.drawable.ic_error)
                        .into(binding.imageAttachment)
                    imageAttachment.visibility = View.VISIBLE
                } else {
                    imageAttachment.visibility = View.GONE
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

            shareBtn.setOnClickListener { onInteractionListener.onShare(post) }
//            viewsButton.setOnClickListener { onInteractionListener.onViews(post) }
//            groupVideo.setAllOnClickListener { onInteractionListener.onRunVideo(post) }
//            root.setOnClickListener { onInteractionListener.onViewPost(post) }
//            send.setOnClickListener { onInteractionListener.onSend(post) }
//            attachment.setOnClickListener { onInteractionListener.onImage(post) }
            }
        }
    }

//fun Group.setAllOnClickListener(listener: View.OnClickListener?) {
//    referencedIds.forEach { id ->
//        rootView.findViewById<View>(id).setOnClickListener(listener)
//    }
//}

    class PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}