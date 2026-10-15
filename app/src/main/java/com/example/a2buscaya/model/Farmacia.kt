package com.example.a2buscaya.model

import com.google.gson.annotations.SerializedName
import com.example.a2buscaya.data.remote.dto.FarmaciaDto

data class FarmaciaResponse(
    val farmacias: List<FarmaciaDto> = emptyList(),
)

data class Farmacia(
    @SerializedName("fecha_raw")
    val fechaRaw: String = "",
    @SerializedName("fecha_iso")
    val fechaIso: String = "",
    @SerializedName("dia_semana")
    val diaSemana: String = "",
    val nombre: String = "",
    val direccion: String = "",
    val imagen: String = "",
)
