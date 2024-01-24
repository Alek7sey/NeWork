package ru.netology.nework.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.IntentCompat.getParcelableExtra
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.adapter.PostLikersListAdapter
import ru.netology.nework.databinding.FragmentLikersBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.viewmodel.UsersViewModel

@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class PostLikersFragment : Fragment() {
    private val usersViewModel: UsersViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentLikersBinding.inflate(layoutInflater, container, false)
        val post = requireArguments().getSerializable("postKey") as Post
        //       val intent = Intent(activity, PostLikersFragment::class.java)
        // val post = getParcelableExtra(intent, "postKey", Post::class.java)

//        val post = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            intent.getParcelableExtra("postKey", Post::class.java)
//        } else {
//            intent.getParcelableExtra<Post>("postKey")
//        }

        val adapter = PostLikersListAdapter()
        binding.likersList.adapter = adapter

            usersViewModel.data.observe(viewLifecycleOwner) {
                val likerOwnersIds = post.likeOwnerIds.orEmpty().toSet()
                if (likerOwnersIds.isNotEmpty()) {
                    likerOwnersIds.forEach { likerOwnerId ->
                        val filterUsers =
                            it.users.filter { it.id == likerOwnerId.toLong() }
                        adapter.submitList(filterUsers)
                    }
                }
            }

        return binding.root
    }
}

//fun <T : Serializable?> getSerializable(key: String, m_class: Class<T>): T {
//    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
//       getSerializableExtra(key, m_class)!!
//    else
//        getSerializableExtra(key) as T
//}