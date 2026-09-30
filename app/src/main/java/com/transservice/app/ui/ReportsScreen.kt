package com.transservice.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.transservice.app.viewmodel.TransServiceViewModel
import java.util.Calendar

@Composable
fun ReportsScreen(viewModel: TransServiceViewModel) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Journalier", "Mensuel", "Par véhicule")

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tabIndex) {
            tabs.forEachIndexed { i, label ->
                Tab(selected = tabIndex == i, onClick = { tabIndex = i }, text = { Text(label) })
            }
        }
        when (tabIndex) {
            0 -> DailyReportTab(viewModel)
            1 -> MonthlyReportTab(viewModel)
            2 -> PerVehicleReportTab(viewModel)
        }
    }
}

@Composable
private fun ReportCard(recettes: Double, depenses: Double) {
    val solde = recettes - depenses
    Card(Modifier.fillMaxWidth().padding(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Recettes : ${formatMontant(recettes)}")
            Text("Dépenses : ${formatMontant(depenses)}")
            Spacer(Modifier.height(6.dp))
            Text(
                "Solde net : ${formatMontant(solde)}",
                fontWeight = FontWeight.Bold,
                color = if (solde >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
    }
}

@Composable
private fun DailyReportTab(viewModel: TransServiceViewModel) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
    val debutJour = cal.timeInMillis
    val finJour = debutJour + 86_400_000L - 1

    val recettes = viewModel.recettesJour(debutJour, finJour)
    val depenses = viewModel.depensesJour(debutJour, finJour)

    Column(Modifier.fillMaxWidth()) {
        Text("Rapport du jour — ${formatDate(debutJour)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 12.dp))
        ReportCard(recettes, depenses)
    }
}

@Composable
private fun MonthlyReportTab(viewModel: TransServiceViewModel) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
    val debutMois = cal.timeInMillis
    val finMois = System.currentTimeMillis()

    val recettes = viewModel.recettesJour(debutMois, finMois)
    val depenses = viewModel.depensesJour(debutMois, finMois)

    Column(Modifier.fillMaxWidth()) {
        Text("Rapport du mois en cours", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp, top = 12.dp))
        ReportCard(recettes, depenses)
    }
}

@Composable
private fun PerVehicleReportTab(viewModel: TransServiceViewModel) {
    val vehicles by viewModel.vehicles.collectAsState()
    val rentabilite = viewModel.rentabiliteParVehicule()

    if (vehicles.isEmpty()) {
        EmptyState("Ajoutez un véhicule pour voir son rapport.")
        return
    }

    LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
        items(vehicles, key = { it.id }) { v ->
            val solde = rentabilite[v.id] ?: 0.0
            val conso = viewModel.consommationMoyenne(v.id)
            val coutKm = viewModel.coutParKm(v.id)
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("${v.immatriculation} — ${v.marque} ${v.modele}", fontWeight = FontWeight.Bold)
                    Text("Kilométrage actuel : ${formatNombre(v.kilometrageActuel, 0)} km")
                    Text("Consommation moyenne : ${conso?.let { "${formatNombre(it)} L/100km" } ?: "—"}")
                    Text("Coût par km : ${coutKm?.let { formatMontant(it) } ?: "—"}")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Rentabilité globale : ${formatMontant(solde)}",
                        fontWeight = FontWeight.Bold,
                        color = if (solde >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}
