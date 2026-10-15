package com.example.a2buscaya.ui

import android.widget.LinearLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a2buscaya.model.Farmacia
import com.example.a2buscaya.model.LocalBusqueda
import com.example.a2buscaya.ui.theme._2BuscaYaTheme
import com.example.a2buscaya.viewmodel.BuscaYaUiState
import com.example.a2buscaya.viewmodel.BuscaYaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaYaScreen(
    viewModel: BuscaYaViewModel = viewModel(factory = BuscaYaViewModel.Factory(LocalContext.current)),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocal by viewModel.selectedLocal.collectAsStateWithLifecycle()
    val comercios = when (state) {
        is BuscaYaUiState.Exito -> (state as BuscaYaUiState.Exito).comercios
        is BuscaYaUiState.SinConexion -> (state as BuscaYaUiState.SinConexion).comercios
        is BuscaYaUiState.Error -> (state as BuscaYaUiState.Error).comercios
        BuscaYaUiState.Cargando -> emptyList()
    }
    val farmacias = when (state) {
        is BuscaYaUiState.Exito -> (state as BuscaYaUiState.Exito).farmacias
        is BuscaYaUiState.SinConexion -> (state as BuscaYaUiState.SinConexion).farmacias
        is BuscaYaUiState.Error -> (state as BuscaYaUiState.Error).farmacias
        BuscaYaUiState.Cargando -> emptyList()
    }
    val rows = remember(state, comercios, farmacias) { crearFilas(comercios, farmacias, state) }
    val adapter = remember(viewModel) {
        BuscaYaAdapter(viewModel::seleccionarLocal, viewModel::ocultarComercio)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BuscaYa") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BuscaYaGreen,
                    titleContentColor = androidx.compose.ui.graphics.Color.White,
                ),
            )
        },
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
            factory = { context ->
                LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)

                    val controls = ComposeView(context).apply {
                        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                        setContent {
                            _2BuscaYaTheme {
                                BuscaYaSearchControls(viewModel)
                            }
                        }
                    }
                    addView(controls, LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ))

                    addView(
                        RecyclerView(context).apply {
                            layoutManager = LinearLayoutManager(context)
                            this.adapter = adapter
                            clipToPadding = false
                            setPadding(dp(context, 14), 0, dp(context, 14), 0)
                        },
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0,
                            1f,
                        ),
                    )
                }
            },
            update = { container ->
                (container.getChildAt(1) as? RecyclerView)?.let { recycler ->
                    if (recycler.adapter !== adapter) recycler.adapter = adapter
                    adapter.submitList(rows)
                }
            },
        )
    }
    selectedLocal?.let { local -> LocalDetalleModal(local = local, onDismiss = viewModel::cerrarDetalleLocal) }
}

@Composable
private fun BuscaYaSearchControls(viewModel: BuscaYaViewModel) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchError by viewModel.searchError.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        if (state is BuscaYaUiState.SinConexion) {
            Text(
                "Sin conexión, mostrando datos guardados",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Text(
            "Encontrá lo que necesitás",
            style = MaterialTheme.typography.headlineSmall,
            color = BuscaYaText,
            modifier = Modifier.padding(top = 12.dp),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            label = { Text("Buscar en comercios guardados") },
            placeholder = { Text("Buscar...") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { viewModel.onQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            },
        )
        Spacer(Modifier.height(6.dp))
        Button(
            onClick = { viewModel.buscarLocales() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSearching && query.isNotBlank(),
        ) {
            if (isSearching) CircularProgressIndicator()
            else Text("Buscar en internet")
        }
        searchError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
        }
        Button(
            onClick = { viewModel.refrescar(forzar = true) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text("Actualizar")
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun crearFilas(
    comercios: List<LocalBusqueda>,
    farmacias: List<Farmacia>,
    state: BuscaYaUiState,
): List<BuscaYaFeedRow> = buildList {
    if (state !is BuscaYaUiState.Cargando) {
        if (comercios.isEmpty()) {
            add(BuscaYaFeedRow.Texto("sin-comercios", "No se encontraron comercios guardados."))
        } else {
            comercios.forEach { add(BuscaYaFeedRow.Comercio(it)) }
        }
    }

    add(BuscaYaFeedRow.Texto("titulo-farmacias", "Farmacias de turno"))
    when (state) {
        BuscaYaUiState.Cargando -> add(BuscaYaFeedRow.Texto("cargando-farmacias", "Cargando…"))
        is BuscaYaUiState.Error -> add(BuscaYaFeedRow.Texto("error-farmacias", state.mensaje, error = true))
        else -> if (farmacias.isEmpty()) {
            add(BuscaYaFeedRow.Texto("sin-farmacias", "No hay farmacias disponibles."))
        }
    }
    farmacias.forEach { add(BuscaYaFeedRow.FarmaciaItem(it)) }
}

private fun dp(context: android.content.Context, value: Int): Int =
    (value * context.resources.displayMetrics.density).toInt()

internal val BuscaYaGreen = androidx.compose.ui.graphics.Color(0xFF16865B)
internal val BuscaYaText = androidx.compose.ui.graphics.Color(0xFF17231E)
internal val BuscaYaMutedText = androidx.compose.ui.graphics.Color(0xFF68756F)
internal val BuscaYaImageBackground = androidx.compose.ui.graphics.Color(0xFFEAF2ED)
