package com.example.kleaner.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.lang.reflect.ParameterizedType

abstract class BaseFragment<DB : ViewDataBinding, VM : BaseViewModel> : Fragment() {
    lateinit var dataBinding: DB
    lateinit var viewModel: VM
    private var needRegister: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dataBinding = DataBindingUtil.inflate(layoutInflater, layoutId(), container, false)
        initViewModel()
        needRegister = registerEventBus(javaClass)
        if (needRegister) {
            EventBus.getDefault().register(this)
        }
        return dataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initData()
        initView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (needRegister) {
            EventBus.getDefault().unregister(this)
        }
    }

    private fun initViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val vmClass = type.actualTypeArguments[1] as Class<VM>
            viewModel = ViewModelProviders.of(this).get(vmClass)
        }
        dataBinding.setVariable(viewmodelId(), viewModel)
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

    abstract fun viewmodelId(): Int
}