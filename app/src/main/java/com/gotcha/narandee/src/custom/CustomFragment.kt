package com.gotcha.narandee.src.custom

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CUSTOM
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentClothesBinding
import com.gotcha.narandee.databinding.FragmentCustomBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.util.navigate

private const val TAG = "CustomFragment_싸피"
class CustomFragment :
    BaseFragment<FragmentCustomBinding>(FragmentCustomBinding::bind, R.layout.fragment_custom) {

    private lateinit var mainActivity: MainActivity
    private val mainActivityViewModel: MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initEvent()
    }

    private fun initView() {
        mainActivity.hideBottomNav(true)
    }

    private fun initEvent() {
        binding.customNextBtn.setOnClickListener {
            mainActivityViewModel.setResultType(FRAGMENT_CUSTOM)
            navigate(R.id.action_customFragment_to_resultFragment)
        }
    }

}