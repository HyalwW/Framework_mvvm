package com.example.kleaner.base

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel

abstract class BasePopupWindow<DB : ViewDataBinding, VM : ViewModel>(
    protected var mContext: Context,
    protected var viewModel: VM
) :
    PopupWindow(mContext) {
    protected var dataBinding: DB

    init {
        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(mContext), layoutId(), null, false)
        dataBinding.setVariable(viewmodel(), viewModel)
        initData()
        initView()
        contentView = dataBinding.root
        width = width()
        height = height()
        isFocusable = true
        animationStyle = animStyle()
    }

    /**
     * @param view      展示的依赖view
     * @param direction [.DIRECTION_TOP] 其一
     * @param marginDp  margin
     */
    fun showAtLocation(view: View, direction: Int, marginDp: Float) {
        dataBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = dataBinding.root.measuredWidth
        val popupHeight = dataBinding.root.measuredHeight
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x: Int
        val y: Int
        when (direction) {
            DIRECTION_LEFT -> {
                x = location[0] - popupWidth - dp2px(marginDp)
                y = location[1] + view.height / 2 - popupHeight / 2
            }
            DIRECTION_TOP -> {
                x = location[0] + view.width / 2 - popupWidth / 2
                y = location[1] - popupHeight - dp2px(marginDp)
            }
            DIRECTION_RIGHT -> {
                x = location[0] + view.width + dp2px(marginDp)
                y = location[1] + view.height / 2 - popupHeight / 2
            }
            DIRECTION_BOTTOM -> {
                x = location[0] + view.width / 2 - popupWidth / 2
                y = location[1] + view.height + dp2px(marginDp)
            }
            else -> {
                x = location[0] + view.width / 2 - popupWidth / 2
                y = location[1] + view.height + dp2px(marginDp)
            }
        }
        showAtLocation(view, Gravity.NO_GRAVITY, x, y)
        update()
    }

    private fun dp2px(dpVal: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, mContext.resources.displayMetrics).toInt()
    }

    protected abstract fun layoutId(): Int

    protected abstract fun initData()

    protected abstract fun initView()


    protected abstract fun animStyle(): Int

    protected abstract fun width(): Int

    protected abstract fun height(): Int

    protected abstract fun viewmodel(): Int

    companion object {

        val DIRECTION_TOP = 1
        val DIRECTION_RIGHT = 2
        val DIRECTION_BOTTOM = 3
        val DIRECTION_LEFT = 4
    }
}