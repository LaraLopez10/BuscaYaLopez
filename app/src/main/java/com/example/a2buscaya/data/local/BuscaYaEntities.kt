package com.example.a2buscaya.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.a2buscaya.model.Farmacia
import com.example.a2buscaya.model.LocalBusqueda
import com.example.a2buscaya.data.remote.dto.FarmaciaDto
import com.example.a2buscaya.data.remote.dto.LocalBusquedaDto

/** Fila local de un comercio; se mantiene separada del DTO que Retrofit recibe. */
@Entity(tableName = "comercios")
data class ComercioEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: String,
    val imagen: String,
    val negocioNombre: String,
    val emailContacto: String,
    val telefono: String,
    val direccion: String,
    val ubicacion: String,
    val rating: Double?,
    val placeId: String,
    val fechaActualizacion: Long,
)

/** Fila local de una farmacia de turno. */
@Entity(tableName = "farmacias")
data class FarmaciaEntity(
    @PrimaryKey val id: String,
    val fechaRaw: String,
    val fechaIso: String,
    val diaSemana: String,
    val nombre: String,
    val direccion: String,
    val imagen: String,
    val fechaActualizacion: Long,
)

/** Convierte el modelo recibido por Retrofit en una fila persistible. */
fun LocalBusquedaDto.toEntity(fechaActualizacion: Long = System.currentTimeMillis()) =
    ComercioEntity(
        id = id.ifBlank { placeId.ifBlank { "$nombre-$direccion" } },
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        imagen = imagen,
        negocioNombre = negocioNombre,
        emailContacto = emailContacto,
        telefono = telefono,
        direccion = direccion,
        ubicacion = ubicacion,
        rating = rating,
        placeId = placeId,
        fechaActualizacion = fechaActualizacion,
    )

/** Convierte una fila de Room al modelo que ya utiliza Compose. */
fun ComercioEntity.toUiModel() = LocalBusqueda(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    imagen = imagen,
    negocioNombre = negocioNombre,
    emailContacto = emailContacto,
    telefono = telefono,
    direccion = direccion,
    ubicacion = ubicacion,
    rating = rating,
    placeId = placeId,
)

/** Mapea explícitamente el DTO de Retrofit al modelo que consume la interfaz. */
fun LocalBusquedaDto.toUiModel() = LocalBusqueda(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    imagen = imagen,
    negocioNombre = negocioNombre,
    emailContacto = emailContacto,
    telefono = telefono,
    direccion = direccion,
    ubicacion = ubicacion,
    rating = rating,
    placeId = placeId,
)

fun FarmaciaDto.toEntity(fechaActualizacion: Long = System.currentTimeMillis()) =
    FarmaciaEntity(
        id = "${fechaIso}_${nombre}",
        fechaRaw = fechaRaw,
        fechaIso = fechaIso,
        diaSemana = diaSemana,
        nombre = nombre,
        direccion = direccion,
        imagen = imagen,
        fechaActualizacion = fechaActualizacion,
    )

fun FarmaciaEntity.toUiModel() = Farmacia(
    fechaRaw = fechaRaw,
    fechaIso = fechaIso,
    diaSemana = diaSemana,
    nombre = nombre,
    direccion = direccion,
    imagen = imagen,
)

fun FarmaciaDto.toUiModel() = Farmacia(
    fechaRaw = fechaRaw,
    fechaIso = fechaIso,
    diaSemana = diaSemana,
    nombre = nombre,
    direccion = direccion,
    imagen = imagen,
)
