package com.example.a2buscaya.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ComercioDao {
    @Query("SELECT * FROM comercios ORDER BY nombre")
    fun obtenerTodos(): Flow<List<ComercioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(comercios: List<ComercioEntity>)

    @Update
    suspend fun actualizar(comercio: ComercioEntity)

    @Delete
    suspend fun eliminar(comercio: ComercioEntity)

    // Room enlaza :texto como parámetro SQL; el contenido nunca se concatena a la consulta.
    @Query("SELECT * FROM comercios WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    fun buscarPorNombre(texto: String): Flow<List<ComercioEntity>>

    @Query("SELECT * FROM comercios WHERE nombre LIKE '%' || :texto || '%' AND id NOT IN (:idsOcultos) ORDER BY nombre")
    fun buscarPorNombreExcluyendo(texto: String, idsOcultos: List<String>): Flow<List<ComercioEntity>>

    @Query("SELECT COUNT(*) FROM comercios")
    suspend fun contar(): Int

    @Query("DELETE FROM comercios")
    suspend fun borrarTodos()
}

@Dao
interface FarmaciaDao {
    @Query("SELECT * FROM farmacias ORDER BY fechaIso, nombre")
    fun obtenerTodos(): Flow<List<FarmaciaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(farmacias: List<FarmaciaEntity>)

    @Update
    suspend fun actualizar(farmacia: FarmaciaEntity)

    @Delete
    suspend fun eliminar(farmacia: FarmaciaEntity)

    @Query("SELECT * FROM farmacias WHERE nombre LIKE '%' || :texto || '%' ORDER BY nombre")
    fun buscarPorNombre(texto: String): Flow<List<FarmaciaEntity>>

    @Query("SELECT COUNT(*) FROM farmacias")
    suspend fun contar(): Int

    @Query("DELETE FROM farmacias")
    suspend fun borrarTodos()
}
