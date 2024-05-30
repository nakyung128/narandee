package com.gotcha.narandee.util

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

fun Fragment.popBackStack() {
    this.findNavController().popBackStack()
    KeyBoardUtil.hide(requireActivity())
}

fun Fragment.navigate(action: Int) {
    this.findNavController().navigate(action)
    KeyBoardUtil.hide(requireActivity())
}