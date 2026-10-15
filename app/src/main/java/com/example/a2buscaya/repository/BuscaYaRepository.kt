package com.example.a2buscaya.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.withTransaction
import com.example.a2buscaya.api.ApiService
import com.example.a2buscaya.api.RetrofitClient
import com.example.a2buscaya.data.local.BuscaYaDatabase
import com.example.a2buscaya.data.local.FarmaciaEntity
import com.example.a2buscaya.data.local.toEntity
import com.example.a2buscaya.data.local.toUiModel
import com.example.a2buscaya.model.Farmacia
import com.example.a2buscaya.model.LocalBusqueda
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface ResultadoRefresco {
    data object SinCambios : ResultadoRefresco
    data object Exito : ResultadoRefresco
    data object SinConexion : ResultadoRefresco
    data class Error(val mensaje: String) : ResultadoRefresco
}

/** Room es la fuente que observa la interfaz; Retrofit solo actualiza las tablas locales. */
class BuscaYaRepository(
    private val context: Context,
    private val database: BuscaYaDatabase,
    private val apiService: ApiService = RetrofitClient.apiService,
) {
    private val comercios = database.comercioDao()
    private val farmacias = database.farmaciaDao()

    fun observarComercios(): Flow<List<LocalBusqueda>> =
        comercios.obtenerTodos().map { filas -> filas.map { it.toUiModel() } }

    fun observarFarmacias(): Flow<List<Farmacia>> =
        farmacias.obtenerTodos().map { filas -> filas.map { it.toUiModel() } }

    /** Descarga y reemplaza las farmacias únicamente después de recibir una respuesta válida. */
    suspend fun refrescar(forzar: Boolean = false): ResultadoRefresco {
        val farmaciaCount = farmacias.contar()
        val baseVacia = comercios.contar() == 0 && farmaciaCount == 0
        if (!forzar && farmaciaCount > 0) {
            return if (hayInternetValidado()) ResultadoRefresco.SinCambios else ResultadoRefresco.SinConexion
        }
        if (!baseVacia && farmaciaCount == 0 && !forzar && !hayInternetValidado()) {
            return ResultadoRefresco.SinConexion
        }

        return try {
            val recibidas = apiService.getFarmaciasTurno().farmacias
            val filas = recibidas.map { it.toEntity() }
            database.withTransaction {
                farmacias.borrarTodos()
                farmacias.insertarTodos(filas)
            }
            ResultadoRefresco.Exito
        } catch (error: IOException) {
            // No se toca Room: el Flow conserva todos los datos descargados anteriormente.
            ResultadoRefresco.SinConexion
        } catch (error: Exception) {
            ResultadoRefresco.Error(error.localizedMessage ?: "No se pudieron actualizar los datos.")
        }
    }

    /** Guarda en Room una búsqueda nueva sin borrar los resultados previos. */
    suspend fun buscarLocales(query: String): Result<List<LocalBusqueda>> = runCatching {
        val recibidos = apiService.buscarLocales(query.trim()).results
        val filas = recibidos.map { it.toEntity() }
        database.withTransaction { comercios.insertarTodos(filas) }
        recibidos.map { it.toUiModel() }
    }

    fun buscarPorNombre(texto: String, idsOcultos: Set<String> = emptySet()): Flow<List<LocalBusqueda>> {
        // Una base con búsquedas cacheadas no debe llenar la pantalla al abrirla.
        // Solo se consultan los comercios cuando el usuario escribió una búsqueda.
        if (texto.isBlank()) return flowOf(emptyList())

        val filas = if (idsOcultos.isEmpty()) {
            comercios.buscarPorNombre(texto)
        } else {
            comercios.buscarPorNombreExcluyendo(texto, idsOcultos.toList())
        }
        return filas.map { lista -> lista.map { it.toUiModel() } }
    }

    /** El estado validado evita confundir una red Wi-Fi conectada sin acceso a Internet. */
    private fun hayInternetValidado(): Boolean {
        val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val red = gestor.activeNetwork ?: return false
        return gestor.getNetworkCapabilities(red)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }
}
