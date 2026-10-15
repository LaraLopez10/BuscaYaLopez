package com.example.a2buscaya.repository

import com.example.a2buscaya.data.model.FarmaciaTurnoModel
import com.example.a2buscaya.data.model.FarmaciaTurnoItemResponse
import com.example.a2buscaya.data.model.FarmaciaTurnoResponse
import com.example.a2buscaya.data.remote.ApiService
import com.example.a2buscaya.data.remote.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.JsonElement

class FarmaciaRepository(
    private val apiService: ApiService = RetrofitInstance.api,
    private val gson: Gson = Gson(),
) {

    suspend fun getFarmaciasTurno(): Result<List<FarmaciaTurnoModel>> {
        return try {
            val farmacias = apiService.getFarmaciasTurno().toFarmaciaModels()
            if (farmacias.isEmpty()) {
                Result.failure(EmptyFarmaciasException())
            } else {
                Result.success(farmacias)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun JsonElement.toFarmaciaModels(): List<FarmaciaTurnoModel> {
        return when {
            isJsonArray -> asJsonArray.map { item ->
                gson.fromJson(item, FarmaciaTurnoItemResponse::class.java).toModel()
            }

            isJsonObject -> {
                val jsonObject = asJsonObject
                val farmaciasElement = jsonObject["farmacias"]
                    ?: jsonObject["data"]
                    ?: jsonObject["resultados"]
                    ?: jsonObject["farmacias_turno"]

                when {
                    farmaciasElement?.isJsonArray == true -> {
                        val response = gson.fromJson(jsonObject, FarmaciaTurnoResponse::class.java)
                        if (response.farmacias.isNotEmpty()) {
                            response.toModels()
                        } else {
                            farmaciasElement.asJsonArray.map { item ->
                                gson.fromJson(item, FarmaciaTurnoItemResponse::class.java)
                                    .toModel(response)
                            }
                        }
                    }

                    jsonObject.has("nombre") -> listOf(
                        gson.fromJson(jsonObject, FarmaciaTurnoItemResponse::class.java).toModel(),
                    )

                    else -> emptyList()
                }
            }

            else -> emptyList()
        }.filter { farmacia ->
            farmacia.nombre.isNotBlank() || farmacia.direccion.isNotBlank()
        }
    }

}

class EmptyFarmaciasException : Exception("No hay farmacias de turno disponibles")
