package com.example.kleaner.utils.aspect.permission.annotataions

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Permissions(val permissions: Array<String>, val requestCode: Int = 0)