package ru.netology.nework.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.adapter.ViewPagerAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentMyProfileBinding
import ru.netology.nework.viewmodel.RegisterViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ProfileMyFragment : Fragment(R.layout.fragment_my_profile) {

    @Inject
    lateinit var appAuth: AppAuth

    private val viewModel: RegisterViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val binding = FragmentMyProfileBinding.bind(view)



//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                if (appAuth.authFlow.value.token != null) {
//                    menuInflater.inflate(R.menu.menu_my_profile, menu)
//                } else {
//                    menuInflater.inflate(R.menu.menu_my_profile_auth, menu)
//                }
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
//                when (menuItem.itemId) {
//                    R.id.loginMyProfile -> {
//                               findNavController().navigate(R.id.action_profileMyFragment_to_loginFragment)
//                        true
//
//                    }
//
//                    R.id.logoutMyProfile -> {
//                        appAuth.clearAuth()
//                        true
//                    }
//
//                    else -> false
//                }
//        }, viewLifecycleOwner)

        val toolbar = binding.toolbarMyProfile.toolbarMenuMyProfile

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.logoutMyProfile -> {
                    appAuth.clearAuth()
                    true
                }

                else -> false
            }
        }

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = binding.viewPager
        val viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter

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

        binding.fab.setOnClickListener {
            if (appAuth.authFlow.value.token != null) {
                findNavController().navigate(R.id.action_profileMyFragment_to_jobAddFragment)
            }
            Snackbar.make(
                binding.root,
                getString(R.string.to_add_job_you_need_to_register_or_login),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.login)) {
                findNavController().navigate(R.id.action_profileMyFragment_to_loginFragment)
            }.show()
        }

        viewModel.photoState.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.profileMyImage.setImageURI(Uri.parse(it.file.toString()))
            }
        }
    }
}