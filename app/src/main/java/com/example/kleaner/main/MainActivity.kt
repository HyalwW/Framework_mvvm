package com.example.kleaner.main

import android.Manifest
import android.util.Log
import androidx.lifecycle.Observer
import com.example.kleaner.BR
import com.example.kleaner.R
import com.example.kleaner.base.BaseActivity
import com.example.kleaner.data.database.AppDatabase
import com.example.kleaner.data.models.Bean
import com.example.kleaner.databinding.ActivityMainBinding
import com.example.kleaner.utils.aspect.permission.annotataions.PermissionDenied
import com.example.kleaner.utils.aspect.permission.annotataions.Permissions
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {
    override fun layoutId(): Int {
        return R.layout.activity_main
    }

    override fun initData() {
        window.setBackgroundDrawableResource(R.color.colorAccent)
        checkPermissions()
    }

    @Permissions(permissions = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun checkPermissions() {
        Log.e(TAG, "permission granted!!!")
    }

    @PermissionDenied
    private fun permissionDenied(requestCode: Int, isDeniedForever: Boolean) {
        Log.e(TAG, "permission denied,isDeniedForever?$isDeniedForever")
    }

    override fun initView() {
        AppDatabase.getDatabase().beanDao().loadLives().observe(this,
            Observer<MutableList<Bean>> { list ->
                if (list != null) {
                    for (bean in list) {
                        Log.e(TAG, "item" + bean.time)
                    }
                }
            })
    }

    override fun viewModelId(): Int {
        return BR.viewmodel
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receive(str: String) {
        Log.e(TAG, "received String : $str")
    }
}
