package com.example.a2buscaya.data.model

import com.google.gson.annotations.SerializedName

data class FarmaciaTurnoResponse(
    @SerializedName("fecha_raw")
    val fechaRaw: String? = null,
    @SerializedName("fecha_iso")
    val fechaIso: String? = null,
    @SerializedName("dia_semana")
    val diaSemana: String? = null,
    val farmacias: List<FarmaciaTurnoItemResponse> = emptyList(),
) {
    fun toModels(): List<FarmaciaTurnoModel> {
        return farmacias.map { farmacia ->
            farmacia.toModel(this)
        }
    }
}

data class FarmaciaTurnoItemResponse(
    @SerializedName("fecha_raw")
    val fechaRaw: String? = null,
    @SerializedName("fecha_iso")
    val fechaIso: String? = null,
    @SerializedName("dia_semana")
    val diaSemana: String? = null,
    val nombre: String? = null,
    val direccion: String? = null,
    val imagen: String? = null,
) {
    fun toModel(response: FarmaciaTurnoResponse? = null): FarmaciaTurnoModel {
        return FarmaciaTurnoModel(
            fechaRaw = fechaRaw.orEmpty().ifBlank { response?.fechaRaw.orEmpty() },
            fechaIso = fechaIso.orEmpty().ifBlank { response?.fechaIso.orEmpty() },
            diaSemana = diaSemana.orEmpty().ifBlank { response?.diaSemana.orEmpty() },
            nombre = nombre.orEmpty(),
            direccion = direccion.orEmpty(),
            imagen = imagen.orEmpty(),
        )
    }
}
