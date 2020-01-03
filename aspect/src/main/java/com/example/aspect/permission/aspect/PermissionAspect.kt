package com.example.aspect.permission.aspect

import android.content.Context
import android.util.Log
import com.example.utils.Utils
import com.example.aspect.permission.IPermissionCallback
import com.example.aspect.permission.annotations.PermissionDenied
import com.example.aspect.permission.annotations.Permissions
import com.example.aspect.permission.utils.PermissionUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

@Aspect
class PermissionAspect {
    @Pointcut("execution(@com.example.aspect.permission.annotations.Permissions * *(..))")
    fun pointcut() {
    }

    @Around("pointcut() && @annotation(permissions)")
    fun permissions(joinPoint: ProceedingJoinPoint, permissions: Permissions) {
        Log.e("wwh", "checkPermissions")
        PermissionActivity.getPermissions(
            getContext(joinPoint),
            permissions.permissions,
            permissions.requestCode,
            object : IPermissionCallback {
                override fun granted(requestCode: Int) {
                    joinPoint.proceed()
                }

                override fun denied(requestCode: Int, isDeniedForever: Boolean) {
                    PermissionUtil.invokeAnnotation(
                        joinPoint.getThis(),
                        PermissionDenied::class.java,
                        requestCode,
                        isDeniedForever
                    )
                }

            })
    }

    private fun getContext(joinPoint: ProceedingJoinPoint): Context? {
        val obj: Any = joinPoint.`this`
        return if (obj is Context) {
            obj
        } else {
            val args = joinPoint.args
            if (args.isNotEmpty()) {
                if (args[0] is Context) {
                    args[0] as Context?
                } else {
                    Utils.context
                }
            } else {
                Utils.context
            }
        }
    }
}