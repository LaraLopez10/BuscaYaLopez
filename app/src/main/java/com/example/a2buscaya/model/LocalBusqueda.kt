package com.example.a2buscaya.model

import com.google.gson.annotations.SerializedName
import com.example.a2buscaya.data.remote.dto.LocalBusquedaDto

data class LocalBusquedaResponse(
    val results: List<LocalBusquedaDto> = emptyList(),
    val total: Int = 0,
    val message: String? = null,
)

data class LocalBusqueda(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val imagen: String = "",
    @SerializedName("negocio_nombre")
    val negocioNombre: String = "",
    @SerializedName("email_contacto")
    val emailContacto: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val ubicacion: String = "",
    val rating: Double? = null,
    @SerializedName("place_id")
    val placeId: String = "",
) {
    val titulo: String
        get() = nombre.ifBlank { negocioNombre }

    val direccionVisible: String
        get() = direccion.ifBlank { ubicacion }
}
