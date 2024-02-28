package ru.netology.nework.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.activity.JobsMyFragment
import ru.netology.nework.activity.WallMyFragment

@ExperimentalCoroutinesApi
class ViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WallMyFragment()
            1 -> JobsMyFragment()
            else -> WallMyFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}