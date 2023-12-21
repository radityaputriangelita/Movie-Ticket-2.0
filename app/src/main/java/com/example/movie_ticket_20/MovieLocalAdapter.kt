package com.example.movie_ticket_20
//movie adapter saat pakai yang room karena dia ga bakal bisa di klik atau di longclik
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.movie_ticket_20.database.Movie

class MovieLocalAdapter(private val movieList: List<Movie>) :
    RecyclerView.Adapter<MovieLocalAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.txt_movie_title)
        var director: TextView = itemView.findViewById(R.id.txt_movie_director)
        var rating: TextView = itemView.findViewById(R.id.int_movie_rateS)
        var batasUmur: TextView = itemView.findViewById(R.id.int_movie_rateR)
        private var movieImage: ImageView = itemView.findViewById(R.id.movie_image)

        fun bind(movie: Movie) {
            title.text = movie.moviename
            director.text = movie.moviedirector
            rating.text = movie.movierateS
            batasUmur.text = movie.movierateR
            Glide.with(itemView)
                .load(movie.movieImage) // tampilin image dari link
                .placeholder(R.drawable.load) // image sementara dia load
                .error(R.drawable.error) // image error atau kosong
                .into(movieImage)
        }
    }

    //buat tampilannya
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieLocalAdapter.MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(itemView)
    }

    //hubungin dia ke tampilanny
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val currentMovie = movieList[position]
        holder.bind(currentMovie)
    }

    //cek jumlah item di list nya
    override fun getItemCount(): Int {
        return movieList.size
    }

}
