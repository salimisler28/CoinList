package com.example.coinlist.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val API_KEY = "CG-2vvZUCbVyJ96EMo3PEMzsDk1"

val client = Retrofit.Builder()
    .client(OkHttpClient())
    .baseUrl("https://api.coingecko.com/api/v3/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()