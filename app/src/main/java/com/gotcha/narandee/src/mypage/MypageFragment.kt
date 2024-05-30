package com.gotcha.narandee.src.mypage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_EMAIL
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_NAME
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_UID
import com.gotcha.narandee.config.ApplicationClass.Companion.dbRef
import com.gotcha.narandee.config.ApplicationClass.Companion.firebaseAuth
import com.gotcha.narandee.config.ApplicationClass.Companion.gso
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentMypageBinding
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.login.LoginActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.src.signup.NameActivity
import com.gotcha.narandee.src.signup.SelectActivity
import com.gotcha.narandee.util.CustomDialog

private const val TAG = "MypageFragment_싸피"

class MyPageFragment :
    BaseFragment<FragmentMypageBinding>(FragmentMypageBinding::bind, R.layout.fragment_mypage) {

    private lateinit var mainActivity: MainActivity
    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGoogleSignInClient()
        initObserver()
        initClickEvent()
    }

    private fun initGoogleSignInClient() {
        googleSignInClient = GoogleSignIn.getClient(mainActivity, gso)
    }

    private fun initObserver() {
        mainActivityViewModel.userName.observe(viewLifecycleOwner) {
            initUI()
        }
    }

    private fun initClickEvent() {
        binding.apply {
            mypageEditProfileTv.setOnClickListener {
                activityResultLauncher.launch(Intent(mainActivity, NameActivity::class.java))
            }

            mypagePreferenceCl.setOnClickListener {
                val intent = Intent(mainActivity, SelectActivity::class.java)

                intent.putStringArrayListExtra("food_list", mainActivityViewModel.userFoodList.value)
                intent.putStringArrayListExtra("fashion_list", mainActivityViewModel.userFashionList.value)

                selectLauncher.launch(intent)
            }

            // 로그아웃
            mypageLogoutCl.setOnClickListener {
                val dialog = CustomDialog(mainActivity, "로그아웃", "로그아웃 하시겠습니까?")
                dialog.setItemClickListener(object : CustomDialog.ItemClickListener {
                    override fun onClick() {
                        logout()
                    }
                })

                dialog.show()
            }

            // 회원탈퇴
            mypageUnsubscribeCl.setOnClickListener {
                val dialog = CustomDialog(mainActivity, "회원탈퇴", "정말로 회원탈퇴를... 하시겠습니까?")
                dialog.setItemClickListener(object : CustomDialog.ItemClickListener {
                    override fun onClick() {
                        unsubscribe()
                    }
                })

                dialog.show()
            }
        }
    }

    private fun unsubscribe() {
        val user = firebaseAuth.currentUser
        user?.let {
            it.delete().addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    val uid = sharedPreferences.getString(USER_UID, "")

                    Log.d(TAG, "unsubscribe: $uid")

                    dbRef.child("users").child(uid).removeValue().addOnCompleteListener { removeTask ->
                        if (removeTask.isSuccessful) {
                            showToast("정상적으로 탈퇴되었습니다.")

                            firebaseAuth.signOut()
                            googleSignInClient.revokeAccess()

                            resetSharedPreferences()

                            startActivity(Intent(mainActivity, LoginActivity::class.java))
                            mainActivity.finish()
                        } else {
                            Log.e(TAG, "Database removal failed: ${removeTask.exception}")
                            showToast("데이터베이스에서 사용자 데이터를 삭제하는 데 실패했습니다.")
                        }
                    }
                } else {
                    Log.e(TAG, "User deletion failed: ${deleteTask.exception}")
                    showToast("재로그인 후 다시 시도해 주세요.")
                }
            }
        } ?: run {
            Log.e(TAG, "No user is currently signed in.")
            showToast("현재 로그인된 사용자가 없습니다.")
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        googleSignInClient.signOut()

        showToast("로그아웃되었습니다.")

        resetSharedPreferences()

        startActivity(Intent(mainActivity, LoginActivity::class.java))
        mainActivity.finish()
    }

    // sharedPreferences 초기화
    private fun resetSharedPreferences() {
        sharedPreferences.setString(USER_NAME, "")
        sharedPreferences.setString(USER_EMAIL, "")
        sharedPreferences.setString(USER_UID, "")
    }

    private fun initUI() {
        binding.mypageNicknameTv.text = sharedPreferences.getString(USER_NAME, "")
        binding.mypageEmailTv.text = sharedPreferences.getString(USER_EMAIL, "")
    }

    private val activityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        mainActivityViewModel.setUserNameFromSP()
    }

    private val selectLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        mainActivityViewModel.setPreferenceList()
    }

}