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
import com.example.movie_ticket_20.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
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
//
//    companion object {
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SettingsFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}
