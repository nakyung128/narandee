package com.gotcha.narandee.util

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.gotcha.narandee.databinding.DialogCustomBinding

class CustomDialog(context: Context, private val title: String, private val content: String) :
    Dialog(context) {
    private lateinit var itemClickListener: ItemClickListener
    private lateinit var binding: DialogCustomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initDialog()
        initEvent()

    }

    private fun initDialog() {
        // 배경을 투명하게 (Make the background transparent)
        // 다이얼로그를 둥글게 표현하기 위해 필요 (Required to round corner)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 다이얼로그 바깥쪽 클릭시 종료되도록 함 (Cancel the dialog when you touch outside)
        setCanceledOnTouchOutside(true)

        // 취소 가능 유무
        setCancelable(true)

        binding.dialogTitle.text = title
        binding.dialogContent.text = content
    }

    private fun initEvent() {
        binding.dialogCancel.setOnClickListener {
            dismiss()
        }
        binding.dialogCheck.setOnClickListener {
            itemClickListener.onClick()
            dismiss()
        }
    }

    interface ItemClickListener {
        fun onClick()
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }
}