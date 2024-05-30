package com.gotcha.narandee.src.place

import com.gotcha.narandee.src.result.CommonInterface

data class PlaceAnswer(
    val places: ArrayList<Place>
)

data class Place(
    val name: String,
    val address: String,
    val reason: String,
    val todo: String
) : CommonInterface {
    override fun getGptScript(): String {
        return address + "\n\n" + reason + "\n\n" + todo
    }

}
