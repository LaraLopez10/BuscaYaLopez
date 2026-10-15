package com.example.a2buscaya.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a2buscaya.model.Farmacia
import com.example.a2buscaya.model.LocalBusqueda
import com.example.a2buscaya.ui.theme._2BuscaYaTheme

internal sealed interface BuscaYaFeedRow {
    val key: String

    data class Comercio(val local: LocalBusqueda) : BuscaYaFeedRow {
        override val key = "comercio:${local.id}"
    }

    data class FarmaciaItem(val farmacia: Farmacia) : BuscaYaFeedRow {
        override val key = "farmacia:${farmacia.fechaIso}:${farmacia.nombre}"
    }

    data class Texto(
        override val key: String,
        val texto: String,
        val error: Boolean = false,
    ) : BuscaYaFeedRow
}

/** Lista RecyclerView; las filas reutilizan las tarjetas Compose que ya tenía la app. */
internal class BuscaYaAdapter(
    private val onLocalClick: (LocalBusqueda) -> Unit,
    private val onLocalRemove: (LocalBusqueda) -> Unit,
) : ListAdapter<BuscaYaFeedRow, BuscaYaAdapter.FilaHolder>(DIFERENCIAS) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilaHolder =
        FilaHolder(ComposeView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        })

    override fun onBindViewHolder(holder: FilaHolder, position: Int) {
        holder.bind(getItem(position), onLocalClick, onLocalRemove)
    }

    internal class FilaHolder(private val composeView: ComposeView) : RecyclerView.ViewHolder(composeView) {
        fun bind(
            row: BuscaYaFeedRow,
            onLocalClick: (LocalBusqueda) -> Unit,
            onLocalRemove: (LocalBusqueda) -> Unit,
        ) {
            composeView.setContent {
                _2BuscaYaTheme {
                    when (row) {
                        is BuscaYaFeedRow.Comercio -> LocalCard(
                            local = row.local,
                            onClick = { onLocalClick(row.local) },
                            onRemove = { onLocalRemove(row.local) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 3.dp),
                        )
                        is BuscaYaFeedRow.FarmaciaItem -> FarmaciaCard(
                            farmacia = row.farmacia,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 3.dp),
                        )
                        is BuscaYaFeedRow.Texto -> Text(
                            text = row.texto,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (row.error) MaterialTheme.colorScheme.error else BuscaYaText,
                        )
                    }
                }
            }
        }
    }

    private companion object {
        val DIFERENCIAS = object : DiffUtil.ItemCallback<BuscaYaFeedRow>() {
            override fun areItemsTheSame(oldItem: BuscaYaFeedRow, newItem: BuscaYaFeedRow) =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: BuscaYaFeedRow, newItem: BuscaYaFeedRow) =
                oldItem == newItem
        }
    }
}
