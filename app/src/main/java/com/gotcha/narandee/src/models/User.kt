package com.gotcha.narandee.src.models

data class User(
    val email: String = "",
    val nickname: String = "",
    val age: String = "",
    val gender: String = "",
    val food: ArrayList<String> = arrayListOf(),
    val fashion: ArrayList<String> = arrayListOf()
)
