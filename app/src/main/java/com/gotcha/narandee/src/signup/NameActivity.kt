package com.gotcha.narandee.src.signup

import android.content.Intent
import android.os.Bundle
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_NAME
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_UID
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseActivity
import com.gotcha.narandee.databinding.ActivityNameBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.models.User
import kotlin.random.Random

class NameActivity : BaseActivity<ActivityNameBinding>(ActivityNameBinding::inflate) {

    private var userName = sharedPreferences.getString(USER_NAME, "")
    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val name = sharedPreferences.getString(USER_NAME, "")
        if (name != "") {
            isEdit = true
        }

        initUI()
        initEvent()
    }

    private fun initEvent() {
        binding.randomBtn.setOnClickListener {
            val nickname = generateRandomNickname()
            binding.nicknameEt.setText(nickname)
        }

        binding.nameNextBtn.setOnClickListener {
            val name = binding.nicknameEt.text.toString()
            if (name.isNotEmpty()) {
                sharedPreferences.setString(USER_NAME, name)

                // 닉네임 수정이면 udpate
                if (isEdit) {
                    updateName(name)
                    finish()
                } else {
                    val intent = Intent(this, InfoActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                showToast(resources.getString(R.string.name_empty))
            }
        }
    }

    private fun initUI() {
        if (userName != "") {
            binding.nicknameEt.setText(userName)
            binding.nameNextBtn.text = resources.getString(R.string.check)
        } else {
            binding.nameNextBtn.text = resources.getString(R.string.next)
        }
    }

    companion object {
        private val verbs = arrayOf(
            "뛰는",
            "웃는",
            "읽는",
            "걷는",
            "먹는",
            "보는",
            "듣는",
            "쓰는",
            "그리는",
            "노는",
            "춤추는",
            "노래하는",
            "피는",
            "자는",
            "일하는",
            "공부하는",
            "물어보는",
            "타는",
            "여행하는",
            "사랑하는",
            "도와주는",
            "생각하는",
            "기다리는",
            "시작하는",
            "마시는",
            "선택하는",
            "움직이는",
            "떠나는",
            "배우는",
            "파는",
            "사는",
            "찾는",
            "만드는",
            "만나는",
            "좋아하는",
            "싫어하는",
            "놀라는",
            "소리치는",
            "웃긴",
            "슬픈",
            "즐거운",
            "건강한",
            "아픈",
            "강한",
            "약한",
            "빠른",
            "느린"
        )
        private val nouns = arrayOf(
            "토끼",
            "사과",
            "나무",
            "별",
            "곰",
            "책",
            "길",
            "음식",
            "풍경",
            "음악",
            "펜",
            "그림",
            "게임",
            "춤",
            "노래",
            "꽃",
            "잠",
            "일",
            "공부",
            "질문",
            "자동차",
            "여행",
            "사랑",
            "도움",
            "생각",
            "시간",
            "음료",
            "선택",
            "움직임",
            "출발",
            "지식",
            "상품",
            "구매",
            "탐색",
            "창조",
            "만남",
            "취미",
            "감정",
            "소리",
            "웃음",
            "슬픔",
            "즐거움",
            "건강",
            "병",
            "힘",
            "약점",
            "속도",
            "여유",
            "고양이",
            "강아지",
            "햄스터",
            "토끼",
            "판다",
            "코알라",
            "고슴도치",
            "다람쥐",
            "기니피그",
            "페릿",
            "여우",
            "너구리",
            "고라니",
            "사막여우",
            "레서판다",
            "치와와",
            "바다표범",
            "돌고래",
            "알파카",
            "라마",
            "오리너구리",
            "수달"
        )

        fun generateRandomNickname(): String {
            val verb = verbs[Random.nextInt(verbs.size)]
            val noun = nouns[Random.nextInt(nouns.size)]
            return "$verb $noun"
        }
    }

    // firebase db에 닉네임 수정
    private fun updateName(name: String) {
        val uid = sharedPreferences.getString(USER_UID, "")
        ApplicationClass.dbRef.child("users").child(uid).child("nickname").setValue(name)
    }
}