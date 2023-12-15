package com.example.movie_ticket_20

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.movie_ticket_20.fragments.SettingAdminFragment
import com.example.movie_ticket_20.fragments.ListFilmAdminFragment
import SettingsFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdaptor(ac: AppCompatActivity) : FragmentStateAdapter(ac) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFilmAdminFragment()
            1 -> SettingAdminFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}
