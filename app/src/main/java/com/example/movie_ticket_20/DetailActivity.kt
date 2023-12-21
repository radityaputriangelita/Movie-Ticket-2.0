package com.example.movie_ticket_20

import android.content.Context
import android.net.ConnectivityManager
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
    private lateinit var binding: ActivityDetailBinding
    private lateinit var movieId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //untuk online
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        movieCollectionRef = firestore.collection("movies")
        //movie dao untuk offline
        movieDao = MovieDatabase.getDatabase(this).movieDao()

        movieId = intent.getStringExtra("movie_id") ?: ""
        //cek online atau engga
        if (isOnline(getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)) {
            loadMovieFromFirebase()
        } else {
            loadMovieFromRoom()
        }
        //btnback ke list
        binding.btnBackFromForm.setOnClickListener {
            onBackPressed()
        }
    }

    //cekonline function onlin e
    private fun isOnline(connectivityManager: ConnectivityManager): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    //mengambil data movie dari firebase berdasarkan ID nya.
    private fun loadMovieFromFirebase() {
        movieCollectionRef.document(movieId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val movie = documentSnapshot.toObject(Movie::class.java)
                    // kalau ada nanti di display
                    displayMovieData(movie)
                }
            }
    }

    //load data movie dari room berdasarkan ID dan pake CoroutineScope untuk ambil data dari Dao
    private fun loadMovieFromRoom() {
        CoroutineScope(Dispatchers.IO).launch {
            val movie = movieDao.getMovieById(movieId)
            withContext(Dispatchers.Main) {
                //nampilin pake displayMovieData juga
                displayMovieData(movie)
            }
        }
    }

    //nampilin data film ke antar muka di binding sama id yang ada di item_movie.xml nampilin gambar pake glide karena dia link
    private fun displayMovieData(movie: Movie?) {
        movie?.let {
            binding.movTitle.text = it.moviename
            binding.movDierector.text = it.moviedirector
            binding.intMovieRateS.text = it.movierateS
            binding.intMovieRateR.text = it.movierateR
            binding.movDesc.text = it.moviedesc
            Glide.with(this)
                .load(it.movieImage) // nampilin gambar sesuai link yang di simpen
                .placeholder(R.drawable.load) // gambar placeholder sementara sembari gambar asli di load
                .error(R.drawable.error) // gambar error yang ditampilkan saat ada kesalahan
                .into(binding.imageDetail) // ditampilinnya di xml yang punya id emageDetail
        }
    }
}
