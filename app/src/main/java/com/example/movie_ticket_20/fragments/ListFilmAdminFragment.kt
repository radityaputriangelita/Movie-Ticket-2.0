package com.example.movie_ticket_20.fragments

import android.content.Intent
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.MovieAdapter
import com.example.movie_ticket_20.MovieFormActivity
import com.example.movie_ticket_20.MovieLocalAdapter
import com.example.movie_ticket_20.database.MovieDao
import com.example.movie_ticket_20.database.MovieDatabase
import com.example.movie_ticket_20.databinding.FragmentListFilmAdminBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ListFilmAdminFragment : Fragment() {
    private var _binding: FragmentListFilmAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView


    private lateinit var firestore: FirebaseFirestore
    private val movieList: ArrayList<Movie> = ArrayList()
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFilmAdminBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()
        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()


        setupRecyclerView()
        setupButtons()

        if (isOnline(requireContext())) {
            loadMoviesFromFirestore()
        } else {
            displayLocalMovies()
        }
        return view
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rvMovie
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = MovieAdapter(movieList,
            onItemClick = { selectedMovie ->
                navigateToUpdateMovie(selectedMovie)
            }
        ) { movieToDelete ->
            deleteMovie(movieToDelete)
        }
        recyclerView.adapter = movieAdapter
    }

    private fun setupButtons() {
        binding.btnTambah.setOnClickListener {
            navigateToAddMovie()
        }
    }

    private fun navigateToAddMovie() {
        val intent = Intent(requireContext(), MovieFormActivity::class.java)
        intent.putExtra("action_type", "add")
        startActivity(intent)
    }

    private fun navigateToUpdateMovie(movie: Movie) {
        val intent = Intent(requireContext(), MovieFormActivity::class.java)
        intent.putExtra("action_type", "update")
        intent.putExtra("movie_id", movie.movieID) // or any other necessary data
        startActivity(intent)
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun loadMoviesFromFirestore() {
        movieCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle error
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val movieListFirestore = mutableListOf<Movie>()
                for (document in snapshot) {
                    val movie = document.toObject(Movie::class.java)
                    movieListFirestore.add(movie)
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val movieEntities = movieListFirestore.map { movie ->
                        Movie(
                            movieID = movie.movieID,
                            moviename = movie.moviename,
                            moviedirector = movie.moviedirector,
                            movierateS = movie.movierateS,
                            moviedesc = movie.moviedesc,
                            movierateR = movie.movierateR,
                            movieImage = movie.movieImage
                        )
                    }

                    // Delete all existing data in Room Database
                    movieDao.deleteAllMovies()

                    // Insert new data from Firestore to Room Database
                    movieDao.insertMovies(movieEntities)

                    withContext(Dispatchers.Main) {
                        // Update adapter from data fetched from Firestore
                        movieAdapter = MovieAdapter(movieEntities,
                            onItemClick = { selectedMovie ->
                                navigateToUpdateMovie(selectedMovie)
                            }
                        ) { movieToDelete ->
                            deleteMovie(movieToDelete)
                        }
                        recyclerView.adapter = movieAdapter
                    }
                }
            }
        }
    }




    private fun displayLocalMovies() {
        CoroutineScope(Dispatchers.IO).launch {
            val movieList = movieDao.getAllMovies()
            Log.d("LocalDatabase", "Retrieved ${movieList.size} rows from local database")
            withContext(Dispatchers.Main) {
                val localAdapter = MovieLocalAdapter(movieList)
                recyclerView.adapter = localAdapter
            }
        }
    }

    private fun deleteMovie(movie: Movie) {
        movieCollectionRef.document(movie.movieID)
            .delete()
            .addOnSuccessListener {
                movieList.remove(movie)
                movieAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}