package com.transservice.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val immatriculation: String,
    val marque: String,
    val modele: String,
    val anneeAcquisition: Int,
    val kilometrageActuel: Double = 0.0
)

@Entity(
    tableName = "trips",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val dateTrajet: Long,
    val depart: String,
    val arrivee: String,
    val distanceKm: Double,
    val montantFacture: Double,
    val notes: String = ""
)

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: Long,
    val litres: Double,
    val prixParLitre: Double,
    val kilometrage: Double
) {
    val montantTotal: Double get() = litres * prixParLitre
}

@Entity(
    tableName = "rentals",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Rental(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val locataire: String,
    val dateDebut: Long,
    val dateFin: Long,
    val tarifJournalier: Double
) {
    val nbJours: Long get() = ((dateFin - dateDebut) / 86_400_000L).coerceAtLeast(1)
    val montantTotal: Double get() = nbJours * tarifJournalier
}

@Entity(
    tableName = "maintenances",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Maintenance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val type: String, // "Entretien" ou "Réparation"
    val date: Long,
    val description: String,
    val cout: Double,
    val prochaineEcheanceKm: Double? = null,
    val prochaineEcheanceDate: Long? = null
)

@Entity(
    tableName = "taxes",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Tax(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val annee: Int,
    val montant: Double,
    val dateEcheance: Long,
    val paye: Boolean = false
)

@Entity(
    tableName = "insurances",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Insurance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val compagnie: String,
    val dateDebut: Long,
    val dateFin: Long,
    val montant: Double
)

@Entity(
    tableName = "inspections",
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = ["id"],
        childColumns = ["vehicleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Inspection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val dateVisite: Long,
    val dateProchaine: Long,
    val resultat: String, // "Favorable" / "Défavorable" / "Contre-visite"
    val cout: Double
)
