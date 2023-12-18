package com.example.movie_ticket_20.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.MovieAdapter
import com.example.movie_ticket_20.MovieFormActivity
import com.example.movie_ticket_20.databinding.FragmentListFilmAdminBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class ListFilmAdminFragment : Fragment() {
    private var _binding: FragmentListFilmAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var recyclerView: RecyclerView


    private lateinit var firestore: FirebaseFirestore
    private val movieList: ArrayList<Movie> = ArrayList()
    private lateinit var movieCollectionRef: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListFilmAdminBinding.inflate(inflater, container, false)
        val view = binding.root

        firestore = FirebaseFirestore.getInstance()
        movieCollectionRef = firestore.collection("movies")

        setupRecyclerView()
        setupButtons()

        loadMoviesFromFirestore()

        return view
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rvMovie
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        movieAdapter = MovieAdapter(movieList,
            onItemClick = { selectedMovie ->
                navigateToUpdateMovie(selectedMovie)
            },
            onItemLongClick = { movieToDelete ->
                deleteMovie(movieToDelete)
            }
        )
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

    private fun loadMoviesFromFirestore() {
        movieCollectionRef.get()
            .addOnSuccessListener { documents ->
                movieList.clear()
                for (document in documents) {
                    val movie = document.toObject(Movie::class.java)
                    movieList.add(movie)
                }
                movieAdapter.notifyDataSetChanged() // Update adapter after data retrieval
            }
            .addOnFailureListener { e ->
                // Handle failure
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