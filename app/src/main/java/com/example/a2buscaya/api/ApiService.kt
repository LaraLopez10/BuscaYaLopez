package com.example.a2buscaya.api

import com.example.a2buscaya.model.FarmaciaResponse
import com.example.a2buscaya.model.LocalBusquedaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/farmacias_turno_android")
    suspend fun getFarmaciasTurno(): FarmaciaResponse

    @GET("api/hybrid-search-android")
    suspend fun buscarLocales(
        @Query("q") query: String,
    ): LocalBusquedaResponse
}
