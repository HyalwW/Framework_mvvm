package com.example.net.beans

import com.google.gson.annotations.SerializedName

/**
 * 根据项目实际需求修改该实体
 */
class BaseResponse<T> {
    @SerializedName("code")
    var code = -1
    @SerializedName("msg")
    var msg: String? = null
    @SerializedName("data")
    var t: T? = null

}