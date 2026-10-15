package com.example.a2buscaya.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.a2buscaya.model.LocalBusqueda

@Composable
fun LocalDetalleModal(
    local: LocalBusqueda,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 8.dp,
        ) {
            Column {
                Box {
                    AsyncImage(
                        model = local.imagen.takeIf { it.isNotBlank() },
                        contentDescription = local.titulo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .background(BuscaYaImageBackground),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.9f)),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = local.titulo,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = BuscaYaText,
                    )
                    if (local.descripcion.isNotBlank()) {
                        Text(
                            text = local.descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = BuscaYaMutedText,
                        )
                    }
                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        text = local.direccionVisible.ifBlank { "Dirección no disponible" },
                    )
                    if (local.telefono.isNotBlank()) {
                        DetailRow(icon = Icons.Default.Phone, text = local.telefono)
                    }
                    if (local.rating != null) {
                        DetailRow(
                            icon = Icons.Default.Storefront,
                            text = "Valoración ${local.rating}",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = BuscaYaGreen,
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = BuscaYaText,
        )
    }
}
