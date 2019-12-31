package com.example.kleaner.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.kleaner.data.models.Bean

@Dao
interface BeanDao : BaseDao<Bean> {
    @Query("select * from bean")
    fun loadLives(): LiveData<MutableList<Bean>>
}