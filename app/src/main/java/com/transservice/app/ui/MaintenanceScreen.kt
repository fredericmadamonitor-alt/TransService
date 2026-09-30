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
import com.transservice.app.data.Maintenance
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val maintenances by viewModel.maintenances.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val prochaines = maintenances.filter { it.prochaineEcheanceDate != null || it.prochaineEcheanceKm != null }

    Box(Modifier.fillMaxSize()) {
        if (maintenances.isEmpty()) {
            EmptyState("Aucun entretien ou réparation enregistré.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                if (prochaines.isNotEmpty()) {
                    item {
                        Text("Prochaines échéances", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    items(prochaines, key = { "ech-${it.id}" }) { m ->
                        Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(viewModel.nomVehicule(m.vehicleId), fontWeight = FontWeight.Bold)
                                Text(m.description)
                                m.prochaineEcheanceDate?.let { Text("Échéance : ${formatDate(it)}") }
                                m.prochaineEcheanceKm?.let { Text("Échéance : ${formatNombre(it, 0)} km") }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
                item {
                    Text("Historique (entretiens et réparations)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                }
                items(maintenances, key = { it.id }) { m ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${m.type} — ${viewModel.nomVehicule(m.vehicleId)}", fontWeight = FontWeight.Bold)
                                Text(m.description)
                                Text("${formatDate(m.date)} — ${formatMontant(m.cout)}")
                            }
                            IconButton(onClick = { viewModel.deleteMaintenance(m) }) {
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
            ) { Icon(Icons.Filled.Add, contentDescription = "Ajouter") }
        }
    }

    if (showAddDialog) {
        MaintenanceFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaintenanceFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var type by remember { mutableStateOf("Entretien") }
    var dateText by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var description by remember { mutableStateOf("") }
    var cout by remember { mutableStateOf("") }
    var prochaineKm by remember { mutableStateOf("") }
    var prochaineDate by remember { mutableStateOf("") }
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Entretien / Réparation") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expandedVehicle, onExpandedChange = { expandedVehicle = it }) {
                    OutlinedTextField(
                        value = vehicles.find { it.id == vehicleId }?.immatriculation ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Véhicule") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedVehicle, onDismissRequest = { expandedVehicle = false }) {
                        vehicles.forEach { v ->
                            DropdownMenuItem(text = { Text(v.immatriculation) }, onClick = { vehicleId = v.id; expandedVehicle = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = it }) {
                    OutlinedTextField(
                        value = type, onValueChange = {}, readOnly = true, label = { Text("Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                        listOf("Entretien", "Réparation").forEach { t ->
                            DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateText, { dateText = it }, label = { Text("Date (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(cout, { cout = it }, label = { Text("Coût") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(prochaineKm, { prochaineKm = it }, label = { Text("Prochaine échéance (km, optionnel)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(prochaineDate, { prochaineDate = it }, label = { Text("Prochaine échéance (date, optionnel)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null && description.isNotBlank()) {
                    viewModel.addMaintenance(
                        Maintenance(
                            vehicleId = vId,
                            type = type,
                            date = parseDateOrNull(dateText) ?: System.currentTimeMillis(),
                            description = description,
                            cout = cout.toDoubleOrNull() ?: 0.0,
                            prochaineEcheanceKm = prochaineKm.toDoubleOrNull(),
                            prochaineEcheanceDate = parseDateOrNull(prochaineDate)
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
