package com.example.a2buscaya.data.model

import com.google.gson.annotations.SerializedName

data class FarmaciaTurnoModel(
    @SerializedName("fecha_raw")
    val fechaRaw: String,
    @SerializedName("fecha_iso")
    val fechaIso: String,
    @SerializedName("dia_semana")
    val diaSemana: String,
    val nombre: String,
    val direccion: String,
    val imagen: String,
)
