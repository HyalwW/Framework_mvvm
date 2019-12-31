package com.example.kleaner.data.database

import androidx.room.*

@Dao
interface BaseDao<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(bean: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(beans: MutableList<T>)

    @Delete
    fun deleteList(elements: MutableList<T>)

    @Delete
    fun deleteSome(vararg elements: T)

    @Update
    fun update(element: T)
}