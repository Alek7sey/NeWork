package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nework.R
import ru.netology.nework.databinding.CardUserSelectedBinding
import ru.netology.nework.dto.User

class UsersSelectedAdapter : ListAdapter<User, UsersSelectedAdapter.UserSelectedViewHolder>(UserSelectedDiffCallback()) {

    private val _selectedList = MutableLiveData<Int>()
    val selectedList: LiveData<Int>
        get() = _selectedList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSelectedViewHolder {
        return UserSelectedViewHolder(
            CardUserSelectedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun onBindViewHolder(holder: UserSelectedViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.findViewById<CheckBox>(R.id.selectedUserCheckBox)
            .setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    _selectedList.value = getItemId(position).toInt()
                }
            }
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class UserSelectedViewHolder(
        private val binding: CardUserSelectedBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.apply {
                selectedUserName.text = user.name
                selectedUserLogin.text = user.login

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