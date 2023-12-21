package com.example.movie_ticket_20
//kelola item dalam recycleview dan menampilkan data di film movieList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.movie_ticket_20.database.Movie
import java.util.ArrayList
import com.bumptech.glide.Glide

class MovieAdapter(private val movieList: List<Movie>, private val onItemClick: (Movie) -> Unit,
                   private val onItemLongClick: (Movie) -> Unit) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.txt_movie_title)
        var director: TextView = itemView.findViewById(R.id.txt_movie_director)
        var rating: TextView = itemView.findViewById(R.id.int_movie_rateS)
        var batasUmur: TextView = itemView.findViewById(R.id.int_movie_rateR)
        var movieImage: ImageView = itemView.findViewById(R.id.movie_image)

        //inisialisasi agar recycle view dapat di klik dan di klik lama
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
        //menghubungkan objek Movie dengan tampilan viewholder
        fun bind(movie: Movie) {
            title.text = movie.moviename
            director.text = movie.moviedirector
            rating.text = movie.movierateS.toString()
            batasUmur.text = movie.movierateR.toString()
            Glide.with(itemView)
                .load(movie.movieImage) // Assuming movieImage is the URL in your Movie data class
                .placeholder(R.drawable.load) // Placeholder image while loading
                .error(R.drawable.error) // Image to show in case of error loading
                .into(movieImage)
        }
    }

    //mengembalikan tampilan objek MovieViewHolder yang berisi tataletaknya
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(itemView)
    }

    //jumlah item dalam movieList
    override fun getItemCount(): Int {
        return movieList.size
    }

    //menampilkan data posisi tertentu di recycleview  dan manggil movieViwHolder
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val currentMovie = movieList[position]
        holder.bind(currentMovie)
    }

}

