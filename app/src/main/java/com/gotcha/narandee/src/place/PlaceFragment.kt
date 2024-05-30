package com.gotcha.narandee.src.place

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_PLACE
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_RESULT
import com.gotcha.narandee.config.ApplicationClass.Companion.regionCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.withCategoryList
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentPlaceBinding
import com.gotcha.narandee.src.food.CustomSpinnerAdapter
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.util.navigate

class PlaceFragment :
    BaseFragment<FragmentPlaceBinding>(FragmentPlaceBinding::bind, R.layout.fragment_place) {

    private lateinit var mainActivity: MainActivity
    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private lateinit var place: String
    private lateinit var with: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        place = regionCategoryList[0]
        with = withCategoryList[0]

        initView()
        initEvent()
        initSpinner()
    }

    private fun initView() {
        mainActivity.hideBottomNav(true)
    }

    private fun initEvent() {
        binding.placeNextBtn.setOnClickListener {
            val detail = binding.placeDetailEt.text.toString()

            // 디테일 입력이 없다면 빈 칸으로 저장해 두기
            if (detail.isNotEmpty()) {
                mainActivityViewModel.setCurrentPlacePreferences(place, detail, with)
            } else {
                mainActivityViewModel.setCurrentPlacePreferences(place, "", with)
            }

            mainActivityViewModel.setResultType(FRAGMENT_PLACE)
            navigate(R.id.action_placeFragment_to_resultFragment)
        }
    }

    private fun initSpinner() {
        val spinnerCategoryAdapter =
            CustomSpinnerAdapter(mainActivity, regionCategoryList)
        binding.placeCategorySpinner.adapter = spinnerCategoryAdapter

        val spinnerWithAdapter =
            CustomSpinnerAdapter(mainActivity, withCategoryList)
        binding.placeWithSpinner.adapter = spinnerWithAdapter

        val spinnerItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerCategoryAdapter.setSelectedItemPosition(position)
                place = regionCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        val spinnerWithItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerCategoryAdapter.setSelectedItemPosition(position)
                with = withCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.placeCategorySpinner.onItemSelectedListener = spinnerItemSelectedListener
        binding.placeWithSpinner.onItemSelectedListener = spinnerWithItemSelectedListener
    }

}