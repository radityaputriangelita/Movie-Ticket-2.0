package com.example.movie_ticket_20

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.movie_ticket_20.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Signin : AppCompatActivity() {

    private lateinit var binding: ActivitySigninBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private val userCollection = firestore.collection("user")

    private lateinit var sharedPreferences:SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        //buat preferences
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        //cek shared preferences itu
        val isLogIn = sharedPreferences.getBoolean("isLogIn", false)
        if (isLogIn){
            // udh login tapi cek dulu role penggunanya
            val currentUser = firebaseAuth.currentUser
            currentUser?.let { user ->
                val uid = user.uid
                firestore.collection("user").document(uid)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val role = document.getString("role")
                            if (role == "admin") {
                                val intent = Intent(this@Signin, MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                val intent = Intent(this@Signin, Pengguna::class.java)
                                startActivity(intent)
                            }
                            finish()
                        } else {
                            Log.d("SigninActivity", "Dokumen uid tidak ditemukan")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("SigninActivity", "Error fetching user data: $e")
                    }
            }
        }


        with(binding){
            btnLogin.setOnClickListener{
                val email = binding.emailInputEdittext.text.toString()
                val pass = binding.passwordInputEdittext.text.toString()

                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    // sign in ke main activity
                    firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if(checkboxRemember.isChecked) {
                                val autologin = sharedPreferences.edit()
                                autologin.putBoolean("isLogIn", true)
                                autologin.apply()
                            }

                            //coba admin dan pengguna
                            val currentUser = firebaseAuth.currentUser
                            //get data penggunanya dulu uid nya
                            currentUser?.let { user ->
                                val uid = user.uid
                                firestore.collection("user").document(uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null){
                                            val role = document.getString("role")
                                            if(role == "admin"){
                                                //intent ke admin page
                                                val intent = Intent(this@Signin, MainActivity::class.java)
                                                startActivity(intent)
                                            }else{
                                                //ke pengguna page
                                                val intent = Intent(this@Signin, Pengguna::class.java)
                                                startActivity(intent)
                                            }
                                            finish()
                                        } else{
                                            Log.d("SigninActivity", "Dokumen uid tidak ditemukan")
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("SigninActivity", "Error fetching user data: $e")
                                    }
                            }
                        } else {
                            // Sign-in tidak berhasil karena data tidak valid
                            Toast.makeText(this@Signin, "Login gagal. Periksa kembali email dan password Anda.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    //input formnya belom lengkap
                    Toast.makeText(this@Signin, "Lengkapi form untuk masuk", Toast.LENGTH_SHORT).show()
                }
            }

            //intent pindah ke signup form
            signup.setOnClickListener{
                val intent = Intent(this@Signin, Signup::class.java)
                startActivity(intent)
            }

        }
    }
}