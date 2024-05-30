package com.gotcha.narandee.src.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_AGE
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_GENDER
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_NAME
import com.gotcha.narandee.config.ApplicationClass.Companion.ageCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.genderCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseActivity
import com.gotcha.narandee.databinding.ActivityInfoBinding
import com.gotcha.narandee.src.food.CustomSpinnerAdapter

private const val TAG = "InfoActivity_싸피"

class InfoActivity : BaseActivity<ActivityInfoBinding>(ActivityInfoBinding::inflate) {

    private lateinit var selectedAge: String
    private lateinit var selectedGender: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        selectedAge = ageCategoryList[0]
        selectedGender = genderCategoryList[0]

        initUI()
        initEvent()
        initSpinner()
    }

    private fun initUI() {
        val nickname = sharedPreferences.getString(USER_NAME, "")
        binding.infoNameTv.text = nickname
    }

    private fun initEvent() {
        binding.infoNextBtn.setOnClickListener {
            val intent = Intent(this, SelectActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initSpinner() {
        val genderCategoryAdapter =
            CustomSpinnerAdapter(this, genderCategoryList)
        binding.genderCategorySpinner.adapter = genderCategoryAdapter

        val ageCategoryAdapter =
            CustomSpinnerAdapter(this, ageCategoryList)
        binding.ageCategorySpinner.adapter = ageCategoryAdapter

        val spinnerGenderItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                genderCategoryAdapter.setSelectedItemPosition(position)
                selectedGender = genderCategoryList[position]
                sharedPreferences.setString(USER_GENDER, selectedGender)
                Log.d(TAG, "onItemSelected: $selectedGender")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        val spinnerAgeItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                ageCategoryAdapter.setSelectedItemPosition(position)
                selectedAge = ageCategoryList[position]
                sharedPreferences.setString(USER_AGE, selectedAge)
                Log.d(TAG, "onItemSelected: $selectedAge")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.genderCategorySpinner.onItemSelectedListener = spinnerGenderItemSelectedListener
        binding.ageCategorySpinner.onItemSelectedListener = spinnerAgeItemSelectedListener
    }
}