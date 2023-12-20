package com.example.movie_ticket_20.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.DetailActivity
import com.example.movie_ticket_20.MovieAdapter
import com.example.movie_ticket_20.MovieLocalAdapter
import com.example.movie_ticket_20.MovieLocalAdapterUser
import com.example.movie_ticket_20.User
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.database.MovieDao
import com.example.movie_ticket_20.database.MovieDatabase
import com.example.movie_ticket_20.databinding.FragmentHomeBinding
import com.example.movie_ticket_20.databinding.FragmentListFilmAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val movieList: ArrayList<Movie> = ArrayList()
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            firestore.collection("user").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        binding.usernameHome.text = username
                    }
                }
        }

        setupRecyclerView()


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
                navigateToDetailMovie(selectedMovie)
            },
            onItemLongClick = { movieToFav ->
            }
        )
        recyclerView.adapter = movieAdapter
    }

    private fun navigateToDetailMovie(movie: Movie) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("movie_id", movie.movieID)
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
                                navigateToDetailMovie(selectedMovie)
                            }
                        ) { selectedMovie ->
                            loveMovie(selectedMovie)
                        }
                        recyclerView.adapter = movieAdapter
                    }
                }
            }
        }
    }

    private fun displayLocalMovies() {
        CoroutineScope(Dispatchers.IO).launch {
            val localMovies = movieDao.getAllMovies()
            Log.d("LocalDatabase", "Retrieved ${localMovies.size} rows from local database")

            withContext(Dispatchers.Main) {
                val localAdapter = MovieLocalAdapterUser(localMovies,
                    onItemClick = { selectedMovie ->
                        navigateToDetailMovie(selectedMovie)
                    }
                )
                recyclerView.adapter = localAdapter
            }
        }
    }

    private fun loveMovie(movie: Movie) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val userRef = firestore.collection("user").document(uid)

            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        val updatedFavMovies = it.moviefav.toMutableList()
                        if (!updatedFavMovies.contains(movie.movieID)) {
                            updatedFavMovies.add(movie.movieID)
                            val updatedUser = User(it.username, it.role, updatedFavMovies)
                            userRef.set(updatedUser)
                                .addOnSuccessListener {
                                    // Update local UI or perform any necessary actions upon successful update
                                    // You might want to inform the user that the movie was added to favorites
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure in updating user data
                                }
                        } else {
                            // The movie is already in favorites, you might want to inform the user
                        }
                    }
                }
            }.addOnFailureListener { e ->
                // Handle failure in getting user data
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
