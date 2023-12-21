package com.example.movie_ticket_20

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movie_ticket_20.database.Movie

class MovieLocalAdapterUser(
    private val movieList: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieLocalAdapterUser.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.txt_movie_title)
        var director: TextView = itemView.findViewById(R.id.txt_movie_director)
        var rating: TextView = itemView.findViewById(R.id.int_movie_rateS)
        var batasUmur: TextView = itemView.findViewById(R.id.int_movie_rateR)
        private var movieImage: ImageView = itemView.findViewById(R.id.movie_image)

        //bind data yang ditampilin
        fun bind(movie: Movie) {
            title.text = movie.moviename
            director.text = movie.moviedirector
            rating.text = movie.movierateS
            batasUmur.text = movie.movierateR
            Glide.with(itemView)
                .load(movie.movieImage) // data dari firebase
                .placeholder(R.drawable.load) // data sementar loading
                .error(R.drawable.error) // image kalau kosong jadi error
                .into(movieImage)

            //dia bisa di klik
            itemView.setOnClickListener {
                onItemClick(movie)
            }
        }
    }

    //buat tampilin untuk nampung viewnya
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieLocalAdapterUser.MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(itemView)
    }

    //hubungin data dengan tampilannya
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val currentMovie = movieList[position]
        holder.bind(currentMovie)
    }

    //jumlah data yang ada di list
    override fun getItemCount(): Int {
        return movieList.size
    }

}