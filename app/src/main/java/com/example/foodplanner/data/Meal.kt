package com.example.foodplanner.data

data class Meal(
    val type: MealType,
    val recipes: List<String>
)

enum class MealType {

    BREAKFAST, BRUNCH, MORNING_SNACK, LUNCH, AFTERNOON_SNACK, DINNER
}
