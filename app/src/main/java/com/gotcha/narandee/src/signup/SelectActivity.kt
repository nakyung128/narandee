package com.gotcha.narandee.src.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_AGE
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_EMAIL
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_GENDER
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_NAME
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_UID
import com.gotcha.narandee.config.ApplicationClass.Companion.dbRef
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseActivity
import com.gotcha.narandee.databinding.ActivitySelectBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.models.User

private const val TAG = "SelectActivity_싸피"

class SelectActivity : BaseActivity<ActivitySelectBinding>(ActivitySelectBinding::inflate) {

    private var isEdit = false
    private var foodList = arrayListOf<String>() // 음식 취향 리스트
    private var fashionList = arrayListOf<String>() // 패션 취향 리스트

    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addListener() // 버튼들에 리스너 달기

        uid = sharedPreferences.getString(USER_UID, "")

        // 받은 값 있으면 수정 모드
        if (intent.getStringArrayListExtra("food_list") != null) {
            isEdit = true
            showSelectedBtn()
        }

        // 완료 버튼 클릭 시
        binding.selectFinishBtn.setOnClickListener {
            val email = sharedPreferences.getString(USER_EMAIL, "")
            val name = sharedPreferences.getString(USER_NAME, "")
            val age = sharedPreferences.getString(USER_AGE, "")
            val gender = sharedPreferences.getString(USER_GENDER, "")

            if (foodList.isNotEmpty() && fashionList.isNotEmpty()) {
                if (!isEdit) {
                    saveUserInfo(
                        email,
                        name,
                        age,
                        gender,
                        foodList,
                        fashionList
                    ) // realtime database에 저장

                    // 화면 이동
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    updateInfo(foodList, fashionList) // 수정 모드면
                    finish()
                }
            } else {
                showToast("최소 한 개 이상 선택해 주세요")
            }
        }
    }

    private fun addListener() {
        // 음식 클릭 리스너
        val foodListener = View.OnClickListener { v ->
            v.isSelected = !v.isSelected
            val btn = v as Button

            if (!v.isSelected) {
                foodList.remove(btn.text.toString())
            } else {
                foodList.add(btn.text.toString())
            }
        }

        // 패션 클릭 리스너
        val fashionListener = View.OnClickListener { v ->
            v.isSelected = !v.isSelected
            val btn = v as Button

            if (!v.isSelected) {
                fashionList.remove(btn.text.toString())
            } else {
                fashionList.add(btn.text.toString())
            }
        }

        // 버튼에 리스너 달기
        binding.apply {
            koreanBtn.setOnClickListener(foodListener)
            westernBtn.setOnClickListener(foodListener)
            chineseBtn.setOnClickListener(foodListener)
            japaneseBtn.setOnClickListener(foodListener)
            asianBtn.setOnClickListener(foodListener)
            mexicanBtn.setOnClickListener(foodListener)
            foodEverythingBtn.setOnClickListener(foodListener)

            casualBtn.setOnClickListener(fashionListener)
            streetBtn.setOnClickListener(fashionListener)
            vintageBtn.setOnClickListener(fashionListener)
            minimalBtn.setOnClickListener(fashionListener)
            modernBtn.setOnClickListener(fashionListener)
            femininBtn.setOnClickListener(fashionListener)
            lovelyBtn.setOnClickListener(fashionListener)
            sportyBtn.setOnClickListener(fashionListener)
            vacationBtn.setOnClickListener(fashionListener)
            fashionEverythingBtn.setOnClickListener(fashionListener)
        }
    }

    // 선택되어 있는 취향 selected 처리하기
    private fun showSelectedBtn() {
        foodList = intent.getStringArrayListExtra("food_list")!!
        fashionList = intent.getStringArrayListExtra("fashion_list")!!

        if (foodList.contains("한식")) {
            binding.koreanBtn.isSelected = true
        }
        if (foodList.contains("서양식")) {
            binding.westernBtn.isSelected = true
        }
        if (foodList.contains("중식")) {
            binding.chineseBtn.isSelected = true
        }
        if (foodList.contains("일식")) {
            binding.japaneseBtn.isSelected = true
        }
        if (foodList.contains("아시안")) {
            binding.asianBtn.isSelected = true
        }
        if (foodList.contains("멕시칸")) {
            binding.mexicanBtn.isSelected = true
        }
        if (foodList.contains("상관없음")) {
            binding.foodEverythingBtn.isSelected = true
        }

        if (fashionList.contains("캐주얼")) {
            binding.casualBtn.isSelected = true
        }
        if (fashionList.contains("스트릿")) {
            binding.streetBtn.isSelected = true
        }
        if (fashionList.contains("빈티지")) {
            binding.vintageBtn.isSelected = true
        }
        if (fashionList.contains("미니멀")) {
            binding.minimalBtn.isSelected = true
        }
        if (fashionList.contains("모던")) {
            binding.modernBtn.isSelected = true
        }
        if (fashionList.contains("페미닌")) {
            binding.femininBtn.isSelected = true
        }
        if (fashionList.contains("러블리")) {
            binding.lovelyBtn.isSelected = true
        }
        if (fashionList.contains("스포티")) {
            binding.sportyBtn.isSelected = true
        }
        if (fashionList.contains("휴양지룩")) {
            binding.vacationBtn.isSelected = true
        }
        if (fashionList.contains("상관없음")) {
            binding.fashionEverythingBtn.isSelected = true
        }
    }

    // firebase db에 사용자 정보 저장
    private fun saveUserInfo(
        email: String,
        name: String,
        age: String,
        gender: String,
        food: ArrayList<String>,
        fashion: ArrayList<String>
    ) {
        val userInfo = User(email, name, age, gender, food, fashion)
        dbRef.child("users").child(uid).setValue(userInfo)
    }

    // 취향 수정
    private fun updateInfo(food: ArrayList<String>, fashion: ArrayList<String>) {
        dbRef.child("users").child(uid).child("fashion").setValue(fashion)
        dbRef.child("users").child(uid).child("food").setValue(food)
    }
}