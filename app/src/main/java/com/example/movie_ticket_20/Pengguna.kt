package com.example.movie_ticket_20

import SettingsFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.movie_ticket_20.databinding.ActivityPenggunaBinding
import com.example.movie_ticket_20.fragments.FavoritesFragment
import com.example.movie_ticket_20.fragments.HomeFragment
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

class Pengguna : AppCompatActivity() {

    private lateinit var binding: ActivityPenggunaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenggunaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //ngehubungin bottom navigationnya dengan 3 fragment home, favorite, dan settings
        val homeFragment = HomeFragment()
        val favoritesFragment = FavoritesFragment()
        val settingsFragment = SettingsFragment()

        makeCurrentFragment(homeFragment)
        //nanti dia bakal bisa zoom in out gitu pake perform scale saat btn nya di klik
        binding.bottomNavPengguna.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.baseline_home -> {
                    makeCurrentFragment(homeFragment)
                    performScaleAnimation(binding.bottomNavPengguna.findViewById(R.id.baseline_home))
                }
                R.id.baseline_favorite -> {
                    makeCurrentFragment(favoritesFragment)
                    performScaleAnimation(binding.bottomNavPengguna.findViewById(R.id.baseline_favorite))
                }
                R.id.baseline_settings -> {
                    makeCurrentFragment(settingsFragment)
                    performScaleAnimation(binding.bottomNavPengguna.findViewById(R.id.baseline_settings))
                }
            }
            true
        }
    }

    //bikin fragmentnya
    private fun makeCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper_navbottom, fragment)
            commit()
        }

    //animasi zoom nya dkit
    private fun performScaleAnimation(view: View) {
        val scaleAnimation = ScaleAnimation(
            1.0f, 1.2f, // dari scaleX, ke scaleX setelah animasi
            1.0f, 1.2f, // dari scaleY, ke scaleY setelah animasi
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        //pakein durasi lagi
        scaleAnimation.duration = 200
        scaleAnimation.fillAfter = true
        view.startAnimation(scaleAnimation)
    }
}

