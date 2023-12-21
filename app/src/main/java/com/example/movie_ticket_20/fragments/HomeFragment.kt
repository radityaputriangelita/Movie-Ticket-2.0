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
import android.widget.Toast
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
    //binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    //adapter recycle view
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView
    //firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    //movie bentuk list
    private val movieList: ArrayList<Movie> = ArrayList()
    //offline data
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        //inisiasi firestore
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //inisiasi offline
        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()

        val currentUser = firebaseAuth.currentUser
        //get user sekarang
        currentUser?.let { user ->
            val uid = user.uid
            //ambil uid nya
            firestore.collection("user").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        //kalau data ga null
                        val username = document.getString("username")
                        //binding teks username diatas biar sesuai username
                        binding.usernameHome.text = username
                    }
                }
        }
        //set recycle viewnya
        setupRecyclerView()

        //cek online apa engga
        if (isOnline(requireContext())) {
            loadMoviesFromFirestore()
        } else {
            //display dari local
            displayLocalMovies()
        }

        return view
    }

    //cek bentuk online
    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    //set up kalau dia di klik atau klik lama
    private fun setupRecyclerView() {
        recyclerView = binding.rvMovie
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = MovieAdapter(movieList,
            onItemClick = { selectedMovie ->
                navigateToDetailMovie(selectedMovie)
            },
            onItemLongClick = { movieToFav ->
                loveMovie(movieToFav)
            }
        )
        recyclerView.adapter = movieAdapter
    }

    //navigate ke detail movie
    private fun navigateToDetailMovie(movie: Movie) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("movie_id", movie.movieID)
        startActivity(intent)
    }

    //load movie dari firestore
    private fun loadMoviesFromFirestore() {
        //cek dia ada apa engga
        movieCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) { return@addSnapshotListener }
            if (snapshot != null) {
                //cek dari list di firebase
                val movieListFirestore = mutableListOf<Movie>()
                //nampilin pake for loop
                for (document in snapshot) {
                    val movie = document.toObject(Movie::class.java)
                    movieListFirestore.add(movie)
                }

                //untuk input output ke databasenya
                CoroutineScope(Dispatchers.IO).launch {
                    //isinya nanti semua datanya yang dibawah di get dari firebase
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

                    // Hapus data yang ada di ROOM
                    movieDao.deleteAllMovies()

                    // isi ROOM dengan data baru dari firebase
                    movieDao.insertMovies(movieEntities)

                    withContext(Dispatchers.Main) {
                        // update data barunya
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

    //nampilin data dari local movie nya
    private fun displayLocalMovies() {
        //buat ngubah data di databse
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


    //masukin movie ke fav
    private fun loveMovie(movie: Movie) {
        //cek user yang login pake firebase auth
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val userRef = firestore.collection("user").document(uid)

            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        //get list movie fav user
                        val updatedFavMovies = it.moviefav.toMutableList()
                        //kalau ada ID movie ga ada di fav maka ditambahin
                        if (!updatedFavMovies.contains(movie.movieID)) {
                            updatedFavMovies.add(movie.movieID)
                            val updatedUser = User(it.username, it.role, updatedFavMovies)
                            //update nambahin ID movie ke list moviefav
                            userRef.set(updatedUser)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Movie added to favorites", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                }
                        } else {
                            Toast.makeText(context, "Movie already in favorites", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }.addOnFailureListener { e ->
            }
        }
    }

    //bagian untuk lifecycle fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
