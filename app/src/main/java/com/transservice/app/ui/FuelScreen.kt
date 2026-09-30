package com.transservice.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transservice.app.data.FuelEntry
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val fuelEntries by viewModel.fuelEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (vehicles.isEmpty()) {
            EmptyState("Ajoutez un véhicule avant d'enregistrer un plein.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                item {
                    Text("Statistiques par véhicule", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }
                items(vehicles, key = { it.id }) { v ->
                    val conso = viewModel.consommationMoyenne(v.id)
                    val cout = viewModel.coutParKm(v.id)
                    val dernierPrix = fuelEntries.filter { it.vehicleId == v.id }.maxByOrNull { it.date }?.prixParLitre
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(v.immatriculation, fontWeight = FontWeight.Bold)
                            Text("Dernier prix/L : ${dernierPrix?.let { formatMontant(it) } ?: "—"}")
                            Text("Consommation moyenne : ${conso?.let { "${formatNombre(it)} L/100km" } ?: "à déterminer (2 pleins min.)"}")
                            Text("Coût par km : ${cout?.let { formatMontant(it) } ?: "à déterminer"}")
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(12.dp))
                    Text("Historique des pleins", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }
                items(fuelEntries, key = { it.id }) { f ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(viewModel.nomVehicule(f.vehicleId), fontWeight = FontWeight.Bold)
                                Text("${formatDate(f.date)} — ${formatNombre(f.litres)} L × ${formatMontant(f.prixParLitre)} = ${formatMontant(f.montantTotal)}")
                                Text("Kilométrage : ${formatNombre(f.kilometrage, 0)} km", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.deleteFuel(f) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }

        if (vehicles.isNotEmpty()) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Nouveau plein") }
        }
    }

    if (showAddDialog) {
        FuelFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FuelFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var dateText by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var litres by remember { mutableStateOf("") }
    var prixParLitre by remember { mutableStateOf("") }
    var kilometrage by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau plein") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = vehicles.find { it.id == vehicleId }?.immatriculation ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Véhicule") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        vehicles.forEach { v ->
                            DropdownMenuItem(text = { Text(v.immatriculation) }, onClick = { vehicleId = v.id; expanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateText, { dateText = it }, label = { Text("Date (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(litres, { litres = it }, label = { Text("Litres") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(prixParLitre, { prixParLitre = it }, label = { Text("Prix par litre") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(kilometrage, { kilometrage = it }, label = { Text("Kilométrage au compteur") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null && litres.isNotBlank() && prixParLitre.isNotBlank()) {
                    viewModel.addFuel(
                        FuelEntry(
                            vehicleId = vId,
                            date = parseDateOrNull(dateText) ?: System.currentTimeMillis(),
                            litres = litres.toDoubleOrNull() ?: 0.0,
                            prixParLitre = prixParLitre.toDoubleOrNull() ?: 0.0,
                            kilometrage = kilometrage.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
