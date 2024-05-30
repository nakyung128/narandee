package com.gotcha.narandee.src.main

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CLOTHES
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CUSTOM
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_FOOD
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_PLACE
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_RESULT
import com.gotcha.narandee.config.BaseActivity
import com.gotcha.narandee.databinding.ActivityMainBinding
import com.gotcha.narandee.src.clothes.ClothesFragment
import com.gotcha.narandee.src.custom.CustomFragment
import com.gotcha.narandee.src.food.FoodFragment
import com.gotcha.narandee.src.place.PlaceFragment
import com.gotcha.narandee.src.result.ResultFragment

private const val TAG = "MainActivity_싸피"

class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {

    private val mainViewModel: MainViewModel by viewModels()
    private var isFromWidget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getUserInfo()
        initBottomNavigation()
        disableBottomNavTooltip()

        isFromWidget = intent.getBooleanExtra("from_widget", false)
        mainViewModel.setIsFromWidget(isFromWidget)
        isFromWidget = false

//        if(isFromWidget){
//
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.main_nav_host_fcv, CustomFragment())
//                .commit()
//
//            isFromWidget = false
//        }
    }

    fun hideBottomNav(isHide: Boolean) {
        if (isHide) {
            binding.mainBottomNavBnv.visibility = View.GONE
        } else {
            binding.mainBottomNavBnv.visibility = View.VISIBLE
        }
    }

    private fun disableBottomNavTooltip() {
        binding.apply {
            mainBottomNavBnv.menu.forEach {
                val view = mainBottomNavBnv.findViewById<View>(it.itemId)
                view.setOnLongClickListener {
                    true
                }
            }
        }
    }

    private fun initBottomNavigation() {
        Log.d(TAG, "initBottomNavigation: 1111")
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.main_nav_host_fcv) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.mainBottomNavBnv, navController)
        Log.d(TAG, "initBottomNavigation: 2222")
    }

    private fun getUserInfo() {
        mainViewModel.setUserName()
        mainViewModel.setPreferenceList()
        mainViewModel.setUserInfo()
    }

}