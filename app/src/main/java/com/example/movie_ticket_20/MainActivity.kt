package com.example.movie_ticket_20

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.movie_ticket_20.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        // simpen pengguna yang lagi login dari fireAuth
        val currentUser = firebaseAuth.currentUser

        // Jika pengguna sedang masuk
        currentUser?.let { user ->
            val uid = user.uid

            // get data dia dari firestore
            firestore.collection("user").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        // namanya ditaro di textview
                        val usernameTextView: TextView = binding.usernameXml
                        usernameTextView.text = "$username"
                    } else {
                        Log.d("MainActivity", "Document not found")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainActivity", "Error fetching user data: $e")
                }
        }
        with(binding){
            btnLogout.setOnClickListener{
                // Hapus status login dari SharedPreferences
                val autologin = sharedPreferences.edit()
                autologin.putBoolean("isLogIn", false)
                autologin.apply()

                // Logout dari Firebase Auth
                firebaseAuth.signOut()

                // Pindah ke Signin Activity
                val intent = Intent(this@MainActivity, Signin::class.java)
                startActivity(intent)
                finish() // Selesaikan activity MainActivity agar tidak kembali saat tombol back ditekan
            }
        }
    }
}
