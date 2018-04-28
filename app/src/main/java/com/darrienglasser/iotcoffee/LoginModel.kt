package com.darrienglasser.iotcoffee

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface LoginModel {

    @POST("/login")
    @FormUrlEncoded
    fun login(@Field("email") email: String,
              @Field("password") password: String): Call<Msg>

    @POST("/register")
    @FormUrlEncoded
    fun register(@Field("email") email: String,
                 @Field("password") password: String): Call<Msg>
}

data class Msg(val msg: String)