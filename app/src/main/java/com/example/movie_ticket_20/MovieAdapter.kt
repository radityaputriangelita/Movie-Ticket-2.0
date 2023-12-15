package com.example.movie_ticket_20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.Movie
import java.util.ArrayList

class MovieAdapter(private val movieList: ArrayList<Movie>, private val onItemClick: (Movie) -> Unit,
                   private val onItemLongClick: (Movie) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.txt_movie_title)
        var director: TextView = itemView.findViewById(R.id.txt_movie_director)
        var rating: TextView = itemView.findViewById(R.id.int_movie_rateS)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(movieList[position])
                }
            }

            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(movieList[position])
                    true
                } else {
                    false
                }
            }
        }
        fun bind(movie: Movie) {
            title.text = movie.moviename
            director.text = movie.moviedirector
            rating.text = movie.movierateS.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return movieList.size
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val currentMovie = movieList[position]
        holder.title.text = currentMovie.moviename
        holder.director.text = currentMovie.moviedirector
        holder.rating.text = currentMovie.movierateS.toString()
    }
}

