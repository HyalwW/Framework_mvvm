package com.example.kleaner.base

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.example.kleaner.R

abstract class BaseAlertDialog<DB : ViewDataBinding, VM : ViewModel>(
    protected var mContext: Activity,
    protected var viewModel: VM
) :
    AlertDialog(mContext) {
    lateinit var dataBinding: DB

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), layoutId(), null, false)
        dataBinding.setVariable(viewModelId(), viewModel)
        setContentView(dataBinding.root)
        initWindow()

    }

    private fun initWindow() {
        val window = window
        if (window != null) {
            window.setWindowAnimations(animId())
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
            window.setBackgroundDrawableResource(R.color.transparent)
            val params = getWindow()!!.attributes
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }

    protected abstract fun layoutId(): Int

    protected abstract fun viewModelId(): Int

    protected abstract fun animId(): Int
}
