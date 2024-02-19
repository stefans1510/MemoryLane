package com.example.memorylane.models

data class PlaceModel(
    val id: Int,
    val creatorId: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
)
