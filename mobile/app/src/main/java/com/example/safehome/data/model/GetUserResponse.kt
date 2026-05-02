package com.example.safehome.data.model

data class GetUserResponse(
    val user: User
)

data class User(
    val user_id: String,
    val email: String,
    val name: String,
    val role: String,
    val subscription_plan_name: String,
    val email_confirmed: String,
    val created_at: String,
)