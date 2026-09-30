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
import com.transservice.app.data.Rental
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val rentals by viewModel.rentals.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (rentals.isEmpty()) {
            EmptyState("Aucune location enregistrée.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(rentals, key = { it.id }) { r ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(viewModel.nomVehicule(r.vehicleId), fontWeight = FontWeight.Bold)
                                Text("Locataire : ${r.locataire}")
                                Text("${formatDate(r.dateDebut)} → ${formatDate(r.dateFin)} (${r.nbJours} j)")
                                Text("Tarif : ${formatMontant(r.tarifJournalier)}/j — Total : ${formatMontant(r.montantTotal)}")
                            }
                            IconButton(onClick = { viewModel.deleteRental(r) }) {
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
            ) { Icon(Icons.Filled.Add, contentDescription = "Nouvelle location") }
        }
    }

    if (showAddDialog) {
        RentalFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RentalFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var locataire by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var dateFin by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var tarif by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle location") },
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
                OutlinedTextField(locataire, { locataire = it }, label = { Text("Locataire") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateDebut, { dateDebut = it }, label = { Text("Date début (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateFin, { dateFin = it }, label = { Text("Date fin (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(tarif, { tarif = it }, label = { Text("Tarif journalier") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null && locataire.isNotBlank()) {
                    viewModel.addRental(
                        Rental(
                            vehicleId = vId,
                            locataire = locataire,
                            dateDebut = parseDateOrNull(dateDebut) ?: System.currentTimeMillis(),
                            dateFin = parseDateOrNull(dateFin) ?: System.currentTimeMillis(),
                            tarifJournalier = tarif.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
