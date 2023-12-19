import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.movie_ticket_20.R
import com.example.movie_ticket_20.Signin
import com.example.movie_ticket_20.databinding.FragmentHomeAdminBinding
import com.example.movie_ticket_20.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("user").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        binding.btnLogoutPengguna.setOnClickListener {
            logout()
        }
    }

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
        requireActivity().finish() // Selesaikan activity MainActivity agar tidak kembali saat tombol back ditekan
    }
}
