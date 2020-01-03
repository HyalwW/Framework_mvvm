package com.example.aspect.permission.aspect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aspect.permission.IPermissionCallback
import com.example.aspect.permission.utils.PermissionUtil

class PermissionActivity : AppCompatActivity() {

    companion object StaticMethods {
        private const val permissionsTag = "permissions"
        private const val requestCodeTag = "requestCode"
        private var mCallback: IPermissionCallback? = null
        fun getPermissions(
            context: Context?,
            permissions: Array<out String>,
            requestCode: Int,
            callback: IPermissionCallback?
        ) {
            if (context == null) return
            mCallback = callback
            if (PermissionUtil.hasSelfPermissions(context, *permissions)) {
                mCallback?.granted(requestCode)
                return
            }
            //启动当前这个Activiyt并且取消切换动画
            val intent = Intent(context, PermissionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP //开启新的任务栈并且清除栈顶...为何要清除栈顶

            intent.putExtra(permissionsTag, permissions)
            intent.putExtra(requestCodeTag, requestCode)

            context.startActivity(intent) //利用context启动activity


            if (context is Activity) { //并且，如果是activity启动的，那么还要屏蔽掉activity切换动画
                context.overridePendingTransition(0, 0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val permissions = intent.getStringArrayExtra(permissionsTag)
        val requestCode = intent.getIntExtra(requestCodeTag, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //现在拿到了权限的申请结果，那么如何处理，我这个Activity只是为了申请，然后把结果告诉外界，所以结果的处理只能是外界传进来
        val granted: Boolean = PermissionUtil.verifyPermissions(*grantResults)
        if (granted) {
            mCallback?.granted(requestCode)
        } else {
            mCallback?.denied(requestCode, !PermissionUtil.shouldShowRequestPermissionRationale(this, *permissions))
        }
        finish()
        overridePendingTransition(0, 0)
    }
}
