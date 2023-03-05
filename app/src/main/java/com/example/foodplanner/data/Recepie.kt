package com.example.foodplanner.data

data class Recipe(
    val id: String,
    val name: String,
    val tags: List<String>,
    val ingredients: List<Ingredient>,
    val wayOfMaking: String, //multiline
    val kcal: String,
    val users: List<String>
)

