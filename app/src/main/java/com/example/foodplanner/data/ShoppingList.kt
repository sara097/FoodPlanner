package com.example.foodplanner.data

import java.util.Date

data class ShoppingList(
    val id: String,
    val name: String,
    val timeOfCreating: Date,
    val timeFromWhichIsMade: TimeRange,
    val products: List<Product>,
    val users: List<String>
    )


data class TimeRange(
    val from: Date,
    val to: Date
){

    constructor(time: Pair<Date, Date>): this(time.first, time.second)
}