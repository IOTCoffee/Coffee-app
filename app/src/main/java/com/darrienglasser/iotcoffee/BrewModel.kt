package com.darrienglasser.iotcoffee

import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BrewModel {
    @POST("/setbrew")
    @FormUrlEncoded
    fun setBrew(@Field("email") email: String,
                @Field("token") token: String,
                @Field("url") url: String,
                @Field("name") name: String): Call<Msg>

    @POST("/brew")
    @FormUrlEncoded
    fun brew(@Field("email") email: String,
             @Field("token") token: String): Call<Msg>

    @POST("/pingcoffee")
    @FormUrlEncoded
    fun brewPing(@Field("email") email: String,
                 @Field("token") token: String): Call<Msg>
}
