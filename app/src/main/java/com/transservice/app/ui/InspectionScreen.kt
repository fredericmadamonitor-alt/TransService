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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transservice.app.data.Inspection
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val inspections by viewModel.inspections.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()

    Box(Modifier.fillMaxSize()) {
        if (inspections.isEmpty()) {
            EmptyState("Aucune visite technique enregistrée.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(inspections, key = { it.id }) { i ->
                    val enRetard = i.dateProchaine < now
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(viewModel.nomVehicule(i.vehicleId), fontWeight = FontWeight.Bold)
                                Text("Visite : ${formatDate(i.dateVisite)} — Résultat : ${i.resultat}")
                                Text("Prochaine visite : ${formatDate(i.dateProchaine)}", color = if (enRetard) Color(0xFFC62828) else Color.Unspecified)
                                Text("Coût : ${formatMontant(i.cout)}")
                            }
                            IconButton(onClick = { viewModel.deleteInspection(i) }) {
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
            ) { Icon(Icons.Filled.Add, contentDescription = "Ajouter une visite technique") }
        }
    }

    if (showAddDialog) {
        InspectionFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspectionFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var dateVisite by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var dateProchaine by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var resultat by remember { mutableStateOf("Favorable") }
    var cout by remember { mutableStateOf("") }
    var expandedVehicle by remember { mutableStateOf(false) }
    var expandedResultat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle visite technique") },
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
                OutlinedTextField(dateVisite, { dateVisite = it }, label = { Text("Date de la visite (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateProchaine, { dateProchaine = it }, label = { Text("Prochaine visite (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedResultat, onExpandedChange = { expandedResultat = it }) {
                    OutlinedTextField(
                        value = resultat, onValueChange = {}, readOnly = true, label = { Text("Résultat") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedResultat, onDismissRequest = { expandedResultat = false }) {
                        listOf("Favorable", "Défavorable", "Contre-visite").forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { resultat = r; expandedResultat = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(cout, { cout = it }, label = { Text("Coût") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null) {
                    viewModel.addInspection(
                        Inspection(
                            vehicleId = vId,
                            dateVisite = parseDateOrNull(dateVisite) ?: System.currentTimeMillis(),
                            dateProchaine = parseDateOrNull(dateProchaine) ?: System.currentTimeMillis(),
                            resultat = resultat,
                            cout = cout.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
