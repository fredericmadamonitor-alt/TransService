package com.transservice.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.transservice.app.viewmodel.TransServiceViewModel
import java.util.Calendar

data class DashboardStat(val titre: String, val valeur: String, val sousTitre: String)

@Composable
fun DashboardScreen(viewModel: TransServiceViewModel, navController: NavHostController) {
    val vehicles by viewModel.vehicles.collectAsState()
    val trips by viewModel.trips.collectAsState()
    val fuelEntries by viewModel.fuelEntries.collectAsState()
    val rentals by viewModel.rentals.collectAsState()
    val maintenances by viewModel.maintenances.collectAsState()

    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
    val debutMois = cal.timeInMillis
    val finMois = System.currentTimeMillis()

    val recettesMois = trips.filter { it.dateTrajet in debutMois..finMois }.sumOf { it.montantFacture } +
        rentals.filter { it.dateDebut in debutMois..finMois }.sumOf { it.montantTotal }
    val depensesMois = fuelEntries.filter { it.date in debutMois..finMois }.sumOf { it.montantTotal } +
        maintenances.filter { it.date in debutMois..finMois }.sumOf { it.cout }

    val stats = listOf(
        DashboardStat("Véhicules", vehicles.size.toString(), "au total"),
        DashboardStat("Trajets", trips.size.toString(), "enregistrés"),
        DashboardStat("Recettes du mois", formatMontant(recettesMois), "trajets + locations"),
        DashboardStat("Dépenses du mois", formatMontant(depensesMois), "gasoil + entretien"),
        DashboardStat("Solde du mois", formatMontant(recettesMois - depensesMois), "net"),
        DashboardStat("Pleins enregistrés", fuelEntries.size.toString(), "total")
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Vue d'ensemble", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(stats) { stat ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stat.titre, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(stat.valeur, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(stat.sousTitre, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
