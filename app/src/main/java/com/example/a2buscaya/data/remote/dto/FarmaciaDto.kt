package com.example.a2buscaya.data.remote.dto

import com.google.gson.annotations.SerializedName

/** DTO de Retrofit: refleja los nombres de campos que envía el servidor. */
data class FarmaciaDto(
    @SerializedName("fecha_raw") val fechaRaw: String = "",
    @SerializedName("fecha_iso") val fechaIso: String = "",
    @SerializedName("dia_semana") val diaSemana: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val imagen: String = "",
)
