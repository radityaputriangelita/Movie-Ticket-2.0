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
    //binding
    private var _binding: FragmentListFilmAdminBinding? = null
    private val binding get() = _binding!!
    //adapter dan recycle view
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView
    //firestore
    private lateinit var firestore: FirebaseFirestore
    private val movieList: ArrayList<Movie> = ArrayList()
    //offline
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding
        _binding = FragmentListFilmAdminBinding.inflate(inflater, container, false)
        val view = binding.root
        //firestore
        firestore = FirebaseFirestore.getInstance()
        //offline
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

    //set up kalau dia di klik atau klik lama
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

    //setup btn  nya jadi
    private fun setupButtons() {
        //online bisa nambahin
        if (isOnline(requireContext())) {
            binding.btnTambah.setOnClickListener {
                navigateToAddMovie()
            }
        } else {
            // nonaktif btn
            binding.btnTambah.isEnabled = false
        }
    }


    //navigate ke form
    private fun navigateToAddMovie() {
        val intent = Intent(requireContext(), MovieFormActivity::class.java)
        //dengan action type nya dd
        intent.putExtra("action_type", "add")
        startActivity(intent)
    }

    //navigate untuk update
    private fun navigateToUpdateMovie(movie: Movie) {
        val intent = Intent(requireContext(), MovieFormActivity::class.java)
        //bawa movie id dia juga action type update
        intent.putExtra("action_type", "update")
        intent.putExtra("movie_id", movie.movieID)
        startActivity(intent)
    }

    //cek online atau offline
    private fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun loadMoviesFromFirestore() {
        //cek dia ada apa engga
        movieCollectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) { return@addSnapshotListener}
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

    //nampilin data dari local movie nya
    private fun displayLocalMovies() {
        //buat ngubah data di databse
        CoroutineScope(Dispatchers.IO).launch {
            val movieList = movieDao.getAllMovies()
            Log.d("LocalDatabase", "Retrieved ${movieList.size} rows from local database")
            withContext(Dispatchers.Main) {
                val localAdapter = MovieLocalAdapter(movieList)
                recyclerView.adapter = localAdapter
            }
        }
    }

    //hapus movie
    private fun deleteMovie(movie: Movie) {
        //cek uid movie itu
        movieCollectionRef.document(movie.movieID)
            .delete()
            .addOnSuccessListener {
                //hapus dari list
                movieList.remove(movie)
                movieAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
            }
    }

    //bagian untuk lifecycle fragment
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}