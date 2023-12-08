package com.example.movie_ticket_20

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.movie_ticket_20.databinding.ActivityPenggunaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Pengguna : AppCompatActivity() {

    private lateinit var binding: ActivityPenggunaBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPenggunaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        with(binding){
            btnLogoutPengguna.setOnClickListener{
                // Hapus status login dari SharedPreferences
                val autologin = sharedPreferences.edit()
                autologin.putBoolean("isLogIn", false)
                autologin.apply()

                // Logout dari Firebase Auth
                firebaseAuth.signOut()

                // Pindah ke Signin Activity
                val intent = Intent(this@Pengguna, Signin::class.java)
                startActivity(intent)
                finish() // Selesaikan activity MainActivity agar tidak kembali saat tombol back ditekan
            }
        }
    }
}