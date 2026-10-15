package com.example.a2buscaya.data.remote

import com.google.gson.JsonElement
import retrofit2.http.GET

interface ApiService {

    @GET("api/farmacias_turno_android")
    suspend fun getFarmaciasTurno(): JsonElement
}
