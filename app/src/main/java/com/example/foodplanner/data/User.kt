package com.example.foodplanner.data

import android.util.Patterns

class InvalidInputException(message: String): Exception(message)

data class User(
    val id: String,
    val login: String,
    val email: String,
    val password: String,
    val gender: Gender
)


fun emailChecker(input: String): String{
    if(!Patterns.EMAIL_ADDRESS.matcher(input).matches()) throw InvalidInputException("Invalid email address.")
    return input
}

fun passwordChecker(input: String) : String{
    when{
        input.length < 8 -> throw InvalidInputException("Password too short.")
        !input.contains("\\d".toRegex()) -> throw InvalidInputException("Password should contain at least one number.")
        !input.contains("[A-Z]".toRegex()) ->  throw InvalidInputException("Password should contain at least one uppercase letter")
    }
    return input
}