package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.User

interface UsersOnInteractionListener {
    fun onDetailsClick(user: User) {}
}

class UsersAdapter(
    private val listener: UsersOnInteractionListener
) : ListAdapter<User, UsersAdapter.UsersViewHolder>(UsersDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return UsersViewHolder(
            CardUserBinding.inflate(inflater, parent, false), listener)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user= getItem(position)
        holder.bind(user)
    }

    class UsersViewHolder(
        private val binding: CardUserBinding,
        private val listener: UsersOnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                userName.text = user.name
                userLogin.text = user.login

                val urlAvatar = "${user.avatar}"
                Glide.with(binding.avatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.avatar)

                binding.root.setOnClickListener {
                    listener.onDetailsClick(user)
                }
            }
        }
    }

    class UsersDiffCallBack : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}

