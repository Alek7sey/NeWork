package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.databinding.LikersImageBinding
import ru.netology.nework.dto.User

class PostLikersListAdapter : ListAdapter<User, PostLikersListAdapter.PostLikersViewHolder>(PostLikersDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostLikersViewHolder {
        return PostLikersViewHolder(
            CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PostLikersViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostLikersViewHolder(
        private val binding: CardUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                userName.text = user.name
                userLogin.text = user.login
                Glide.with(binding.avatar)
                    .load("${user.avatar}")
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)
            }
        }
    }

    class PostLikersDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}