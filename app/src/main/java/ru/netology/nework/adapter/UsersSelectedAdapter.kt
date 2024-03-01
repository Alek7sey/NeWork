package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserSelectedBinding
import ru.netology.nework.dto.User

interface OnInteractionListenerSelectedUsers {
    fun selectUser(user: User)
}

class UsersSelectedAdapter(
    private val listener: OnInteractionListenerSelectedUsers,
//    private val selectUser: Boolean,
    private val selectedUser: List<Int>? = null
) : ListAdapter<User, UsersSelectedAdapter.UserSelectedViewHolder>(UserSelectedDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSelectedViewHolder {
        val binding = CardUserSelectedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserSelectedViewHolder(binding, listener)
    }
    override fun onBindViewHolder(holder: UserSelectedViewHolder, position: Int) {
        val item = getItem(position) as User
        holder.bind(
            if (selectedUser?.firstOrNull { it.toLong() == item.id } == null)
                item
            else {
                item.copy(selected = true)
            })
    }


    class UserSelectedViewHolder(
        private val binding: CardUserSelectedBinding,
        private val listener: OnInteractionListenerSelectedUsers,
//        private val selectUser: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                selectedUserName.text = user.name
                selectedUserLogin.text = user.login
                selectedUserCheckBox.isChecked = user.selected
                selectedUserCheckBox.setOnClickListener {
                    listener.selectUser(user)
                }

                val urlAvatar = "${user.avatar}"
                Glide.with(binding.selectedUserAvatar)
                    .load(urlAvatar)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_error)
                    .into(binding.selectedUserAvatar)


            }
        }
    }

    class UserSelectedDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
    }
}