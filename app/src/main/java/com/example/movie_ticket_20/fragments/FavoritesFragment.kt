package com.example.movie_ticket_20.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.DetailActivity
import com.example.movie_ticket_20.MovieAdapter
import com.example.movie_ticket_20.MovieLocalAdapterUser
import com.example.movie_ticket_20.R
import com.example.movie_ticket_20.User
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.database.MovieDao
import com.example.movie_ticket_20.database.MovieDatabase
import com.example.movie_ticket_20.databinding.FragmentFavoritesBinding
import com.example.movie_ticket_20.databinding.FragmentHomeBinding
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val movieList: ArrayList<Movie> = ArrayList()
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()

        setupRecyclerView()

        // Get favorites of the current user and display them
        getFavoriteMovies()

        return view
    }
    private fun setupRecyclerView() {
        recyclerView = binding.rvMovie
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = MovieAdapter(movieList,
            onItemClick = { selectedMovie ->
                navigateToDetailMovie(selectedMovie)
            },
            onItemLongClick = { selectedMovie ->
                deleteFavoriteMovie(selectedMovie) // Panggil fungsi deleteFavoriteMovie di sini
            }
        )
        recyclerView.adapter = movieAdapter
    }

    private fun getFavoriteMovies() {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val userRef = firestore.collection("user").document(uid)

            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        val favoriteMovieIds = it.moviefav

                        val tasks = mutableListOf<Task<DocumentSnapshot>>()

                        for (movieId in favoriteMovieIds) {
                            val task = movieCollectionRef.document(movieId).get()
                            tasks.add(task)
                        }

                        Tasks.whenAllSuccess<DocumentSnapshot>(tasks)
                            .addOnSuccessListener { taskSnapshots ->
                                val favoriteMovies = mutableListOf<Movie>()

                                for (snapshot in taskSnapshots) {
                                    val movie = snapshot.toObject(Movie::class.java)
                                    movie?.let {
                                        favoriteMovies.add(it)
                                    }
                                }

                                updateFavoriteMoviesUI(favoriteMovies)
                            }
                            .addOnFailureListener { e ->
                                // Handle failure in fetching favorite movies
                            }
                    }
                }
            }.addOnFailureListener { e ->
                // Handle failure in getting user data
            }
        }
    }

    private fun navigateToDetailMovie(movie: Movie) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("movie_id", movie.movieID)
        startActivity(intent)
    }
    private fun deleteFavoriteMovie(movie: Movie) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val userRef = firestore.collection("user").document(uid)

            userRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    val favoriteMovies = it.moviefav as ArrayList<String>

                    if (favoriteMovies.contains(movie.movieID)) {
                        favoriteMovies.remove(movie.movieID)

                        userRef.update("moviefav", favoriteMovies)
                            .addOnSuccessListener {
                                // Remove the clicked movie from the displayed UI
                                movieList.removeAll { it.movieID == movie.movieID }
                                movieAdapter.notifyDataSetChanged()
                                Toast.makeText(context, "Movie deleted from favorites", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle failure in updating moviefav array
                            }
                    }
                }
            }.addOnFailureListener { e ->
                // Handle failure in getting user data
            }
        }
    }


    private fun updateFavoriteMoviesUI(favoriteMovies: List<Movie>) {
        movieList.clear()
        movieList.addAll(favoriteMovies)
        movieAdapter.notifyDataSetChanged()
    }
}