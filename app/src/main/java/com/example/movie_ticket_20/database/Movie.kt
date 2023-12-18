package com.example.movie_ticket_20.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localmovies")
data class Movie(
    val movieID: String = "",
    @PrimaryKey
    val moviename: String = "",
    val moviedirector :String = "",
    val movierateS: String = "",
    val moviedesc: String = "",
    val movierateR: String = "",
    val movieImage: String = "",
)
