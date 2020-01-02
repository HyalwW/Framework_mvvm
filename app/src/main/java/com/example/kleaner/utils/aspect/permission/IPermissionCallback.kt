package com.example.kleaner.utils.aspect.permission

interface IPermissionCallback {
    /**
     * 授予权限
     */
    fun granted(requestCode: Int)

    /**
     * 拒绝，isDeniedForever 是否勾选“以后不再提示”
     */
    fun denied(requestCode: Int, isDeniedForever: Boolean)

}