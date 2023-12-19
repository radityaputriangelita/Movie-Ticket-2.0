package com.example.movie_ticket_20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.movie_ticket_20.databinding.ActivityDetailBinding
import com.example.movie_ticket_20.databinding.ActivityMovieFormBinding
import com.example.movie_ticket_20.R
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.database.MovieDao
import com.example.movie_ticket_20.database.MovieDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding // Deklarasi binding
    private lateinit var movieId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater) // Inisialisasi binding
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(this).movieDao()

        movieId = intent.getStringExtra("movie_id") ?: ""

        if (isOnline()) {
            loadMovieFromFirebase()
        } else {
            loadMovieFromRoom()
        }

        binding.btnBackFromForm.setOnClickListener {
            onBackPressed()
        }
    }

    private fun isOnline(): Boolean {
        return true // Contoh: selalu kembalikan true untuk sementara
    }

    private fun loadMovieFromFirebase() {
        movieCollectionRef.document(movieId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val movie = documentSnapshot.toObject(Movie::class.java)
                    displayMovieData(movie)
                }
            }
    }

    private fun loadMovieFromRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            val movie = movieDao.getMovieById(movieId)
            withContext(Dispatchers.Main) {
                displayMovieData(movie)
            }
        }
    }

    private fun displayMovieData(movie: Movie?) {
        movie?.let {
            binding.movTitle.text = it.moviename
            binding.movDierector.text = it.moviedirector
            binding.intMovieRateS.text = it.movierateS
            binding.intMovieRateR.text = it.movierateR
            binding.movDesc.text = it.moviedesc
            Glide.with(this)
                .load(it.movieImage) // URL gambar disimpan dalam movieImage di Movie object
                .placeholder(R.drawable.load) // Placeholder jika gambar belum dimuat
                .error(R.drawable.error) // Gambar yang ditampilkan jika terjadi kesalahan
                .into(binding.imageDetail) // ImageView untuk menampilkan gambar
        }
    }
}
