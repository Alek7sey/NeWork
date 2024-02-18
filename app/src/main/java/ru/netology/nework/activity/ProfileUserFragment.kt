package ru.netology.nework.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.activity.UsersFragment.Companion.userId
import ru.netology.nework.adapter.ViewPagerUserAdapter
import ru.netology.nework.databinding.FragmentUserProfileBinding
import ru.netology.nework.viewmodel.JobsViewModel
import ru.netology.nework.viewmodel.UsersViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ProfileUserFragment : Fragment(R.layout.fragment_user_profile) {

    private val usersViewModel: UsersViewModel by activityViewModels()
    private val jobsViewModel: JobsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentUserProfileBinding.bind(view)

        val toolbar = binding.toolbarProfileUser.toolbarUserProfile

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = binding.viewPagerUser
        val viewPagerUserAdapter = ViewPagerUserAdapter(this)
        viewPager.adapter = viewPagerUserAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                return
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                return
            }
        })

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                tabLayout.getTabAt(position)?.select()
            }
        })

        val userId = arguments?.let { it.userId }

        usersViewModel.data.observe(viewLifecycleOwner) { userModel ->
            userModel.users.find { it.id == userId }.let { user ->
                binding.apply {
                    Glide.with(binding.profileUserImage)
                        .load("${user?.avatar}")
                        .centerInside()
                        .error(R.drawable.ic_error)
                        .into(binding.profileUserImage)
                    toolbar.title = user?.name + " / " + user?.login
                }
                jobsViewModel.setUserIdState(user?.id!!.toLong())
            }
        }
    }
}