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

//interface OnIteractionListenerUsersFiltered {
//    fun returnEvent(event: Event) {}
//}

class UsersFilteredEventAdapter(
//    private val listener: OnIteractionListenerUsersFiltered
) : ListAdapter<User, UsersFilteredEventAdapter.UsersFilteredEventViewHolder>(UsersFilteredEventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersFilteredEventViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CardImageBinding.inflate(inflater, parent, false)
        return UsersFilteredEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UsersFilteredEventViewHolder, position: Int) {
        if (position == 5) {
            holder.addButtonMore()
        } else {
            val user = getItem(position) as User
            holder.bind(user)
        }

    }

    override fun getItemCount(): Int {
        val size = currentList.size
        return if (size > 4) size + 1 else size
    }

    class UsersFilteredEventViewHolder(
        private val binding: CardImageBinding,
//        private val listener: OnIteractionListenerUsersFiltered
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

        fun addButtonMore() {
            binding.previewImage.setImageResource(R.drawable.ic_add_avatar)
        }
    }

    class UsersFilteredEventDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}