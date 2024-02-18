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
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentTypePost
import ru.netology.nework.dto.Post

interface OnInteractionListenerMyWall {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
}

class MyWallAdapter(
    private val listener: OnInteractionListenerMyWall
) : PagingDataAdapter<Post, MyWallAdapter.MyWallViewHolder>(MyWallDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyWallViewHolder {
        return MyWallViewHolder(
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            listener = listener
        )
    }

    override fun onBindViewHolder(holder: MyWallViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item)
    }

    class MyWallViewHolder(
        private val binding: CardPostBinding,
        private val listener: OnInteractionListenerMyWall
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(post: Post) {
            binding.apply {
                postAuthor.text = post.author
                content.text = post.content
                published.text = post.published

                likeBtn.text = post.likeOwnerIds?.size.toString()
                likeBtn.setOnClickListener {
                    likeBtn.isChecked = !likeBtn.isChecked
                    listener.onLike(post)
                }

                postMenu.isVisible

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
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.edit -> {
                                    listener.onEdit(post)
                                    true
                                }

                                R.id.delete -> {
                                    listener.onRemove(post)
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

    class MyWallDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean = oldItem == newItem
    }
}