package com.gotcha.narandee.src.clothes

import com.gotcha.narandee.src.result.CommonInterface


data class ClothesAnswer(
    val clothes: ArrayList<Clothes>
)

data class Clothes(
    val name: String,
    val reason: String
): CommonInterface {
    override fun getGptScript(): String {
        return reason
    }
}
