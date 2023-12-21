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
    //binding adapter sama recycle
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView
    //bind firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    //inisiasi movieList
    private val movieList: ArrayList<Movie> = ArrayList()
    //movie saat dia offline
    private lateinit var movieCollectionRef: CollectionReference
    private lateinit var movieDao: MovieDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //bindingnya
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        val view = binding.root
        //inisiasi firebase lagi
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //inisasi kalau dia offline
        movieCollectionRef = firestore.collection("movies")
        movieDao = MovieDatabase.getDatabase(requireContext()).movieDao()

        //setup hubungan recycle viewnya
        setupRecyclerView()
        //nampilin data favorite
        getFavoriteMovies()

        return view
    }

    //menyampaikan recycle view nampilin data nya dia
    private fun setupRecyclerView() {
        recyclerView = binding.rvMovie
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = MovieAdapter(movieList,
            onItemClick = { selectedMovie ->
                //kalo di klik ke detail
                navigateToDetailMovie(selectedMovie)
            },
            onItemLongClick = { selectedMovie ->
                //kalo di longclik dia hapus dari favorite
                deleteFavoriteMovie(selectedMovie)
            }
        )
        recyclerView.adapter = movieAdapter
    }

    //ambil data movie fav dari firebase
    private fun getFavoriteMovies() {
        //datanya ada di kolom user jadi cek dulu user yang login di firebaseauth
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            //ambil uid nya
            val userRef = firestore.collection("user").document(uid)

            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    //kalo dokumen ada isinya
                    val user = document.toObject(User::class.java)
                    user?.let {
                        //inisiasi movienav lagi
                        val favoriteMovieIds = it.moviefav
                        //ubah array jadi list gitu
                        val tasks = mutableListOf<Task<DocumentSnapshot>>()
                        //pake for loop untuk menampilkan datanya dia karena ada beberpa (bentuk dia array isi ID MOVIE)
                        for (movieId in favoriteMovieIds) {
                            val task = movieCollectionRef.document(movieId).get()
                            tasks.add(task)
                        }

                        //mengkonversi objek menjadi movie dan tambahin jadi daftar film fav
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

    //intent ke detail pas di klik dimana datanya sesuai dengan movie_id jadi dia di intent
    private fun navigateToDetailMovie(movie: Movie) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("movie_id", movie.movieID)
        startActivity(intent)
    }

    //hapus movie tertentu dari favorite
    private fun deleteFavoriteMovie(movie: Movie) {
        //cek user dulu pake auth
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val uid = user.uid
            val userRef = firestore.collection("user").document(uid)
            //get user collectionnya dia di tabel user pake uid
            userRef.get().addOnSuccessListener { documentSnapshot ->
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    //ambil array moviefav
                    val favoriteMovies = it.moviefav as ArrayList<String>
                    //kalau ID movie yang kita longclik berada di dalam list array
                    if (favoriteMovies.contains(movie.movieID)) {
                        //hapus movie ID dari array
                        favoriteMovies.remove(movie.movieID)
                        //upadate data moviefav dengan yang sudah dihapus
                        userRef.update("moviefav", favoriteMovies)
                            .addOnSuccessListener {
                                // dihilangin tampilan dari movie yang di klik lama
                                movieList.removeAll { it.movieID == movie.movieID }
                                movieAdapter.notifyDataSetChanged()
                                //bikin toast kasih tau data terhapus dari favorite
                                Toast.makeText(context, "Movie deleted from favorites", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                            }
                    }
                }
            }.addOnFailureListener { e ->
            }
        }
    }

    private fun updateFavoriteMoviesUI(favoriteMovies: List<Movie>) {
        //bersihin daftar movie yang ditampilin
        movieList.clear()
        //update datanya dengan favoritemovie terbaru
        movieList.addAll(favoriteMovies)
        movieAdapter.notifyDataSetChanged()
    }
}