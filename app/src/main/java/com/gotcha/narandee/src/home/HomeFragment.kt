package com.gotcha.narandee.src.home

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import com.gotcha.narandee.R
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentHomeBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.util.navigate

private const val TAG = "HomeFragment_싸피"

class HomeFragment :
    BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::bind, R.layout.fragment_home) {

    private val mainActivityViewModel: MainViewModel by activityViewModels()
    private lateinit var mainActivity: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initObserver()
        initEvent()
        goToCustomFromWidget()

    }

    private fun goToCustomFromWidget() {
        if (mainActivityViewModel.isFromWidget) {
            navigate(R.id.action_homeFragment_to_customFragment)
            mainActivityViewModel.setIsFromWidget(false)
        }
    }

    override fun onResume() {
        super.onResume()
        mainActivity.hideBottomNav(false)
        mainActivityViewModel.setLocationPreferences(
            Location(null).apply {
                latitude = 0.0
                longitude = 0.0
            }
        )
        mainActivityViewModel.setLocationAddress("")
    }

    private fun initEvent() {
        binding.apply {
            homeFoodCv.setOnClickListener {
                navigate(R.id.action_homeFragment_to_foodFragment)
            }
            homePlaceCv.setOnClickListener {
                navigate(R.id.action_homeFragment_to_placeFragment)
            }
            homeClothesCv.setOnClickListener {
                navigate(R.id.action_homeFragment_to_clothesFragment)
            }
            homeCustomCv.setOnClickListener {
                navigate(R.id.action_homeFragment_to_customFragment)
            }
        }
    }

    private fun initObserver() {
        mainActivityViewModel.userName.observe(viewLifecycleOwner) {
            initUI()
        }
    }

    private fun initUI() {
        binding.homeTitleNicknameTv.text = mainActivityViewModel.userName.value
    }
}