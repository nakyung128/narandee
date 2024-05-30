package com.gotcha.narandee.config

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.gotcha.narandee.R
import com.gotcha.narandee.util.SharedPreference
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApplicationClass : Application() {
    companion object {
        lateinit var sharedPreferences: SharedPreference
        lateinit var dbRef: DatabaseReference
        lateinit var firebaseAuth: FirebaseAuth
        lateinit var gso: GoogleSignInOptions
        lateinit var GPTRetrofit: Retrofit
        lateinit var weatherRetrofit: Retrofit

        const val USER_NAME = "user_name"
        const val USER_EMAIL = "user_email"
        const val USER_UID = "user_uid"
        const val USER_AGE = "user_age"
        const val USER_GENDER = "user_gender"

        const val GPT_BASE_URL = "https://api.openai.com/"
        const val GPT_KEY = "sk-proj-aKurzhjxFAHM3X4c2b4aT3BlbkFJYmvUVRqAZSRrvEc99E93"
//        const val GPT_KEY = "sk-proj-ADdsCJAWR6vY53vlP65JT3BlbkFJn6vMSpCuQzryQiivEvWm"
        const val WEATHER_BASE_URL = "http://api.openweathermap.org/data/2.5/"

        const val WEATHER_API_KEY = "5cbe9dd4041225479ef6d0e088b2ffb8"

        const val FRAGMENT_FOOD = 1
        const val FRAGMENT_PLACE = 2
        const val FRAGMENT_CLOTHES = 3
        const val FRAGMENT_CUSTOM = 4
        const val FRAGMENT_RESULT = 5

        val foodCategoryList = arrayListOf(
            "아뇨, 따로 없어요", "한식", "서양식", "중식", "일식", "아시안", "멕시칸"
        )
        val timeCategoryList = arrayListOf(
            "아침", "점심", "저녁"
        )
        val regionCategoryList = arrayListOf(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시",
            "세종특별자치시", "경기도", "강원도", "충청북도", "충청남도", "전라북도", "전라남도", "경상북도", "경상남도", "제주도"
        )

        val withCategoryList = arrayListOf(
            "혼자", "친구", "연인", "가족"
        )
        val ageCategoryList = arrayListOf(
            "10대", "20대", "30대", "40대", "50대", "60대 이상"
        )
        val genderCategoryList = arrayListOf(
            "여성", "남성"
        )
        val clothesCategoryList = arrayListOf(
            "아뇨, 따로 없어요", "캐주얼", "스트릿", "빈티지", "미니멀", "모던", "페미닌", "러블리", "스포티", "휴양지룩"
        )
    }

    override fun onCreate() {
        super.onCreate()

        sharedPreferences = SharedPreference(applicationContext)
        firebaseAuth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.default_web_client_id))
            .build()

        initRetrofitInstance()
    }

    // retrofit 인스턴스 생성, 레트로핏에 설정값 지정
    private fun initRetrofitInstance() {
        val client: OkHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $GPT_KEY")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        // retrofit 전역변수에 API url, 인터셉터, Gson 넣어주고 빌드
        GPTRetrofit = Retrofit.Builder()
            .baseUrl(GPT_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        weatherRetrofit = Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
}