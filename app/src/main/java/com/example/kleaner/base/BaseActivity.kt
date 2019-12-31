package com.example.kleaner.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProviders
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {
    protected val TAG = javaClass.simpleName

    lateinit var dataBinding: DB
    lateinit var viewModel: VM
    private var needRegister: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this, layoutId())
        initViewModel()
        initData()
        initView()
        needRegister = registerEventBus(javaClass)
        Log.e(TAG, "needRegister ? $needRegister")
        if (needRegister) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needRegister) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun initViewModel() {
        val viewModelId = viewModelId()
        val type: Type? = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val vmClass = type.actualTypeArguments[1] as Class<VM>
            viewModel = ViewModelProviders.of(this).get(vmClass)
        }
        dataBinding.setVariable(viewModelId, viewModel)
    }

    private fun registerEventBus(clazz: Class<*>): Boolean {
        needRegister = false
        for (method in clazz.declaredMethods) {
            if (method.isAnnotationPresent(Subscribe::class.javaObjectType)) {
                return true
            }
        }
        if (clazz.superclass != null) {
            return registerEventBus(clazz.superclass)
        }
        return false
    }

    abstract fun layoutId(): Int

    abstract fun initData()

    abstract fun initView()

    abstract fun viewModelId(): Int
}