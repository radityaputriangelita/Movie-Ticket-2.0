package com.example.movie_ticket_20.database

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class MovieRepository(private val firestore: FirebaseFirestore, private val movieDao: MovieDao) {

    fun fetchDataFromFirestoreAndSaveToLocal() {
        val query = firestore.collection("movies")

        query.get().addOnSuccessListener { documents ->
            val movieList = mutableListOf<Movie>()
            for (document in documents) {
                val movie = document.toObject<Movie>()
                movieList.add(movie)
            }
            saveMoviesToLocalDatabase(movieList)
        }.addOnFailureListener { exception ->
            // Tangani kesalahan saat mengambil data dari Firestore
        }
    }

    private fun saveMoviesToLocalDatabase(movieList: List<Movie>) {
        movieDao.insertMovies(movieList)
    }

    // ... tambahkan fungsi-fungsi lain yang diperlukan
}