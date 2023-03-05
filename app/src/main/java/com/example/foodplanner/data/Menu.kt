package com.example.foodplanner.data

import java.util.*

data class Menu(
    val id: String,
    val date: Date,
    val meals: List<Meal>,
    val users: List<String>
)
