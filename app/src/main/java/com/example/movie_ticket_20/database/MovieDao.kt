package com.example.movie_ticket_20.database

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MovieDao {
    @Query("SELECT * FROM localmovies")
    fun getAllMovies(): List<Movie>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(localmovies: List<Movie>): List<Long>

    @Query("DELETE FROM localmovies")
    fun deleteAllMovies()
    @Query("SELECT * FROM localmovies WHERE movieID = :id")
    fun getMovieById(id: String): Movie?

    // Pada DAO
    @Transaction
    fun insertMovies(localmovies: List<Movie>) {
        val existingMovies = getAllMovies()
        val moviesToInsert = localmovies.filter { newMovie ->
            existingMovies.none { it.moviename == newMovie.moviename && it.moviedirector == newMovie.moviedirector && it.moviedirector == newMovie.moviedirector } // Memeriksa kombinasi atribut untuk memastikan ketidakterduplikatan
        }
        val insertedRowIds = insertAll(moviesToInsert)
        Log.d("LocalDatabase", "Inserted ${insertedRowIds.size} rows into local database")
    }

}