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
import com.transservice.app.data.Trip
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsScreen(viewModel: TransServiceViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Historique", "Rentabilité")
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, label ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(label) })
            }
        }
        Box(Modifier.fillMaxSize()) {
            when (tabIndex) {
                0 -> TripsHistoryTab(viewModel)
                1 -> TripsProfitabilityTab(viewModel)
            }
            if (tabIndex == 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                ) { Icon(Icons.Filled.Add, contentDescription = "Nouveau trajet") }
            }
        }
    }

    if (showAddDialog) {
        TripFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@Composable
private fun TripsHistoryTab(viewModel: TransServiceViewModel) {
    val trips by viewModel.trips.collectAsState()
    if (trips.isEmpty()) {
        EmptyState("Aucun trajet enregistré.")
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(trips, key = { it.id }) { trip ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("${trip.depart} → ${trip.arrivee}", fontWeight = FontWeight.Bold)
                        Text(viewModel.nomVehicule(trip.vehicleId), style = MaterialTheme.typography.bodySmall)
                        Text("${formatDate(trip.dateTrajet)} — ${formatNombre(trip.distanceKm, 0)} km — ${formatMontant(trip.montantFacture)}")
                        if (trip.notes.isNotBlank()) Text(trip.notes, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { viewModel.deleteTrip(trip) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                    }
                }
            }
        }
    }
}

@Composable
private fun TripsProfitabilityTab(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val trips by viewModel.trips.collectAsState()
    if (vehicles.isEmpty()) {
        EmptyState("Ajoutez un véhicule pour voir la rentabilité.")
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(vehicles, key = { it.id }) { v ->
            val vTrips = trips.filter { it.vehicleId == v.id }
            val recettes = vTrips.sumOf { it.montantFacture }
            val distance = vTrips.sumOf { it.distanceKm }
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(v.immatriculation, fontWeight = FontWeight.Bold)
                    Text("${vTrips.size} trajet(s) — ${formatNombre(distance, 0)} km")
                    Text("Recettes trajets : ${formatMontant(recettes)}")
                    if (distance > 0) Text("Recette moyenne / km : ${formatMontant(recettes / distance)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TripFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var dateText by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var depart by remember { mutableStateOf("") }
    var arrivee by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var montant by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau trajet") },
        text = {
            Column {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = vehicles.find { it.id == vehicleId }?.immatriculation ?: "Aucun véhicule",
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
                OutlinedTextField(depart, { depart = it }, label = { Text("Départ") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(arrivee, { arrivee = it }, label = { Text("Arrivée") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(distance, { distance = it }, label = { Text("Distance (km)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(montant, { montant = it }, label = { Text("Montant facturé") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes (optionnel)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null && depart.isNotBlank() && arrivee.isNotBlank()) {
                    viewModel.addTrip(
                        Trip(
                            vehicleId = vId,
                            dateTrajet = parseDateOrNull(dateText) ?: System.currentTimeMillis(),
                            depart = depart, arrivee = arrivee,
                            distanceKm = distance.toDoubleOrNull() ?: 0.0,
                            montantFacture = montant.toDoubleOrNull() ?: 0.0,
                            notes = notes
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
