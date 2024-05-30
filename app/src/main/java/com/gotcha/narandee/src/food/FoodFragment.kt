package com.gotcha.narandee.src.food

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_FOOD
import com.gotcha.narandee.config.ApplicationClass.Companion.foodCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.timeCategoryList
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentFoodBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.util.navigate

private const val TAG = "FoodFragment 싸피"

class FoodFragment :
    BaseFragment<FragmentFoodBinding>(FragmentFoodBinding::bind, R.layout.fragment_food) {

    private lateinit var mainActivity: MainActivity
    private lateinit var selectedType: String
    private lateinit var selectedTime: String

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기본값 (선택 안 했을 때 맨 위 것)
        selectedType = foodCategoryList[0]
        selectedTime = timeCategoryList[0]
        initView()
        initSpinner()
        initEvent()
    }

    private fun initView() {
        mainActivity.hideBottomNav(true)
    }

    private fun initEvent() {
        binding.foodNextBtn.setOnClickListener {
            mainActivityViewModel.setCurrentFoodPreferences(
                binding.foodCategorySpinner.selectedItem.toString(),
                binding.foodTimeCategorySpinner.selectedItem.toString(),
            )

            mainActivityViewModel.setResultType(FRAGMENT_FOOD)
            navigate(R.id.action_foodFragment_to_resultFragment)
        }
    }

    private fun initSpinner() {
        val spinnerCategoryAdapter =
            CustomSpinnerAdapter(mainActivity, foodCategoryList)
        binding.foodCategorySpinner.adapter = spinnerCategoryAdapter

        val spinnerTimeCategoryAdapter =
            CustomSpinnerAdapter(mainActivity, timeCategoryList)
        binding.foodTimeCategorySpinner.adapter = spinnerTimeCategoryAdapter

        val spinnerFoodItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerCategoryAdapter.setSelectedItemPosition(position)
                selectedType = foodCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        val spinnerTimeItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerTimeCategoryAdapter.setSelectedItemPosition(position)
                selectedTime = timeCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.foodCategorySpinner.onItemSelectedListener = spinnerFoodItemSelectedListener
        binding.foodTimeCategorySpinner.onItemSelectedListener = spinnerTimeItemSelectedListener

    }

}