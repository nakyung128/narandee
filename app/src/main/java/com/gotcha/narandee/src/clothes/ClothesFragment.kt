package com.gotcha.narandee.src.clothes

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.gotcha.narandee.R
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_CLOTHES
import com.gotcha.narandee.config.ApplicationClass.Companion.FRAGMENT_RESULT
import com.gotcha.narandee.config.ApplicationClass.Companion.clothesCategoryList
import com.gotcha.narandee.config.BaseFragment
import com.gotcha.narandee.databinding.FragmentClothesBinding
import com.gotcha.narandee.src.food.CustomSpinnerAdapter
import com.gotcha.narandee.src.main.MainActivity
import com.gotcha.narandee.src.main.MainViewModel
import com.gotcha.narandee.util.PermissionChecker
import com.gotcha.narandee.util.navigate
import java.util.Locale

private const val TAG = "ClothesFragment_싸피"
private const val INPUT_ADDRESS = 1
private const val CURRENT_LOCATION = 2

class ClothesFragment :
    BaseFragment<FragmentClothesBinding>(FragmentClothesBinding::bind, R.layout.fragment_clothes) {

    private lateinit var mainActivity: MainActivity

    private lateinit var selectedClothesType: String

    private var currentLocation = Location(null)
    private var currentAddress = ""

    private val mainActivityViewModel: MainViewModel by activityViewModels()

    private val locationManager by lazy {
        mainActivity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    /** permission check **/
    private val checker = PermissionChecker(this)
    private val runtimePermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /** permission check **/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedClothesType = clothesCategoryList[0]
        initView()
        initObserver()
        initSpinner()
        initEvent()
    }

    private fun initObserver() {
        mainActivityViewModel.userLocation.observe(viewLifecycleOwner) {
            Log.d(TAG, "initObserver: ${it}, ${mainActivityViewModel.userAddress.value}")
            if (isValidLocationAndLocation(it, mainActivityViewModel.userAddress.value!!)) {
                Log.d(TAG, "initObserver: 관찰")
                Log.d(TAG, "initObserver: ${it}, ${mainActivityViewModel.userAddress.value}")
                binding.clothesNextBtn.visibility = View.VISIBLE
                binding.clothesNextImpossibleBtn.visibility = View.GONE

                binding.clothesAddressErrorTv.visibility = View.INVISIBLE
                binding.clothesLocationErrorTv.visibility = View.INVISIBLE

                binding.clothesSelectedAddressTv.text = mainActivityViewModel.userAddress.value
            } else {
                Log.d(TAG, "initObserver: 흠?")
                binding.clothesNextBtn.visibility = View.GONE
                binding.clothesNextImpossibleBtn.visibility = View.VISIBLE

                binding.clothesSelectedAddressTv.text = ""
            }
        }
    }

    private fun initView() {
        Log.d(TAG, "initView: 초기화 되는거니_")
        mainActivity.hideBottomNav(true)

        Log.d(TAG, "initView: ${mainActivityViewModel.userLocation.value}")
        Log.d(TAG, "initView: ${mainActivityViewModel.userAddress.value}")
        if (isValidLocationAndLocation(
                mainActivityViewModel.userLocation.value!!,
                mainActivityViewModel.userAddress.value!!
            )
        ) {
            Log.d(TAG, "initView: hello")
            binding.clothesNextBtn.visibility = View.VISIBLE
            binding.clothesNextImpossibleBtn.visibility = View.GONE

            binding.clothesAddressErrorTv.visibility = View.GONE
            binding.clothesLocationErrorTv.visibility = View.GONE

            binding.clothesSelectedAddressTv.text = mainActivityViewModel.userAddress.value
        } else {
            binding.clothesNextBtn.visibility = View.GONE
            binding.clothesNextImpossibleBtn.visibility = View.VISIBLE

            binding.clothesSelectedAddressTv.text = ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getCurrentLocation() {
        var location: Location? = null

        /* permission check */
        if (!checker.checkPermission(mainActivity, runtimePermissions)) {
            checker.setOnGrantedListener {
                //퍼미션 획득 성공일 때
                location =
                    locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
                Log.d(TAG, "getCurrentLocation: $location")

                checkLocationValid(location)
            }
            checker.requestPermissionLauncher.launch(runtimePermissions)
        } else { //이미 전체 권한이 있는 경우
            location =
                locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)
            Log.d(TAG, "getCurrentLocation-2: $location")
        }
        /* permission check */

        checkLocationValid(location)

    }

    private fun checkLocationValid(location: Location?) {
        if (location != null) {
            currentLocation = location

            if (isValidLocation(currentLocation)) {
                getAddressFromLocation()
                setPossibleView(CURRENT_LOCATION, true)
            } else {
                setPossibleView(CURRENT_LOCATION, false)
            }
        } else {
            initLocationAndAddress()
            setPossibleView(CURRENT_LOCATION, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initEvent() {
        binding.clothesNextImpossibleBtn.setOnClickListener {
            showToast("올바른 위치 정보를 입력해 주세요")
        }

        binding.clothesNextBtn.setOnClickListener {
            mainActivityViewModel.setResultType(FRAGMENT_CLOTHES)
            navigate(R.id.action_clothesFragment_to_resultFragment)

        }

        binding.clothesCheckLocationBtn.setOnClickListener {
            if (binding.clothesLocationEt.text.toString() == "") {
                showToast("주소를 입력해 주세요")
            } else {
                getLocationFromAddress()
            }
        }

        binding.clothesCurrentLocationBtn.setOnClickListener {
            getCurrentLocation()
        }
    }

    private fun getAddressFromLocation() {
        val geocoder = Geocoder(mainActivity, Locale.KOREA)

        geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1)?.get(0)
            ?.let {
                currentAddress = it.getAddressLine(0)
                Log.d(TAG, "getAddressFromLocation: $currentAddress")
            }
        Log.d(TAG, "getAddressFromLocation - end: $currentAddress")

        if (currentAddress != "") {
            val addressSplitList = currentAddress.split(" ")
            val simpleAddress = addressSplitList[2] + " " + addressSplitList[3]
            setViewModelLocationAndAddress(currentLocation, simpleAddress)

            setPossibleView(CURRENT_LOCATION, true)
        } else {
            initLocationAndAddress()
            setPossibleView(CURRENT_LOCATION, false)
        }
    }

    private fun getLocationFromAddress() {
        val inputAddress = binding.clothesLocationEt.text.toString()
        val geocoder = Geocoder(mainActivity, Locale.KOREA)

        val resultLocation = geocoder.getFromLocationName(inputAddress, 1)
        if (resultLocation != null) {
            if (resultLocation.size != 0) {
                currentLocation.latitude = resultLocation[0].latitude
                currentLocation.longitude = resultLocation[0].longitude
                currentAddress = inputAddress
            } else {
                initLocationAndAddress()
                setPossibleView(INPUT_ADDRESS, false)
            }
        } else {
            initLocationAndAddress()
            setPossibleView(INPUT_ADDRESS, false)
        }

        setViewModelLocationAndAddress(currentLocation, currentAddress)

    }

    private fun setPossibleView(type: Int, isPossible: Boolean) {
        when (type) {
            INPUT_ADDRESS -> {
                if (isPossible) {
                    binding.clothesAddressErrorTv.visibility = View.INVISIBLE
                    binding.clothesLocationErrorTv.visibility = View.INVISIBLE

                } else {
                    binding.clothesAddressErrorTv.visibility = View.VISIBLE
                    binding.clothesLocationErrorTv.visibility = View.INVISIBLE
                }
            }

            CURRENT_LOCATION -> {
                if (isPossible) {
                    binding.clothesLocationErrorTv.visibility = View.INVISIBLE
                    binding.clothesAddressErrorTv.visibility = View.INVISIBLE

                    binding.clothesLocationEt.setText(currentAddress)
                } else {
                    binding.clothesLocationErrorTv.visibility = View.VISIBLE
                    binding.clothesAddressErrorTv.visibility = View.INVISIBLE

                    binding.clothesLocationEt.setText("")
                }
            }
        }
    }

    private fun initSpinner() {
        val spinnerClothesCategoryAdapter =
            CustomSpinnerAdapter(mainActivity, clothesCategoryList)
        binding.clothesCategorySpinner.adapter = spinnerClothesCategoryAdapter

        val spinnerClothesItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerClothesCategoryAdapter.setSelectedItemPosition(position)
                selectedClothesType = clothesCategoryList[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

        }

        binding.clothesCategorySpinner.onItemSelectedListener = spinnerClothesItemSelectedListener
    }

    private fun initLocationAndAddress() {
        currentLocation = Location(null)
        currentAddress = ""
        binding.clothesSelectedAddressTv.text = ""

        setViewModelLocationAndAddress(currentLocation, currentAddress)
    }

    private fun isValidLocation(location: Location): Boolean {
        return !(location.latitude == 0.0 || location.longitude == 0.0)
    }

    private fun isValidLocationAndLocation(location: Location, address: String): Boolean {
        return !(location.latitude == 0.0 || location.longitude == 0.0 || address == "")
    }

    private fun setViewModelLocationAndAddress(location: Location, address: String) {
        mainActivityViewModel.setLocationAddress(address)
        mainActivityViewModel.setLocationPreferences(location)
    }
}