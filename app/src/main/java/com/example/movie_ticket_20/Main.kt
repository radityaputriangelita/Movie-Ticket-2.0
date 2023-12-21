package com.example.movie_ticket_20

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.movie_ticket_20.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayoutMediator
class Main : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding untuk tab layout menggunakan viewPager
        with(binding) {
            val sectionPager = TabAdaptor(this@Main)
            val viewPager: ViewPager2 = findViewById(R.id.view_pager_admin)
            viewPager.adapter = sectionPager
            TabLayoutMediator(tabLayoutAdmin, viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "EDIT"
                    1 -> tab.text = "SETTINGS"
                    else -> tab.text = "Tab $position"
                }
            }.attach()
        }
    }
}


