package com.gotcha.narandee.src.main

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_AGE
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_EMAIL
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_GENDER
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_NAME
import com.gotcha.narandee.config.ApplicationClass.Companion.foodCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.regionCategoryList
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.ApplicationClass.Companion.timeCategoryList
import com.gotcha.narandee.src.models.User

private const val TAG = "MainViewModel 싸피"

class MainViewModel : ViewModel() {
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String>
        get() = _userName

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail

    private val _userGender = MutableLiveData<String>()
    val userGender: LiveData<String>
        get() = _userGender

    private val _userAge = MutableLiveData<String>()
    val userAge: LiveData<String>
        get() = _userAge

    private val _userFoodList = MutableLiveData<ArrayList<String>>()
    val userFoodList: LiveData<ArrayList<String>>
        get() = _userFoodList

    private val _userFashionList = MutableLiveData<ArrayList<String>>()
    val userFashionList: LiveData<ArrayList<String>>
        get() = _userFashionList

    private var _resultType = 0
    val resultType: Int
        get() = _resultType

    private var _userCurrentFoodPreference = foodCategoryList[0]
    val userCurrentFoodPreference: String
        get() = _userCurrentFoodPreference

    private var _userCurrentTimePreference = timeCategoryList[0]
    val userCurrentTimePreference: String
        get() = _userCurrentTimePreference

    private var _userCurrentPlacePreference = regionCategoryList[0]
    val userCurrentPlacePreference: String
        get() = _userCurrentPlacePreference

    private var _userCurrentPlaceDetailPreference = ""
    val userCurrentPlaceDetailPreference: String
        get() = _userCurrentPlaceDetailPreference

    private var _userCurrentClothesPreference = ""
    val userCurrentClothesPreference: String
        get() = _userCurrentClothesPreference

    private var _userLocation = MutableLiveData<Location>()
    val userLocation: LiveData<Location>
        get() = _userLocation

    private var _userAddress = MutableLiveData("")
    val userAddress: LiveData<String>
        get() = _userAddress

    private var _userPlaceWith = ""
    val userPlaceWith: String
        get() = _userPlaceWith

    private var _isFromWidget = false
    val isFromWidget: Boolean
        get() = _isFromWidget

    fun setIsFromWidget(isFromWidget: Boolean) {
        _isFromWidget = isFromWidget
    }

    fun setUserNameFromSP() {
        _userName.value = sharedPreferences.getString(USER_NAME, "")
    }

    fun setUserEmailFromSP() {
        _userEmail.value = sharedPreferences.getString(USER_EMAIL, "")
    }

    fun setUserName() {
        val uid = ApplicationClass.firebaseAuth.currentUser?.uid.toString()
        val ref = ApplicationClass.dbRef.child("users").child(uid).child("nickname")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 성공적으로 읽힘
                val name = snapshot.value

                // SharedPreference에 저장
                sharedPreferences.setString(USER_NAME, name.toString())
                _userName.value = name.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: 실패")
            }

        })
    }

    fun setPreferenceList() {
        val uid = ApplicationClass.firebaseAuth.currentUser?.uid.toString()
        val ref = ApplicationClass.dbRef.child("users").child(uid)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 성공적으로 읽힘
                val user = snapshot.getValue(User::class.java) // User 형태로 cast

                // SharedPreference에 저장
                _userFoodList.value = user?.food
                _userFashionList.value = user?.fashion
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: 실패")
            }
        })
    }

    fun setUserInfo() {
        val uid = ApplicationClass.firebaseAuth.currentUser?.uid.toString()
        val genderRef = ApplicationClass.dbRef.child("users").child(uid).child("gender")
        val ageRef = ApplicationClass.dbRef.child("users").child(uid).child("age")

        genderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 성공적으로 읽힘
                val gender = snapshot.value

                // SharedPreference에 저장
                sharedPreferences.setString(USER_GENDER, gender.toString())
                _userGender.value = gender.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: 실패")
            }
        })

        ageRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 성공적으로 읽힘
                val age = snapshot.value

                // SharedPreference에 저장
                sharedPreferences.setString(USER_AGE, age.toString())
                _userAge.value = age.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: 실패")
            }
        })
    }

    fun setResultType(resultType: Int) {
        _resultType = resultType
    }

    fun setCurrentFoodPreferences(food: String, time: String) {
        _userCurrentFoodPreference = food
        _userCurrentTimePreference = time
    }

    fun setCurrentPlacePreferences(place: String, detail: String, with: String) {
        _userCurrentPlacePreference = place
        _userCurrentPlaceDetailPreference = detail
        _userPlaceWith = with
    }

    fun setCurrentClothesPreferences(clothes: String) {
        _userCurrentClothesPreference = clothes
    }

    fun setLocationPreferences(location: Location) {
        _userLocation.value = location
    }

    fun setLocationAddress(address: String) {
        _userAddress.value = address
    }

    init {
        setUserNameFromSP()
        setUserEmailFromSP()
    }
}