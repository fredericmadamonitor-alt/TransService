package com.transservice.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.transservice.app.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransServiceViewModel(private val repo: Repository) : ViewModel() {

    val vehicles = repo.getVehicles().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val trips = repo.getTrips().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val fuelEntries = repo.getFuelEntries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val rentals = repo.getRentals().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val maintenances = repo.getMaintenances().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val taxes = repo.getTaxes().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val insurances = repo.getInsurances().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val inspections = repo.getInspections().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Véhicules
    fun addVehicle(v: Vehicle) = viewModelScope.launch { repo.addVehicle(v) }
    fun updateVehicle(v: Vehicle) = viewModelScope.launch { repo.updateVehicle(v) }
    fun deleteVehicle(v: Vehicle) = viewModelScope.launch { repo.deleteVehicle(v) }

    // Trajets
    fun addTrip(t: Trip) = viewModelScope.launch { repo.addTrip(t) }
    fun updateTrip(t: Trip) = viewModelScope.launch { repo.updateTrip(t) }
    fun deleteTrip(t: Trip) = viewModelScope.launch { repo.deleteTrip(t) }

    // Gasoil
    fun addFuel(f: FuelEntry) = viewModelScope.launch {
        repo.addFuel(f)
        // Met à jour le kilométrage du véhicule si plus récent
        val vehicle = vehicles.value.find { it.id == f.vehicleId }
        if (vehicle != null && f.kilometrage > vehicle.kilometrageActuel) {
            repo.updateVehicle(vehicle.copy(kilometrageActuel = f.kilometrage))
        }
    }
    fun updateFuel(f: FuelEntry) = viewModelScope.launch { repo.updateFuel(f) }
    fun deleteFuel(f: FuelEntry) = viewModelScope.launch { repo.deleteFuel(f) }

    // Locations
    fun addRental(r: Rental) = viewModelScope.launch { repo.addRental(r) }
    fun updateRental(r: Rental) = viewModelScope.launch { repo.updateRental(r) }
    fun deleteRental(r: Rental) = viewModelScope.launch { repo.deleteRental(r) }

    // Entretien
    fun addMaintenance(m: Maintenance) = viewModelScope.launch { repo.addMaintenance(m) }
    fun updateMaintenance(m: Maintenance) = viewModelScope.launch { repo.updateMaintenance(m) }
    fun deleteMaintenance(m: Maintenance) = viewModelScope.launch { repo.deleteMaintenance(m) }

    // Impôts
    fun addTax(t: Tax) = viewModelScope.launch { repo.addTax(t) }
    fun updateTax(t: Tax) = viewModelScope.launch { repo.updateTax(t) }
    fun deleteTax(t: Tax) = viewModelScope.launch { repo.deleteTax(t) }

    // Assurance
    fun addInsurance(i: Insurance) = viewModelScope.launch { repo.addInsurance(i) }
    fun updateInsurance(i: Insurance) = viewModelScope.launch { repo.updateInsurance(i) }
    fun deleteInsurance(i: Insurance) = viewModelScope.launch { repo.deleteInsurance(i) }

    // Visite technique
    fun addInspection(i: Inspection) = viewModelScope.launch { repo.addInspection(i) }
    fun updateInspection(i: Inspection) = viewModelScope.launch { repo.updateInspection(i) }
    fun deleteInspection(i: Inspection) = viewModelScope.launch { repo.deleteInspection(i) }

    // ---- Calculs pour Rapports / Rentabilité ----

    fun nomVehicule(id: Long): String =
        vehicles.value.find { it.id == id }?.let { "${it.immatriculation} (${it.marque} ${it.modele})" } ?: "?"

    /** Rentabilité par véhicule : recettes trajets+locations - dépenses (gasoil+entretien+impôts+assurance) */
    fun rentabiliteParVehicule(): Map<Long, Double> {
        val result = mutableMapOf<Long, Double>()
        for (v in vehicles.value) {
            val recettesTrajets = trips.value.filter { it.vehicleId == v.id }.sumOf { it.montantFacture }
            val recettesLocations = rentals.value.filter { it.vehicleId == v.id }.sumOf { it.montantTotal }
            val depensesGasoil = fuelEntries.value.filter { it.vehicleId == v.id }.sumOf { it.montantTotal }
            val depensesEntretien = maintenances.value.filter { it.vehicleId == v.id }.sumOf { it.cout }
            val depensesImpots = taxes.value.filter { it.vehicleId == v.id }.sumOf { it.montant }
            val depensesAssurance = insurances.value.filter { it.vehicleId == v.id }.sumOf { it.montant }
            val depensesVisite = inspections.value.filter { it.vehicleId == v.id }.sumOf { it.cout }
            result[v.id] = (recettesTrajets + recettesLocations) -
                (depensesGasoil + depensesEntretien + depensesImpots + depensesAssurance + depensesVisite)
        }
        return result
    }

    fun consommationMoyenne(vehicleId: Long): Double? {
        val entries = fuelEntries.value.filter { it.vehicleId == vehicleId }.sortedBy { it.date }
        if (entries.size < 2) return null
        val distanceTotale = entries.last().kilometrage - entries.first().kilometrage
        if (distanceTotale <= 0) return null
        val litresTotal = entries.drop(1).sumOf { it.litres } // 1er plein exclu (référence)
        return (litresTotal / distanceTotale) * 100.0
    }

    fun coutParKm(vehicleId: Long): Double? {
        val entries = fuelEntries.value.filter { it.vehicleId == vehicleId }.sortedBy { it.date }
        if (entries.size < 2) return null
        val distanceTotale = entries.last().kilometrage - entries.first().kilometrage
        if (distanceTotale <= 0) return null
        val coutTotal = entries.drop(1).sumOf { it.montantTotal }
        return coutTotal / distanceTotale
    }

    fun recettesJour(dateStartMillis: Long, dateEndMillis: Long): Double =
        trips.value.filter { it.dateTrajet in dateStartMillis..dateEndMillis }.sumOf { it.montantFacture } +
        rentals.value.filter { it.dateDebut in dateStartMillis..dateEndMillis }.sumOf { it.montantTotal }

    fun depensesJour(dateStartMillis: Long, dateEndMillis: Long): Double =
        fuelEntries.value.filter { it.date in dateStartMillis..dateEndMillis }.sumOf { it.montantTotal } +
        maintenances.value.filter { it.date in dateStartMillis..dateEndMillis }.sumOf { it.cout }

    companion object {
        fun factory(repo: Repository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    TransServiceViewModel(repo) as T
            }
    }
}
