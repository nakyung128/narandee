package com.gotcha.narandee.src.food

import com.gotcha.narandee.src.result.CommonInterface

data class FoodAnswer(
    val foods: ArrayList<Food>
)

data class Food(
    val name: String,
    val reason: String
) : CommonInterface {
    override fun getGptScript(): String {
        return reason
    }
}
