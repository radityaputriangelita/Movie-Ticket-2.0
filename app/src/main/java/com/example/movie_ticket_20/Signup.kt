package com.example.movie_ticket_20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.movie_ticket_20.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Signup : AppCompatActivity() {
    private lateinit var binding : ActivitySignupBinding
    //firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    //simpen datanya ke firestore user juga
    private val userCollection = firestore.collection("user")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //inialisasi firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        with(binding){
            //saat btn regis di klik
            btnRegist.setOnClickListener{
                val username = binding.usernameInputEdittext.text.toString()
                val email = binding.emailInputEdittext.text.toString()
                val pass = binding.passwordInputEdittext.text.toString()
                //cek ga boleh kosong
                if (username.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    //tambah user di form untuk auth
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            //get user uid karena datanya mau ditaro ke firestore sekalian jadi biar mereka punya id yang sama dan kehubung
                            val user = firebaseAuth.currentUser
                            val uid = user?.uid
                            //role auto jadi jadi 'pengguna' kalau mau jadi admin cuma bisa diganti kalau diupdate dari firebasenya
                            val newUser = User(username, "pengguna")

                            //buat kalo ada buat simpen
                            uid?.let {
                                userCollection.document(it).set(newUser)
                                    .addOnSuccessListener {
                                        // simpan firestore
                                        val intent = Intent(this@Signup, Signin::class.java)
                                        startActivity(intent)
                                    }
                                    .addOnFailureListener { e ->
                                        // simpan firestore gagal
                                        Toast.makeText(this@Signup, "Gagal menyimpan data pengguna: $e", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        } else {
                            //kalo gagal dimpen di auth
                            Toast.makeText(this@Signup, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    //input formnya belom lengkap
                    Toast.makeText(this@Signup, "Lengkapi form untuk mendaftar", Toast.LENGTH_SHORT).show()
                }
            }
            //pindah ke signin activity
            signin.setOnClickListener{
                val intent = Intent(this@Signup, Signin::class.java)
                startActivity(intent)
            }
        }
    }
}