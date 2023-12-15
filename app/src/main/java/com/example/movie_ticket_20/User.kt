package com.example.movie_ticket_20

data class User(
    val username: String = "",
    val role: String = "",
    val moviefav: List<String> = emptyList()
)

