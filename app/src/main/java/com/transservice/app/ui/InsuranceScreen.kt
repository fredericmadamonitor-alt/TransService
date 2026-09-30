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
import com.transservice.app.data.Insurance
import com.transservice.app.viewmodel.TransServiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsuranceScreen(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val insurances by viewModel.insurances.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val now = System.currentTimeMillis()

    Box(Modifier.fillMaxSize()) {
        if (insurances.isEmpty()) {
            EmptyState("Aucune assurance enregistrée.")
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                items(insurances, key = { it.id }) { i ->
                    val expiree = i.dateFin < now
                    Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(viewModel.nomVehicule(i.vehicleId), fontWeight = FontWeight.Bold)
                                Text("Compagnie : ${i.compagnie}")
                                Text("${formatDate(i.dateDebut)} → ${formatDate(i.dateFin)}")
                                Text("Montant : ${formatMontant(i.montant)}")
                                if (expiree) Text("Expirée", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.deleteInsurance(i) }) {
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
            ) { Icon(Icons.Filled.Add, contentDescription = "Ajouter une assurance") }
        }
    }

    if (showAddDialog) {
        InsuranceFormDialog(viewModel = viewModel, onDismiss = { showAddDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InsuranceFormDialog(viewModel: TransServiceViewModel, onDismiss: () -> Unit) {
    val vehicles by viewModel.vehicles.collectAsState()
    var vehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id) }
    var compagnie by remember { mutableStateOf("") }
    var dateDebut by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var dateFin by remember { mutableStateOf(formatDate(System.currentTimeMillis())) }
    var montant by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle assurance") },
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
                OutlinedTextField(compagnie, { compagnie = it }, label = { Text("Compagnie d'assurance") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateDebut, { dateDebut = it }, label = { Text("Date début (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dateFin, { dateFin = it }, label = { Text("Date fin (jj/mm/aaaa)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(montant, { montant = it }, label = { Text("Montant") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val vId = vehicleId
                if (vId != null && compagnie.isNotBlank()) {
                    viewModel.addInsurance(
                        Insurance(
                            vehicleId = vId,
                            compagnie = compagnie,
                            dateDebut = parseDateOrNull(dateDebut) ?: System.currentTimeMillis(),
                            dateFin = parseDateOrNull(dateFin) ?: System.currentTimeMillis(),
                            montant = montant.toDoubleOrNull() ?: 0.0
                        )
                    )
                    onDismiss()
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
