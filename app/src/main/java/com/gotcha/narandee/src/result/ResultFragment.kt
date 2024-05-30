package com.gotcha.narandee.src.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CLOTHES
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CUSTOM
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_FOOD
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_PLACE
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_AGE
import com.gotcha.narandee.config.ApplicationClass.Companion.USER_GENDER
import com.gotcha.narandee.config.ApplicationClass.Companion.sharedPreferences
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentResultBinding
import com.gotcha.narandee.src.clothes.ClothesAnswer
import com.gotcha.narandee.src.clothes.WeatherResponse
import com.gotcha.narandee.src.food.FoodAnswer
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.src.models.ChatGptRequest
import com.gotcha.narandee.src.models.ChatMessage
import com.gotcha.narandee.src.models.Message
import com.gotcha.narandee.src.place.PlaceAnswer
import com.gotcha.narandee.src.service.GptApi
import com.gotcha.narandee.src.service.WeatherApi
import com.gotcha.narandee.util.popBackStack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.notify
import kotlin.math.log

private const val TAG = "ResultFragment_싸피"

class ResultFragment :
    BaseFragment<FragmentResultBinding>(
        FragmentResultBinding::bind,
        R.layout.fragment_result
    ) {

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private var resultType = 0
    private lateinit var nickname: String
    private lateinit var age: String
    private lateinit var gender: String

    private lateinit var foodType: String
    private lateinit var foodTime: String

    private lateinit var placeType: String
    private lateinit var placeDetail: String
    private lateinit var placeWith: String

    private lateinit var clothesType: String
    private lateinit var clothesLocation: Location
    private lateinit var clothesAddress: String
    private lateinit var clothesWeather: WeatherResponse
    private lateinit var clothesWeatherKorean: String
    private var clothesTemperature = 0

    private lateinit var mainActivity: MainActivity
    private lateinit var gptScript: String

    private var isFirstGPTAnswer = true
    private var recentAnswer = arrayListOf<String>() // 이전에 추천했던 것들

    private lateinit var chatAdapter: ChatListAdapter
    private var messageList: MutableList<ChatMessage> = arrayListOf()

    private lateinit var customAnswer: String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = activity as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nickname = mainActivityViewModel.userName.value.toString()
        resultType = mainActivityViewModel.resultType

        getUserInfo()

        when (resultType) {
            FRAGMENT_FOOD -> {
                gptScript = "님의 취향에 맞는 메뉴를 추천해 드릴게요 😆"
                getFoodData()
            }

            FRAGMENT_PLACE -> {
                gptScript = "님에게 알맞는 장소를 추천해 드릴게요 😚"
                getPlaceData()
            }

            FRAGMENT_CLOTHES -> {
                getClothesData()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatAdapter.clearTypingHandlerCallbacks()
    }

    private fun getClothesData() {
        clothesType = mainActivityViewModel.userCurrentClothesPreference
        clothesLocation = mainActivityViewModel.userLocation.value!!
        clothesAddress = mainActivityViewModel.userAddress.value!!

        // 입고 싶은 스타일이 "아뇨, 따로 없어요"라면 기존 사용자 것 가져 오기
        if (clothesType == "아뇨, 따로 없어요") {
            clothesType = mainActivityViewModel.userFashionList.value.toString()
        }
    }

    private suspend fun getWeather(): WeatherResponse {
        return WeatherApi.weatherService.getWeather(
            clothesLocation.latitude.toString(),
            clothesLocation.longitude.toString(),
            ApplicationClass.WEATHER_API_KEY
        )
    }

    private fun getFoodData() {
        foodType = mainActivityViewModel.userCurrentFoodPreference
        foodTime = mainActivityViewModel.userCurrentTimePreference

        // 먹고 싶은 음식 종류가 "아뇨, 따로 없어요"라면 기존 사용자 것 가져 오기
        if (foodType == "아뇨, 따로 없어요") {
            foodType = mainActivityViewModel.userFoodList.value.toString()
        }
    }

    private fun getPlaceData() {
        placeType = mainActivityViewModel.userCurrentPlacePreference
        placeDetail = mainActivityViewModel.userCurrentPlaceDetailPreference
        placeWith = mainActivityViewModel.userPlaceWith

        Log.d(TAG, "getPlaceData: $placeType , $placeDetail")
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        initView()
        initEvent()

        lifecycleScope.launch {
            when (resultType) {
                FRAGMENT_FOOD -> {
                    getFoodResult()
                }

                FRAGMENT_PLACE -> {
                    getPlaceResult()
                }

                FRAGMENT_CLOTHES -> {
                    getClothesResult()
                }
            }
        }
    }

    private fun initAdapter() {
        chatAdapter = ChatListAdapter(object : ChatListAdapter.AdapterCallback {
            override fun onNeedToScroll(position: Int) {
                binding.chatListView.smoothScrollToPosition(chatAdapter.itemCount - 1)
            }
        })

        binding.chatListView.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
    }

    private fun initEvent() {
        if (resultType == FRAGMENT_CUSTOM) {
            binding.sendBtn.setOnClickListener {
                val ask = binding.userAnswerEt.text.toString()
                binding.userAnswerEt.setText("")

                if (ask.isNotEmpty()) {
                    customAnswer = ask
                    messageList.add(ChatMessage("user", "", "", ask))
                    chatAdapter.submitList(messageList.toMutableList())

                    lifecycleScope.launch {
                        getCustomResult()
                    }
                }
            }
        }

        binding.resultBackBtn.setOnClickListener {
            popBackStack()
        }

        binding.resultAgainTv.setOnClickListener {
            val script = "다시 추천해 드릴게요! 🥺"
            messageList.add(ChatMessage("user", "", "", "다시 추천해 줘"))
            messageList.add(ChatMessage("gpt", "", script, ""))

            chatAdapter.submitList(messageList.toMutableList())

            val millis = script.length.toLong()

            lifecycleScope.launch {
                delay(millis)
                when (resultType) {
                    FRAGMENT_FOOD -> {
                        getFoodResult()
                    }

                    FRAGMENT_PLACE -> {
                        getPlaceResult()
                    }

                    FRAGMENT_CLOTHES -> {
                        getClothesResult()
                    }
                }
            }
        }

        binding.resultFinishTv.setOnClickListener {
            binding.resultEndTv.visibility = View.VISIBLE
            showBtn(false)
        }

        setChatClickListener()
    }

    private fun getUserInfo() {
        age = sharedPreferences.getString(USER_AGE, "")
        gender = sharedPreferences.getString(USER_GENDER, "")
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        when (resultType) {
            FRAGMENT_FOOD -> {
                binding.inputLayout.visibility = View.GONE
                messageList.add(
                    ChatMessage(
                        "user",
                        "",
                        null,
                        "${foodTime}으로 먹을 음식 내 취향에 맞춰서 추천해 줘 🤤"
                    )
                )
                messageList.add(ChatMessage("gpt", "", "$nickname $gptScript", ""))
                chatAdapter.submitList(messageList)
            }

            FRAGMENT_PLACE -> {
                binding.inputLayout.visibility = View.GONE
                if (placeDetail == "") {
                    messageList.add(ChatMessage("user", "", null, "${placeType}에서 가볼만한 곳 추천해 줘 😎"))
                } else {
                    messageList.add(
                        ChatMessage(
                            "user",
                            "",
                            null,
                            "$placeType ${placeDetail}에서 가볼만한 곳 추천해 줘 😎"
                        )
                    )
                }
                chatAdapter.submitList(messageList)
            }

            FRAGMENT_CLOTHES -> {
                binding.inputLayout.visibility = View.GONE
                messageList.add(
                    ChatMessage(
                        "user",
                        "",
                        null,
                        "${clothesAddress}에서 오늘 입을만한 옷 추천해 줘 😆"
                    )
                )
                chatAdapter.submitList(messageList)
            }

            FRAGMENT_CUSTOM -> {
                binding.inputLayout.visibility = View.VISIBLE
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private suspend fun getFoodResult() {
        val jsonString = """
    {
      "foods": [
        {
          "name": "__",
          "reason": "__"
        }
      ]
    }
    """.trimIndent()

        val request = ChatGptRequest(
            model = "gpt-4o",
            messages = listOf(
                Message(
                    role = "system",
                    content = "당신은 무엇을 먹을지 고민하고 있는 사용자에게 최신 트렌드를 반영해 메뉴를 추천해 주는 숙련된 요리 추천 전문가로, " +
                            "한국에서 유행하는 음식과 한국인의 입맛을 잘 알고 있으며 친절하게 음식을 추천해 주고 소개해 줍니다."
                ),
                Message(
                    role = "user", content = "$jsonString 무조건 이 형식을 사용해. 안 하면 오류가 나." +
                            "${age}의 한국인 ${gender}이 좋아할 만한 것으로 " +
                            "$foodTime 식사에 알맞는 자세한 음식 이름 (예를 들면 파스타 말고 크림 파스타)과 추천 이유를 100자 이내로 요약해서 " +
                            "$foodType 종류로 음식 세 가지만 $recentAnswer 제외하고 " +
                            "한국어로 존댓말을 사용하고 어울리는 이모지를 사용해서 말해 줘 " +
                            "script는 내가 질문한 것 그대로 보내면 돼" +
                            "json만 string 형태로 ``` 없이 반환해 주면 돼."
                )
            )
        )

        lifecycleScope.launch {
            try {
                showLoadingDialog()

                val chatResponse = withContext(Dispatchers.IO) {
                    val response = GptApi.chatGptService.getChatResponse(request)
                    response.choices.firstOrNull()?.message?.content
                }
                val gson = Gson() // Gson 인스턴스
                val listType = object : TypeToken<FoodAnswer>() {}.type
                val gptAnswer: FoodAnswer = gson.fromJson(chatResponse, listType)

                val newList = messageList.toMutableList()
                val foodList = gptAnswer.foods

                dismissLoadingDialog() // 응답 후 dialog 닫기

                for (food in foodList) {
                    val chatMessage = ChatMessage(
                        "gpt",
                        food.name,
                        food.getGptScript(),
                        ""
                    )
                    newList.add(chatMessage)
                    recentAnswer.add(food.name)
                    chatAdapter.submitList(newList)

                    // 반복문에 지연을 주기 위함 (순차적으로 타이핑 효과를 주기 위해서)
                    val millis = 50 * (food.name.length + food.reason.length).toLong()
                    delay(millis)

                    messageList = newList.toMutableList()
                }
                Log.d(TAG, "getFoodResult: $messageList ")
                showBtn(true)
            } catch (e: Exception) {
                showToast("오류가 발생했어요! 다시 시도해 주세요 \uD83D\uDE25")
                dismissLoadingDialog()
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun getPlaceResult() {
        val jsonString = """
    {
      "places": [
        {
          "name": "__",
          "address": "__",
          "reason": "__",
          "todo": "__"
        }
      ]
    }
    """.trimIndent()

        val request = ChatGptRequest(
            model = "gpt-4o",
            messages = listOf(
                Message(
                    role = "system",
                    content = "당신은 여행을 가서 어디를 갈지 고민하고 있는 사용자에게 최신 트렌드를 반영해 놀러갈 장소들을 추천해 주는" +
                            " 가이드로, 한국에서 유행하는 놀거리를 잘 알고 있으며 친절하게 놀거리를 추천해 주고 소개해 줍니다."
                ),
                Message(
                    role = "user",
                    content = "무조건 $jsonString 이 형식(장소 이름, 주소, 추천 이유, 그 장소에서 할 수 있는 것들)을 사용해서" +
                            "${age}의 한국인 ${gender}이 좋아할 만한 것으로 " +
                            "${placeWith}과 함께 여행을 가는데 $placeType $placeDetail 에서 놀거리와 추천 이유를 100자 이내로 요약해서 $recentAnswer 제외하고 세 가지를 " +
                            " 한국어로 존댓말과 안드로이드에 호환되는 적당한 이모지를 사용하면서 추천해 줘. 이유를 말할 때는 ~하기 때문에 추천드려요, todo는 해시태그처럼 키워드 앞에 #을 붙여서 추천해 줘. " +
                            "최대한 중복되지 않고 트렌디하고 특별한 곳을 추천해 줘 " +
                            "json만 string 형태로 ``` 없이 반환해 주면 돼."
                )
            )
        )

        lifecycleScope.launch {
            try {
                showLoadingDialog()

                val chatResponse = withContext(Dispatchers.IO) {
                    val response = GptApi.chatGptService.getChatResponse(request)
                    response.choices.firstOrNull()?.message?.content
                }
                val gson = Gson() // Gson 인스턴스
                val listType = object : TypeToken<PlaceAnswer>() {}.type
                val gptAnswer: PlaceAnswer = gson.fromJson(chatResponse, listType)

                val newList = messageList.toMutableList()
                val placeList = gptAnswer.places

                dismissLoadingDialog() // 응답 후 dialog 닫기

                for (place in placeList) {
                    val chatMessage = ChatMessage(
                        type = "gpt",
                        name = place.name,
                        gptScript = place.getGptScript(),
                        userScript = "",
                        placeAddress = place.address
                    )
                    newList.add(chatMessage)
                    recentAnswer.add(place.name)
                    chatAdapter.submitList(newList)

                    // 반복문에 지연을 주기 위함 (순차적으로 타이핑 효과를 주기 위해서)
                    val millis = 50 * (place.name.length + place.getGptScript().length).toLong()
                    delay(millis)

                    messageList = newList.toMutableList()
                }
                showBtn(true)
            } catch (e: Exception) {
                showToast("오류가 발생했어요! 다시 시도해 주세요 \uD83D\uDE25")
                dismissLoadingDialog()
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    private suspend fun getClothesResult() {
        val jsonString = """
    {
      "clothes": [
        {
          "name": "__",
          "reason": "__"
        }
      ]
    }
    """.trimIndent()

        lifecycleScope.launch {
            try {
                if (isFirstGPTAnswer) {
                    isFirstGPTAnswer = false

                    clothesWeather = withContext(Dispatchers.IO) {
                        getWeather()
                    }

                    clothesWeatherKorean = weatherDescKo[clothesWeather.weather[0].id]!!
                    clothesTemperature = (clothesWeather.main.temp - 273.15).toInt()
                    Log.d(TAG, "getClothesResult: $clothesTemperature & $clothesWeatherKorean")

                    // 기온 포함된 스크립트
                    gptScript = "님의 취향과 현재 기온에 맞는 스타일을 추천해 드릴게요! \n\n" +
                            "현재 ${clothesAddress}의 기온은 ${clothesTemperature}도 이고 ${clothesWeatherKorean}입니다 😃"

                    messageList.add(ChatMessage("gpt", "", "$nickname $gptScript", ""))

                    chatAdapter.submitList(messageList.toList())
                    chatAdapter.notifyDataSetChanged()
                    delay(50 * (nickname.length + gptScript.length).toLong())
                }

                showLoadingDialog()

                val request = ChatGptRequest(
                    model = "gpt-4o",
                    messages = listOf(
                        Message(
                            role = "system",
                            content = "당신은 특정 지역으로 외출하는 사용자에게 최신 트렌드와 해당 지역의 기온을 반영해 옷과 스타일을 추천해 주는" +
                                    " 숙련된 패션 가이드로, 한국에서 유행하는 패션을 잘 알고 있으며 친절하게 옷을 추천해 주고 소개해 줍니다."
                        ),
                        Message(
                            role = "user",
                            content = "무조건 $jsonString 이 형식(옷 스타일, 추천 이유)을 사용해서 대답해 줘." +
                                    "기온은 ${clothesTemperature}도이고 날씨는 ${clothesWeatherKorean}이야" +
                                    "이런 날에 $clothesType 스타일을 좋아하는 ${age} ${gender}가 맘에 들어할 상의와 하의 조합(무조건 상하의 같이)과 " +
                                    "추천 이유 세 개를 100자 이내로 요약해서 $recentAnswer 제외하고 " +
                                    "한국어로 존댓말과 안드로이드에 호환되는 적당한 이모지를 사용하면서 추천해 줘. " +
                                    "json만 string 형태로 ``` 없이 반환해 주면 돼."
                        )
                    )
                )

                val chatResponse = withContext(Dispatchers.IO) {
                    val response = GptApi.chatGptService.getChatResponse(request)
                    response.choices.firstOrNull()?.message?.content
                }

                val gson = Gson() // Gson 인스턴스 생성
                val listType = object : TypeToken<ClothesAnswer>() {}.type
                val gptAnswer: ClothesAnswer = gson.fromJson(chatResponse, listType)

                val newList = messageList.toMutableList()
                val clothesList = gptAnswer.clothes

                dismissLoadingDialog() // 응답 후 dialog 닫기

                for (clothes in clothesList) {
                    val chatMessage = ChatMessage(
                        "gpt",
                        clothes.name,
                        clothes.getGptScript(),
                        ""
                    )
                    newList.add(chatMessage)
                    recentAnswer.add(clothes.name)
                    chatAdapter.submitList(newList)

                    // 반복문에 지연을 주기 위함 (순차적으로 타이핑 효과를 주기 위해서)
                    val millis = 50 * (clothes.name.length + clothes.getGptScript().length).toLong()
                    delay(millis)

                    messageList = newList.toMutableList()
                }
                showBtn(true)
            } catch (e: Exception) {
                showToast("오류가 발생했어요! 다시 시도해 주세요 \uD83D\uDE25")
                dismissLoadingDialog()
                e.printStackTrace()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private suspend fun getCustomResult() {
        val request = ChatGptRequest(
            model = "gpt-4o",
            messages = listOf(
                Message(
                    role = "system",
                    content = "당신은 사람들이 어려워하고 있는 결정을 대신 해 주는 어시스턴트입니다. 당신은 친절하며 따뜻합니다."
                ),
                Message(
                    role = "user",
                    content = "${customAnswer}은 상대방의 질문이야. 질문에 대한 명확한 대답(결정)이 필요해." +
                            "그리고 네 의견에는 100자 이내의 이유와 함께 말해 줘." +
                            "또한 대답은 존댓말로 안드로이드에 호환되는 적당한 이모지를 사용하면서 해 줬으면 좋겠어."
                )
            )
        )

        lifecycleScope.launch {
            try {
                showLoadingDialog()

                val chatResponse = withContext(Dispatchers.IO) {
                    val response = GptApi.chatGptService.getChatResponse(request)
                    response.choices.firstOrNull()?.message?.content
                }

                val newList = messageList.toMutableList()

                dismissLoadingDialog() // 응답 후 dialog 닫기

                newList.add(ChatMessage("gpt", "", chatResponse, ""))
                chatAdapter.submitList(newList)

                // 반복문에 지연을 주기 위함 (순차적으로 타이핑 효과를 주기 위해서)
                val millis = 50 * (chatResponse?.length?.toLong() ?: 0)
                delay(millis)

                messageList = newList.toMutableList()
            } catch (e: Exception) {
                showToast("오류가 발생했어요! 다시 시도해 주세요 \uD83D\uDE25")
                dismissLoadingDialog()
                e.printStackTrace()
            }
        }
    }

    private fun setChatClickListener() {
        val chatClickListener: ChatListAdapter.ChatClickListener
        if (resultType == FRAGMENT_PLACE) {
            chatClickListener = object : ChatListAdapter.ChatClickListener {
                override fun onClick(address: String?) {
                    if (address != "") {
                        val destinationUri = Uri.parse("geo:0,0?q=$address")
                        val mapIntent = Intent(Intent.ACTION_VIEW, destinationUri)

                        val packageManager = mainActivity.packageManager
                        val activities = packageManager.queryIntentActivities(mapIntent, 0)

                        if (activities.size > 0) {
                            startActivity(mapIntent)
                        }
                    }

                }
            }
        } else {
            chatClickListener = object : ChatListAdapter.ChatClickListener {
                override fun onClick(address: String?) {

                }
            }
        }
        chatAdapter.setChatClickListener(chatClickListener)
    }

    private fun showBtn(isShow: Boolean) {
        if (isShow) {
            binding.resultBtnLl.visibility = View.VISIBLE
        } else {
            binding.resultBtnLl.visibility = View.INVISIBLE
        }
    }

    private val weatherDescKo = mapOf(
        201 to "가벼운 비를 동반한 천둥구름",
        200 to "비를 동반한 천둥구름",
        202 to "폭우를 동반한 천둥구름",
        210 to "약한 천둥구름",
        211 to "천둥구름",
        212 to "강한 천둥구름",
        221 to "불규칙적 천둥구름",
        230 to "약한 연무를 동반한 천둥구름",
        231 to "연무를 동반한 천둥구름",
        232 to "강한 안개비를 동반한 천둥구름",
        300 to "가벼운 안개비",
        301 to "안개비",
        302 to "강한 안개비",
        310 to "가벼운 적은비",
        311 to "적은비",
        312 to "강한 적은비",
        313 to "소나기와 안개비",
        314 to "강한 소나기와 안개비",
        321 to "소나기",
        500 to "악한 비",
        501 to "중간 비",
        502 to "강한 비",
        503 to "매우 강한 비",
        504 to "극심한 비",
        511 to "우박",
        520 to "약한 소나기 비",
        521 to "소나기 비",
        522 to "강한 소나기 비",
        531 to "불규칙적 소나기 비",
        600 to "가벼운 눈",
        601 to "눈",
        602 to "강한 눈",
        611 to "진눈깨비",
        612 to "소나기 진눈깨비",
        615 to "약한 비와 눈",
        616 to "비와 눈",
        620 to "약한 소나기 눈",
        621 to "소나기 눈",
        622 to "강한 소나기 눈",
        701 to "박무",
        711 to "연기",
        721 to "연무",
        731 to "모래 먼지",
        741 to "안개",
        751 to "모래",
        761 to "먼지",
        762 to "화산재",
        771 to "돌풍",
        781 to "토네이도",
        800 to "구름 한 점 없는 맑은 하늘",
        801 to "약간의 구름이 낀 하늘",
        802 to "드문드문 구름이 낀 하늘",
        803 to "구름이 거의 없는 하늘",
        804 to "구름으로 뒤덮인 흐린 하늘",
        900 to "토네이도",
        901 to "태풍",
        902 to "허리케인",
        903 to "한랭",
        904 to "고온",
        905 to "바람부는",
        906 to "우박",
        951 to "바람이 거의 없는",
        952 to "약한 바람",
        953 to "부드러운 바람",
        954 to "중간 세기 바람",
        955 to "신선한 바람",
        956 to "센 바람",
        957 to "돌풍에 가까운 센 바람",
        958 to "돌풍",
        959 to "심각한 돌풍",
        960 to "폭풍",
        961 to "강한 폭풍",
        962 to "허리케인"
    )


}