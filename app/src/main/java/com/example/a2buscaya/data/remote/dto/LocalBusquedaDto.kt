package com.example.a2buscaya.data.remote.dto

import com.google.gson.annotations.SerializedName

/** DTO de Retrofit para un comercio; se convierte antes de guardarse o mostrarse. */
data class LocalBusquedaDto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val imagen: String = "",
    @SerializedName("negocio_nombre") val negocioNombre: String = "",
    @SerializedName("email_contacto") val emailContacto: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val ubicacion: String = "",
    val rating: Double? = null,
    @SerializedName("place_id") val placeId: String = "",
)
