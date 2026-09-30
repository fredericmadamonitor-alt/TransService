package com.transservice.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.transservice.app.viewmodel.TransServiceViewModel
import kotlinx.coroutines.launch

data class MenuItem(val route: String, val label: String, val icon: ImageVector)

private val menuItems = listOf(
    MenuItem("dashboard", "Tableau de bord", Icons.Filled.Home),
    MenuItem("vehicles", "Véhicules", Icons.Filled.DirectionsCar),
    MenuItem("trips", "Trajets", Icons.Filled.Route),
    MenuItem("fuel", "Gasoil", Icons.Filled.LocalGasStation),
    MenuItem("rentals", "Locations", Icons.Filled.CarRental),
    MenuItem("maintenance", "Entretien", Icons.Filled.Build),
    MenuItem("taxes", "Impôts", Icons.Filled.Receipt),
    MenuItem("insurance", "Assurance", Icons.Filled.Shield),
    MenuItem("inspection", "Visite technique", Icons.Filled.FactCheck),
    MenuItem("reports", "Rapports", Icons.Filled.Assessment)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransServiceApp(viewModel: TransServiceViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: "dashboard"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "TRANS' SERVICE",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(20.dp)
                )
                HorizontalDivider()
                Spacer(Modifier.height(4.dp))
                menuItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("dashboard")
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(menuItems.find { it.route == currentRoute }?.label ?: "Trans' Service") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                NavHost(navController = navController, startDestination = "dashboard") {
                    composable("dashboard") { DashboardScreen(viewModel, navController) }
                    composable("vehicles") { VehiclesScreen(viewModel) }
                    composable("trips") { TripsScreen(viewModel) }
                    composable("fuel") { FuelScreen(viewModel) }
                    composable("rentals") { RentalsScreen(viewModel) }
                    composable("maintenance") { MaintenanceScreen(viewModel) }
                    composable("taxes") { TaxesScreen(viewModel) }
                    composable("insurance") { InsuranceScreen(viewModel) }
                    composable("inspection") { InspectionScreen(viewModel) }
                    composable("reports") { ReportsScreen(viewModel) }
                }
            }
        }
    }
}

/** Petit utilitaire réutilisé par les écrans pour un bouton d'ajout flottant + dialogue */
@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}
