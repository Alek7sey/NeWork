package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardImageBinding
import ru.netology.nework.dto.User

class UsersFilteredPostAdapter :
    ListAdapter<User, UsersFilteredPostAdapter.UsersFilteredPostViewHolder>(DiffCallBackUsersFilteredPost()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersFilteredPostViewHolder {
        return UsersFilteredPostViewHolder(
            CardImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: UsersFilteredPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UsersFilteredPostViewHolder(
        private val binding: CardImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                Glide.with(binding.previewImage)
                    .load("${user.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.previewImage)
            }
        }
    }

    class DiffCallBackUsersFilteredPost : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}