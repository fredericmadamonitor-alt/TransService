package com.transservice.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transservice.app.data.Vehicle
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleForHistory by remember { mutableStateOf<Vehicle?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (vehicles.isEmpty()) {
            EmptyState("Aucun véhicule. Appuyez sur + pour en ajouter un.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(vehicles, key = { it.id }) { vehicle ->
                    Card(
                        Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            .clickable { vehicleForHistory = vehicle }
                    ) {
                        Row(
                            Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(vehicle.immatriculation, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("${vehicle.marque} ${vehicle.modele} — ${vehicle.anneeAcquisition}")
                                Text("Kilométrage : ${formatNombre(vehicle.kilometrageActuel, 0)} km", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { vehicleToEdit = vehicle }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Modifier")
                            }
                            IconButton(onClick = { viewModel.deleteVehicle(vehicle) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Ajouter un véhicule") }
    }

    if (showAddDialog) {
        VehicleFormDialog(
            title = "Ajouter un véhicule",
            initial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { viewModel.addVehicle(it); showAddDialog = false }
        )
    }

    vehicleToEdit?.let { v ->
        VehicleFormDialog(
            title = "Modifier le véhicule",
            initial = v,
            onDismiss = { vehicleToEdit = null },
            onConfirm = { viewModel.updateVehicle(it.copy(id = v.id)); vehicleToEdit = null }
        )
    }

    vehicleForHistory?.let { v ->
        VehicleHistoryDialog(viewModel = viewModel, vehicle = v, onDismiss = { vehicleForHistory = null })
    }
}

@Composable
private fun VehicleFormDialog(
    title: String,
    initial: Vehicle?,
    onDismiss: () -> Unit,
    onConfirm: (Vehicle) -> Unit
) {
    var immatriculation by remember { mutableStateOf(initial?.immatriculation ?: "") }
    var marque by remember { mutableStateOf(initial?.marque ?: "") }
    var modele by remember { mutableStateOf(initial?.modele ?: "") }
    var annee by remember { mutableStateOf(initial?.anneeAcquisition?.toString() ?: "") }
    var kilometrage by remember { mutableStateOf(initial?.kilometrageActuel?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(immatriculation, { immatriculation = it }, label = { Text("Immatriculation") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(marque, { marque = it }, label = { Text("Marque") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(modele, { modele = it }, label = { Text("Modèle") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(annee, { annee = it }, label = { Text("Année d'acquisition") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(kilometrage, { kilometrage = it }, label = { Text("Kilométrage actuel (km)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (immatriculation.isNotBlank()) {
                    onConfirm(
                        Vehicle(
                            immatriculation = immatriculation,
                            marque = marque,
                            modele = modele,
                            anneeAcquisition = annee.toIntOrNull() ?: 0,
                            kilometrageActuel = kilometrage.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
private fun VehicleHistoryDialog(
    viewModel: TransServiceViewModel,
    vehicle: Vehicle,
    onDismiss: () -> Unit
) {
    val trips by viewModel.trips.collectAsState()
    val fuel by viewModel.fuelEntries.collectAsState()
    val maintenances by viewModel.maintenances.collectAsState()

    val vTrips = trips.filter { it.vehicleId == vehicle.id }
    val vFuel = fuel.filter { it.vehicleId == vehicle.id }
    val vMaintenance = maintenances.filter { it.vehicleId == vehicle.id }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Historique — ${vehicle.immatriculation}") },
        text = {
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                item { Text("Trajets (${vTrips.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp)) }
                items(vTrips) { t ->
                    Text("${formatDate(t.dateTrajet)} — ${t.depart} → ${t.arrivee} : ${formatMontant(t.montantFacture)}", style = MaterialTheme.typography.bodySmall)
                }
                item { Spacer(Modifier.height(10.dp)); Text("Pleins de gasoil (${vFuel.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp)) }
                items(vFuel) { f ->
                    Text("${formatDate(f.date)} — ${formatNombre(f.litres)} L à ${formatMontant(f.prixParLitre)}/L = ${formatMontant(f.montantTotal)}", style = MaterialTheme.typography.bodySmall)
                }
                item { Spacer(Modifier.height(10.dp)); Text("Entretiens / Réparations (${vMaintenance.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp)) }
                items(vMaintenance) { m ->
                    Text("${formatDate(m.date)} — ${m.type} : ${m.description} (${formatMontant(m.cout)})", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}
