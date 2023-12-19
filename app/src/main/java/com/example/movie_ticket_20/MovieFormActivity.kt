package com.example.movie_ticket_20

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.movie_ticket_20.database.Movie
import com.example.movie_ticket_20.databinding.ActivityMovieFormBinding
import com.example.movie_ticket_20.fragments.ListFilmAdminFragment
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import android.content.Context
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.movie_ticket_20.databinding.ActivityMainBinding

class MovieFormActivity : AppCompatActivity() {
    // Inisialisasi Firestore
    private val firestore = FirebaseFirestore.getInstance()
    private val movieCollectionRef = firestore.collection("movies")
    private lateinit var binding: ActivityMovieFormBinding
    private var movieId = ""
    private lateinit var storage: FirebaseStorage
    private lateinit var imageReference: StorageReference
    private var ImagePath: Uri? = null

    private val channelId = "TEST_NOTIFICATION"
    private val notifId = 90


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        imageReference = Firebase.storage.reference

        val actionType = intent.getStringExtra("action_type")
        movieId = intent.getStringExtra("movie_id") ?: ""

        binding.btnBackFromForm.setOnClickListener {
            finish()
        }
        binding.movieImageForm.setOnClickListener {
            // Membuka galeri saat gambar diklik
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageLauncher.launch(intent)
        }


        with(binding) {
            PendingIntent.FLAG_IMMUTABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("aneh", "bebas")
                val notifChannel = NotificationChannel(
                    channelId,
                    "Notification Movie",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notifManager.createNotificationChannel(notifChannel)
            }


            if (actionType == "add") {
                btnaddMovieForm.visibility = View.VISIBLE
                btnupdateMovieForm.visibility = View.GONE

                btnaddMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    val filename = "movie_${System.currentTimeMillis()}.jpg"

                    ImagePath?.let { lastImagePath ->
                        try {
                            val imageRef = imageReference.child("movie_images/$filename")
                            imageRef.putFile(lastImagePath)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl
                                        .addOnSuccessListener { uri ->
                                            val imageURL = uri.toString()
                                            val newMovie = Movie(moviename = title, moviedirector = director, movierateS = star, moviedesc = description, movierateR = rateUmur, movieImage = imageURL
                                            )
                                            addMovie(newMovie)
                                        }
                                        .addOnFailureListener {
                                            Log.e("Error", "Error getting URL: $it")
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Error", "Error on Upload: ${exception.message}")
                                }
                        } catch (e: Exception) {
                            Log.e("Error", "Error: $e")
                        }
                    } ?: run {
                        Log.e("Error", "Image not selected")
                    }
                }


            } else if (actionType == "update") {
                btnaddMovieForm.visibility = View.GONE
                btnupdateMovieForm.visibility = View.VISIBLE

                movieCollectionRef.document(movieId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val movie = documentSnapshot.toObject(Movie::class.java)
                            // Mengisi formulir dengan data film yang diambil dari Firestore
                            movie?.let {
                                inputMovieTitle.setText(it.moviename)
                                inputMovieDirectore.setText(it.moviedirector)
                                inputMovieRateS.setText(it.movierateS)
                                inputMovieDesc.setText(it.moviedesc)
                                inputMovieRateR.setText(it.movierateR)

                                Glide.with(this@MovieFormActivity)
                                    .load(it.movieImage) // URL gambar sebelumnya dari Firestore
                                    .placeholder(R.drawable.load) // Placeholder image
                                    .error(R.drawable.error) // Image on error
                                    .into(binding.movieImageForm)
                            }
                        } else {
                            // Handle jika data tidak ditemukan
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle error ketika mengambil data dari Firestore
                    }

                btnupdateMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    val movieToUpdate = Movie(moviename = title, moviedirector = director, movierateS = star, moviedesc = description, movierateR = rateUmur)
                    ImagePath?.let { lastImagePath ->
                        try {
                            val imageRef = imageReference.child("movie_images/${System.currentTimeMillis()}.jpg")
                            imageRef.putFile(lastImagePath)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl
                                        .addOnSuccessListener { uri ->
                                            val imageURL = uri.toString()

                                            // Memperbarui movieImage jika ada gambar baru diunggah
                                            val updatedMovie = if (imageURL.isNotEmpty()) {
                                                movieToUpdate.copy(movieImage = imageURL)
                                            } else {
                                                movieToUpdate // Jika tidak ada gambar baru, tetap menggunakan movieImage sebelumnya
                                            }

                                            // Update data film di Firestore
                                            updateMovie(updatedMovie)
                                        }
                                        .addOnFailureListener {
                                            Log.e("Error", "Error getting URL: $it")
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Error", "Error on Upload: ${exception.message}")
                                }
                        } catch (e: Exception) {
                            Log.e("Error", "Error: $e")
                        }
                    } ?: run {
                        Log.e("Error", "Image not selected")
                    }
                }
            }
        }
    }

    private fun addMovie(movie: Movie) {
        val newMovieRef = movieCollectionRef.document() // Create a new document reference without an ID
        val newMovieId = newMovieRef.id // Get the ID

        val movieWithID = movie.copy(movieID = newMovieId) // Assign the new ID to movieID

        newMovieRef
            .set(movieWithID) // Set data film with the updated movieID
            .addOnSuccessListener {
                // Setelah notifikasi ditampilkan, kembali ke halaman utama
                val intent = Intent(this@MovieFormActivity, Main::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

                val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                val builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.baseline_notifications_24)
                    .setContentTitle("M-TIX")
                    .setContentText("Ada Film Baru yang Ditambahkan ❤️")
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                notifManager.notify(notifId, builder.build())
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error adding document", exception)
            }

    }




    private fun updateMovie(movie: Movie) {
        val movieToUpdate = movie.copy(movieID = movieId)
        movieCollectionRef.document(movieId).set(movieToUpdate, SetOptions.merge())
            .addOnSuccessListener {
                val intent = Intent(this@MovieFormActivity, ListFilmAdminFragment::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error updating document", exception)
            }
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK){
            result?.data?.data?.let {
                ImagePath = it
                binding.movieImageForm.setImageURI(it)
            }
        }else{
            Toast.makeText(this, "canceled", Toast.LENGTH_SHORT).show()
        }
    }

}
