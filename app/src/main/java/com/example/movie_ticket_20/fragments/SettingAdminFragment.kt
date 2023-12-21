package com.example.movie_ticket_20.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.movie_ticket_20.Signin
import com.example.movie_ticket_20.databinding.FragmentHomeAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingAdminFragment : Fragment() {
    //binding
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!
    //firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    //sharedpreferences
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        val view = binding.root
        //firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //cek user
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            //ambil uid nya
            val uid = user.uid
            firestore.collection("user").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        //hubungin data dari firebase dengan tampilannya
                        val username = document.getString("username")
                        val email = user.email
                        val role = document.getString("role")
                        binding.usernameXml.text = username
                        binding.emailXml.text = email
                        binding.roleXml.text = role
                    }
                }
        }
        return view
    }

    //bagian untuk lifecycle fragmen
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //dipanggil saat tampilan dibuat
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //firebasenya
        firebaseAuth = FirebaseAuth.getInstance()
        //shared preferences nya
        sharedPreferences = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)
        //binding kalau dia btnlogout jalanin logout
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    //kode logout
    private fun logout() {
        // Hapus status login dari SharedPreferences
        val autologin = sharedPreferences.edit()
        autologin.putBoolean("isLogIn", false)
        autologin.apply()

        // Logout dari Firebase Auth
        firebaseAuth.signOut()

        // Pindah ke Signin Activity
        val intent = Intent(requireContext(), Signin::class.java)
        startActivity(intent)
        requireActivity().finish()
        // Selesaikan activity MainActivity agar tidak kembali saat tombol back ditekan
    }
}
