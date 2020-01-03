package com.example.aspect.permission.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.collection.SimpleArrayMap
import androidx.core.app.ActivityCompat
import java.lang.reflect.InvocationTargetException

class PermissionUtil {

    companion object StaticMethods {
        private val MIN_SDK_PERMISSIONS: SimpleArrayMap<String, Int> = SimpleArrayMap(8)

        init {
            MIN_SDK_PERMISSIONS.put("com.android.voicemail.permission.ADD_VOICEMAIL", 14)
            MIN_SDK_PERMISSIONS.put("android.permission.BODY_SENSORS", 20)
            MIN_SDK_PERMISSIONS.put("android.permission.READ_CALL_LOG", 16)
            MIN_SDK_PERMISSIONS.put("android.permission.READ_EXTERNAL_STORAGE", 16)
            MIN_SDK_PERMISSIONS.put("android.permission.USE_SIP", 9)
            MIN_SDK_PERMISSIONS.put("android.permission.WRITE_CALL_LOG", 16)
            MIN_SDK_PERMISSIONS.put("android.permission.SYSTEM_ALERT_WINDOW", 23)
            MIN_SDK_PERMISSIONS.put("android.permission.WRITE_SETTINGS", 23)
        }

        /**
         * 判断所有权限是否都同意了，都同意返回true 否则返回false
         *
         * @param context     context
         * @param permissions permission list
         * @return return true if all permissions granted else false
         */
        fun hasSelfPermissions(context: Context, vararg permissions: String): Boolean {
            for (permission in permissions) {
                if (permissionExists(
                        permission
                    ) && !hasSelfPermission(
                        context,
                        permission
                    )
                ) {
                    return false
                }
            }
            return true
        }

        /**
         * 判断单个权限是否同意
         *
         * @param context    context
         * @param permission permission
         * @return return true if permission granted
         */
        private fun hasSelfPermission(context: Context, permission: String): Boolean {
            return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        /**
         * 判断权限是否存在
         *
         * @param permission permission
         * @return return true if permission exists in SDK version
         */
        private fun permissionExists(permission: String): Boolean {
            val minVersion: Int? = MIN_SDK_PERMISSIONS.get(permission)
            return minVersion == null || Build.VERSION.SDK_INT >= minVersion
        }

        /**
         * 检查是否都赋予权限
         *
         * @param grantResults grantResults
         * @return 所有都同意返回true 否则返回false
         */
        fun verifyPermissions(vararg grantResults: Int): Boolean {
            if (grantResults.isEmpty()) return false
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }

        /**
         * 检查所给权限List是否需要给提示
         *
         * @param activity    Activity
         * @param permissions 权限list
         * @return 如果某个权限需要提示则返回true
         */
        fun shouldShowRequestPermissionRationale(
            activity: Activity?,
            vararg permissions: String?
        ): Boolean {
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permission!!)) {
                    return true
                }
            }
            return false
        }


        /**
         * 通过反射调用指定方法
         */
        fun invokeAnnotation(
            obj: Any,
            annotationClass: Class<out Annotation>?,
            requestCode: Int,
            isDeniedForever: Boolean
        ) { //获取切面上下文的类型
            val clazz: Class<*> = obj.javaClass
            //获取类型中的方法
            val methods = clazz.declaredMethods
            if (methods.size == 0) {
                return
            }
            for (method in methods) { //获取该方法是否有 annotationClass 注解
                val isHasAnnotation = method.isAnnotationPresent(annotationClass)
                if (isHasAnnotation) { // 判断是否有且仅有两个参数(int , boolean)
                    val parameterTypes = method.parameterTypes
                    if (parameterTypes.size == 2) {
                        method.isAccessible = true
                        try {
                            method.invoke(obj, requestCode, isDeniedForever)
                        } catch (e: IllegalAccessException) {
                            e.printStackTrace()
                        } catch (e: InvocationTargetException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }


}