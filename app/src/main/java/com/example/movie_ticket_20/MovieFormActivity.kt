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
    // Inisialisasi Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val movieCollectionRef = firestore.collection("movies")
    private lateinit var binding: ActivityMovieFormBinding
    //duplikat movie id pas nambah data
    private var movieId = ""
    //untuk tambah gambar di storage pake url
    private lateinit var imageReference: StorageReference
    private var ImagePath: Uri? = null
    //notifikasi
    private val channelId = "TEST_NOTIFICATION"
    private val notifId = 90


    //akan dicek dulu datanya apakah untuk update atau tambah data.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //notif manager untuk notif
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //inisiasi untuk save imagenya
        imageReference = Firebase.storage.reference

        //terima action type untuk tahu add atau update dan intent movie_id nya juga
        val actionType = intent.getStringExtra("action_type")
        movieId = intent.getStringExtra("movie_id") ?: ""

        //intent kalau btnback
        binding.btnBackFromForm.setOnClickListener {
            finish()
        }
        //nampilin movieimage kalau diklik atau diganti filmnya
        binding.movieImageForm.setOnClickListener {
            // Membuka galeri saat gambar diklik
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            imageLauncher.launch(intent)
        }


        with(binding) {
            //cek usdk untuk notifikasi namanya notifikasi movie
            PendingIntent.FLAG_IMMUTABLE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notifChannel = NotificationChannel(
                    channelId,
                    "Notification Movie",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notifManager.createNotificationChannel(notifChannel)
            }

            //cek kalau type nya add atau update
            if (actionType == "add") {
                //kalau add tampilin btn add nya update nya GONE
                btnaddMovieForm.visibility = View.VISIBLE
                btnupdateMovieForm.visibility = View.GONE

                //fuction kalau di klik btnAddnya
                btnaddMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    //tambahin dia sama waktu biar namanya unik
                    val filename = "movie_${System.currentTimeMillis()}.jpg"

                    //ambil gambarnya, cek dulu null apa engga
                    ImagePath?.let { lastImagePath ->
                        try {
                            //save firebase storage dengan nama file baru yang udah di inisiasikan
                            val imageRef = imageReference.child("movie_images/$filename")
                            //ungguah file gambarny ke fb storage sesuai lashImagePath
                            imageRef.putFile(lastImagePath)
                                .addOnSuccessListener { taskSnapshot ->
                                    //jika upload berhasil
                                    imageRef.downloadUrl
                                        .addOnSuccessListener { uri ->
                                            //ambil downloadUrl dari file yang diupload. Ambil url nya dan objek baru dengan url gambar baru.
                                            val imageURL = uri.toString()
                                            val newMovie = Movie(moviename = title, moviedirector = director, movierateS = star, moviedesc = description, movierateR = rateUmur, movieImage = imageURL
                                            )
                                            //simpan gamabr baru nya.
                                            addMovie(newMovie)
                                        }
                                        .addOnFailureListener {
                                            //error url ga dapet
                                            Log.e("Error", "Error getting URL: $it")
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    //error upload nya
                                    Log.e("Error", "Error on Upload: ${exception.message}")
                                }
                        } catch (e: Exception) {
                            Log.e("Error", "Error: $e")
                        }
                    } ?: run {
                        //error image belom diakses
                        Log.e("Error", "Image not selected")
                    }
                }


            }
            //kalau dia bawa action type update
            else if (actionType == "update") {
                //nampilin btnupdate dan add GONE
                btnaddMovieForm.visibility = View.GONE
                btnupdateMovieForm.visibility = View.VISIBLE

                //nampilin data sebelumnya ke form
                movieCollectionRef.document(movieId)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val movie = documentSnapshot.toObject(Movie::class.java)
                            // ngisi form nya dengan data dari firebase
                            movie?.let {
                                inputMovieTitle.setText(it.moviename)
                                inputMovieDirectore.setText(it.moviedirector)
                                inputMovieRateS.setText(it.movierateS)
                                inputMovieDesc.setText(it.moviedesc)
                                inputMovieRateR.setText(it.movierateR)
                                //nampilin gambarnya pake glide
                                Glide.with(this@MovieFormActivity)
                                    .load(it.movieImage) // url gambar dari firestorenya
                                    .placeholder(R.drawable.load) // Placeholder image sementara
                                    .error(R.drawable.error) // Image kalau gambar error
                                    .into(binding.movieImageForm) // binding dengan image di form
                            }
                        }
                    }
                    .addOnFailureListener { exception -> }

                //saat btnupdate di klik dia akan simpen perubahannya
                btnupdateMovieForm.setOnClickListener {
                    val title = inputMovieTitle.text.toString()
                    val director = inputMovieDirectore.text.toString()
                    val star = inputMovieRateS.text.toString()
                    val description = inputMovieDesc.text.toString()
                    val rateUmur = inputMovieRateR.text.toString()
                    val movieToUpdate = Movie(
                        moviename = title,
                        moviedirector = director,
                        movierateS = star,
                        moviedesc = description,
                        movierateR = rateUmur,
                        //image nya di kosongin dulu
                        movieImage = ""
                    )

                    //cek dulu ada gambar baru apa engga
                    if (ImagePath != null) {
                        try {
                            val imageRef = imageReference.child("movie_images/${System.currentTimeMillis()}.jpg")
                            imageRef.putFile(ImagePath!!)
                                .addOnSuccessListener { taskSnapshot ->
                                    imageRef.downloadUrl
                                        .addOnSuccessListener { uri ->
                                            val imageURL = uri.toString()

                                            //update movienya kalau emang ada gambar baru yang dipilin
                                            val updatedMovie = movieToUpdate.copy(movieImage = imageURL)

                                            // update juga datanya di firestore
                                            updateMovie(updatedMovie)
                                        }
                                        .addOnFailureListener {
                                            //error ga dapet url
                                            Log.e("Error", "Error getting URL: $it")
                                        }
                                }
                                .addOnFailureListener { exception ->
                                    //error uploadnya
                                    Log.e("Error", "Error on Upload: ${exception.message}")
                                }
                        } catch (e: Exception) {
                            Log.e("Error", "Error: $e")
                        }
                    } else {
                        //ga ada gambar baru jadi simpen ulang value image sebelumnya
                        movieCollectionRef.document(movieId)
                            .get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val movie = documentSnapshot.toObject(Movie::class.java)
                                    movie?.let {
                                        //copy value image sebelumnya
                                        val updatedMovie = movieToUpdate.copy(movieImage = it.movieImage)
                                        //update datanya untuk
                                        updateMovie(updatedMovie)
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("MovieFormActivity", "Error fetching document", exception)
                            }
                    }
                }

            }
        }
    }

    private fun addMovie(movie: Movie) {
        //buat data baru tanpa id
        val newMovieRef = movieCollectionRef.document()
        //get id nya
        val newMovieId = newMovieRef.id
        //copy id movie dengan column movieID
        val movieWithID = movie.copy(movieID = newMovieId)

        newMovieRef
            .set(movieWithID) // set datanya seusai movie yang udah ada ID
            .addOnSuccessListener {
                // munculin notifikasi saat udah back ke home setelah berhasil nambah
                val intent = Intent(this@MovieFormActivity, Main::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

                //kelola semua notif di apk
                val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                //untuk membentuk notifnya
                val builder = NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.baseline_notifications_24) //iconnya
                    .setContentTitle("M-TIX") //judulnya
                    .setContentText("Ada Film Baru yang Ditambahkan ❤️") //konten notifnya
                    .setAutoCancel(true) //auto hapus saat di klik
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                //Mengirimkan notifikasi yang dibuat ke NotificationManager sesuai ID nya
                notifManager.notify(notifId, builder.build())
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error adding document", exception)
            }

    }

    //update data movienya
    private fun updateMovie(movie: Movie) {
        //movie yang terupdate akan tetep punya value kolom movieID yang saam
        val movieToUpdate = movie.copy(movieID = movieId)
        //melakukan update sesuai ID movie dan merge dengan data lainnya
        movieCollectionRef.document(movieId).set(movieToUpdate, SetOptions.merge())
            .addOnSuccessListener {
                //intent pindah ke halaman form activity
                val intent = Intent(this@MovieFormActivity, Main::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { exception ->
                Log.d("MovieFormActivity", "Error updating document", exception)
            }
    }

    //fungsi untuk emnampilkan image saat image di pillih walaupun belom di save
    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK){
            result?.data?.data?.let {
                ImagePath = it
                binding.movieImageForm.setImageURI(it)
            }
        }
    }

}
