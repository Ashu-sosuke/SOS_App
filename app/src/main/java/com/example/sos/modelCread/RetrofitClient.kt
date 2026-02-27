package com.example.sos.modelCread

import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://sos-ai-service-413351495429.us-central1.run.app"

    val api: AiApiService by lazy {
        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(AiApiService::class.java)
    }
}
