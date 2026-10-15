package com.example.a2buscaya.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.a2buscaya.data.local.BuscaYaDatabase
import com.example.a2buscaya.model.Farmacia
import com.example.a2buscaya.model.LocalBusqueda
import com.example.a2buscaya.repository.BuscaYaRepository
import com.example.a2buscaya.repository.ResultadoRefresco
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estados completos que consume la pantalla, siempre construidos con emisiones de Room. */
sealed interface BuscaYaUiState {
    data object Cargando : BuscaYaUiState
    data class Exito(val comercios: List<LocalBusqueda>, val farmacias: List<Farmacia>) : BuscaYaUiState
    data class SinConexion(val comercios: List<LocalBusqueda>, val farmacias: List<Farmacia>) : BuscaYaUiState
    data class Error(
        val mensaje: String,
        val comercios: List<LocalBusqueda>,
        val farmacias: List<Farmacia>,
    ) : BuscaYaUiState
}

private sealed interface Situacion {
    data object Cargando : Situacion
    data object Disponible : Situacion
    data object SinConexion : Situacion
    data class Fallo(val mensaje: String) : Situacion
}

class BuscaYaViewModel(
    private val repository: BuscaYaRepository,
    context: Context,
) : ViewModel() {
    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallbackRegistered = false
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = actualizarEstadoDeRed()
        override fun onLost(network: Network) = actualizarEstadoDeRed()
        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            actualizarEstadoDeRed(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        }
    }

    private val situacion = MutableStateFlow<Situacion>(Situacion.Cargando)
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val comerciosOcultos = MutableStateFlow<Set<String>>(emptySet())
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()
    private val _selectedLocal = MutableStateFlow<LocalBusqueda?>(null)
    val selectedLocal: StateFlow<LocalBusqueda?> = _selectedLocal.asStateFlow()

    /** Cada cambio de texto reemplaza la consulta activa de Room; el filtrado nunca ocurre en memoria. */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val textoConsulta = query
        .debounce(250)
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val comerciosFiltrados = combine(textoConsulta, comerciosOcultos, situacion) { texto, ocultos, estado ->
        texto to if (estado == Situacion.SinConexion) emptySet() else ocultos
    }.flatMapLatest { (texto, ocultos) -> repository.buscarPorNombre(texto, ocultos) }

    /** Combina los resultados de búsqueda de Room con las farmacias y el estado de conexión. */
    val uiState: StateFlow<BuscaYaUiState> = combine(
        comerciosFiltrados,
        repository.observarFarmacias(),
        situacion,
    ) { comercios: List<LocalBusqueda>, farmacias: List<Farmacia>, estado: Situacion ->
        val resultado: BuscaYaUiState = when (estado) {
            Situacion.Cargando -> BuscaYaUiState.Cargando
            Situacion.Disponible -> BuscaYaUiState.Exito(comercios, farmacias)
            Situacion.SinConexion -> BuscaYaUiState.SinConexion(comercios, farmacias)
            is Situacion.Fallo -> BuscaYaUiState.Error(estado.mensaje, comercios, farmacias)
        }
        resultado
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BuscaYaUiState.Cargando)

    init {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        networkCallbackRegistered = true
        refrescar()
    }

    private fun actualizarEstadoDeRed(conexionValidada: Boolean? = null) {
        val conectado = conexionValidada ?: connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } ?: false

        if (conectado && situacion.value == Situacion.SinConexion) {
            situacion.value = Situacion.Disponible
        } else if (!conectado && situacion.value != Situacion.Cargando) {
            situacion.value = Situacion.SinConexion
        }
    }

    override fun onCleared() {
        if (networkCallbackRegistered) connectivityManager.unregisterNetworkCallback(networkCallback)
        super.onCleared()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        _searchError.value = null
    }

    fun refrescar(forzar: Boolean = false) {
        viewModelScope.launch {
            when (val resultado = repository.refrescar(forzar)) {
                ResultadoRefresco.SinCambios, ResultadoRefresco.Exito -> situacion.value = Situacion.Disponible
                ResultadoRefresco.SinConexion -> situacion.value = Situacion.SinConexion
                is ResultadoRefresco.Error -> {
                    _searchError.value = resultado.mensaje
                    situacion.value = Situacion.Fallo(resultado.mensaje)
                }
            }
        }
    }

    fun buscarLocales(query: String = _query.value) {
        if (query.isBlank()) {
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null
            repository.buscarLocales(query)
                .onSuccess {
                    // Conserva la búsqueda activa: la lista se alimenta desde Room y
                    // debe seguir mostrando solo coincidencias de esta consulta.
                    situacion.value = Situacion.Disponible
                }
                .onFailure { error ->
                    if (error is IOException) {
                        // La pantalla ya presenta el estado sin conexión y sigue mostrando Room.
                        _searchError.value = null
                        situacion.value = Situacion.SinConexion
                    } else {
                        _searchError.value = error.localizedMessage ?: "No se pudo realizar la búsqueda."
                        situacion.value = Situacion.Fallo(_searchError.value!!)
                    }
                }
            _isSearching.value = false
        }
    }

    fun seleccionarLocal(local: LocalBusqueda) { _selectedLocal.value = local }
    fun cerrarDetalleLocal() { _selectedLocal.value = null }

    fun ocultarComercio(local: LocalBusqueda) {
        comerciosOcultos.update { actuales -> actuales + local.id }
    }

    /** Proveedor simple para inyectar la base y el repositorio sin agregar Hilt. */
    class Factory(context: Context) : ViewModelProvider.Factory {
        private val appContext = context.applicationContext

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (!modelClass.isAssignableFrom(BuscaYaViewModel::class.java)) {
                throw IllegalArgumentException("ViewModel no soportado: ${modelClass.name}")
            }
            val database = BuscaYaDatabase.getInstance(appContext)
            return BuscaYaViewModel(BuscaYaRepository(appContext, database), appContext) as T
        }
    }
}
