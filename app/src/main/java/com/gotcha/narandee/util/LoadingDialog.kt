package com.gotcha.narandee.util

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.gotcha.narandee.databinding.LoadingDialogBinding

class LoadingDialog(context: Context) : Dialog(context) {
    private lateinit var binding: LoadingDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = LoadingDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable())
        window!!.setDimAmount(0.9f)
    }

    override fun show() {
        if (!this.isShowing) super.show()
    }
}