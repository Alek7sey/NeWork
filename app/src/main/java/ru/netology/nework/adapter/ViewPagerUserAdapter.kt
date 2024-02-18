package ru.netology.nework.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.activity.JobsUserFragment
import ru.netology.nework.activity.WallUserFragment

class ViewPagerUserAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> WallUserFragment()
            1 -> JobsUserFragment()
            else -> WallUserFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}