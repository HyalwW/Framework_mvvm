package com.example.net

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ParameterizedTypeImpl(private val raw: Class<*>, private var args: Array<Type>) :
    ParameterizedType {

    override fun getActualTypeArguments(): Array<Type> {
        return args
    }

    override fun getRawType(): Type {
        return raw
    }

    override fun getOwnerType(): Type {
        return ParameterizedTypeImpl::class.java
    }
}