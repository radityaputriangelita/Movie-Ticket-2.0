package com.example.movie_ticket_20
//tab adapter untuk tab layout di admin
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.movie_ticket_20.fragments.SettingAdminFragment
import com.example.movie_ticket_20.fragments.ListFilmAdminFragment
import SettingsFragment
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabAdaptor(ac: AppCompatActivity) : FragmentStateAdapter(ac) {

    //ada 2 item doang
    override fun getItemCount(): Int {
        return 2
    }
    //assign 2 fragment yang terhubung ke page tab layout ini
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ListFilmAdminFragment()
            1 -> SettingAdminFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}
