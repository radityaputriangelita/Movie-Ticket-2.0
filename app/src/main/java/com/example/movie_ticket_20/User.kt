package com.example.movie_ticket_20

//tabel user isinya username, role dia auto pengguna kalau mau diubah ke admin di firestore dan moviefav bentunya admin
data class User(
    val username: String = "",
    val role: String = "",
    val moviefav: List<String> = emptyList()
)

