package com.example.movie_ticket_20

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.movie_ticket_20.databinding.ActivityMovieFormBinding
import com.example.movie_ticket_20.fragments.ListFilmAdminFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MovieFormActivity : AppCompatActivity() {
    // Inisialisasi Firestore
    private val firestore = FirebaseFirestore.getInstance()
    private val movieCollectionRef = firestore.collection("movies")
    private lateinit var binding: ActivityMovieFormBinding
    private var movieId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieTitle = intent.getStringExtra("movie_title")
        val movieDescription = intent.getStringExtra("movie_description")
        val actionType = intent.getStringExtra("action_type")
        movieId = intent.getStringExtra("movie_id") ?: ""

        binding.btnBackFromForm.setOnClickListener {
            finish() // Kembali ke fragment sebelumnya
        }

        with(binding) {
            if (actionType == "add") {
                btnaddMovieForm.visibility = View.VISIBLE
                btnupdateMovieForm.visibility = View.GONE

                btnaddMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    val newMovie = Movie(moviename = title, moviedirector = director, movierateS = star, moviedesc = description, movierateR = rateUmur)
                    addMovie(newMovie)
                }
            } else if (actionType == "update") {
                btnaddMovieForm.visibility = View.GONE
                btnupdateMovieForm.visibility = View.VISIBLE

                movieCollectionRef.document(movieId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val movie = documentSnapshot.toObject(Movie::class.java)
                            // Mengisi formulir dengan data film yang diambil dari Firestore
                            movie?.let {
                                inputMovieTitle.setText(it.moviename)
                                inputMovieDirectore.setText(it.moviedirector)
                                inputMovieRateS.setText(it.movierateS)
                                inputMovieDesc.setText(it.moviedesc)
                                inputMovieRateR.setText(it.movierateR)
                            }
                        } else {
                            // Handle jika data tidak ditemukan
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle error ketika mengambil data dari Firestore
                    }

                btnupdateMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    val movieToUpdate = Movie(moviename = title, moviedirector = director, movierateS = star, moviedesc = description, movierateR = rateUmur)
                    updateMovie(movieToUpdate)
                }
            }
        }
    }

    private fun addMovie(movie: Movie) {
        val newMovieRef = movieCollectionRef.document() // Create a new document reference without an ID
        val newMovieId = newMovieRef.id // Get the ID

        val movieWithID = movie.copy(movieID = newMovieId) // Assign the new ID to movieID

        newMovieRef
            .set(movieWithID) // Set data film with the updated movieID
            .addOnSuccessListener {
                val intent = Intent(this@MovieFormActivity, ListFilmAdminFragment::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error adding document", exception)
            }
    }



    private fun updateMovie(movie: Movie) {
        val movieToUpdate = movie.copy(movieID = movieId)
        movieCollectionRef.document(movieId).set(movieToUpdate, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(this@MovieFormActivity, ListFilmAdminFragment::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error updating document", exception)
            }
    }

}
