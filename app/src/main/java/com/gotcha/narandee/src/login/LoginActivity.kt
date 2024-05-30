package com.gotcha.narandee.src.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_EMAIL
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_UID
import com.gotcha.narandee.config.ApplicationClass.Companion.firebaseAuth
import com.gotcha.narandee.config.ApplicationClass.Companion.gso
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseActivity
import com.gotcha.narandee.databinding.ActivityLoginBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.signup.NameActivity

private const val TAG = "LoginActivity_싸피"

class LoginActivity : BaseActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate) {
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isFromWidget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isFromWidget = intent.getBooleanExtra("from_widget", false)

        // indicator
        val indicator = binding.indicator

        // viewPager
        val viewPager = binding.viewPager

        val pagerAdapter = ViewPagerAdapter(getList())

        viewPager.apply {
            adapter = pagerAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        indicator.attachTo(viewPager)

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 로그인 / 회원가입 버튼 클릭
        binding.loginBtn.setOnClickListener {
            signIn()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    val user = firebaseAuth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser

                    sharedPreferences.setString(USER_EMAIL, user?.email.toString())
                    sharedPreferences.setString(USER_UID, user?.uid.toString())

                    Log.d(TAG, "firebaseAuthWithGoogle: isNewUser -> $isNewUser")

                    if (isNewUser == true) {
                        // 회원가입 화면으로 이동
                        goToSignUp()
                    } else {
                        // 메인 화면으로 이동
                        goToMain(user)
                    }
                } else {
                    // 로그인 실패
                    showToast("로그인 실패")
                }
            }
    }

    // 로그인
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // 최근 유저 확인
    override fun onStart() {
        super.onStart()
        val currentUser = firebaseAuth.currentUser
        goToMain(currentUser)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "onActivityResult: ", e)
            }
        }
    }

    // 메인으로 가는 함수
    private fun goToMain(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("from_widget", isFromWidget)
            startActivity(intent)
        }
    }

    private fun goToSignUp() {
        val intent = Intent(this, NameActivity::class.java)
        startActivity(intent)
    }

    // 뷰 페이저 들어갈 아이템
    private fun getList(): ArrayList<Content> {
        val item1 =
            Content("오늘 뭐 먹지?", "항상 고민되는 오늘의 메뉴,\n내 취향에 맞는 음식을 추천해 줘요", R.drawable.page_img1)
        val item2 =
            Content("오늘 뭐 하지?", "항상 고민되는 놀거리,\n연인, 친구 등 알맞는 놀거리를 추천해 줘요", R.drawable.page_img2)
        val item3 =
            Content("오늘 뭐 입지?", "항상 고민되는 오늘의 코디,\n날씨에 알맞는 옷차림을 추천해 줘요", R.drawable.page_img3)

        return arrayListOf(item1, item2, item3)
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}