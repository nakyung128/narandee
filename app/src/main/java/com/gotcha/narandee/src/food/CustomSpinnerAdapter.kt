package com.gotcha.narandee.src.food

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.gotcha.narandee.databinding.ItemCategoryBinding
import com.gotcha.narandee.databinding.ItemCategorySelectedBinding

private const val TAG = "CustomSpinnerAdapter_싸피"

class CustomSpinnerAdapter(context: Context, list: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, list) {

    private val layoutInflater = LayoutInflater.from(context)
    private var selectedItemPosition = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView == null) {
            ItemCategorySelectedBinding.inflate(layoutInflater)
        } else {
            ItemCategorySelectedBinding.bind(convertView)
        }

        getItem(position)?.let {
            binding.foodCategoryItemTv.text = it
        }

        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (selectedItemPosition == position) {
            ItemCategorySelectedBinding.inflate(layoutInflater)
        } else {
            ItemCategoryBinding.inflate(layoutInflater)
        }

        getItem(position)?.let {
            if (selectedItemPosition == position) {
                (binding as ItemCategorySelectedBinding).foodCategoryItemTv.text = it
            } else {
                (binding as ItemCategoryBinding).foodCategoryItemTv.text = it
            }

        }

        return binding.root
    }

    fun setSelectedItemPosition(position: Int) {
        selectedItemPosition = position
        notifyDataSetChanged() // 선택된 항목을 갱신하기 위해 어댑터를 다시 로드
    }

}