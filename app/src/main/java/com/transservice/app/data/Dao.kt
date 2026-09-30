package com.transservice.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY immatriculation")
    fun getAll(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles WHERE id = :id")
    suspend fun getById(id: Long): Vehicle?

    @Insert
    suspend fun insert(vehicle: Vehicle): Long

    @Update
    suspend fun update(vehicle: Vehicle)

    @Delete
    suspend fun delete(vehicle: Vehicle)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY dateTrajet DESC")
    fun getAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE vehicleId = :vehicleId ORDER BY dateTrajet DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<Trip>>

    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)

    @Query("SELECT COALESCE(SUM(montantFacture),0) FROM trips WHERE dateTrajet BETWEEN :start AND :end")
    suspend fun sumRevenueBetween(start: Long, end: Long): Double
}

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_entries ORDER BY date DESC")
    fun getAll(): Flow<List<FuelEntry>>

    @Query("SELECT * FROM fuel_entries WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<FuelEntry>>

    @Insert
    suspend fun insert(entry: FuelEntry): Long

    @Update
    suspend fun update(entry: FuelEntry)

    @Delete
    suspend fun delete(entry: FuelEntry)

    @Query("SELECT COALESCE(SUM(litres * prixParLitre),0) FROM fuel_entries WHERE date BETWEEN :start AND :end")
    suspend fun sumCostBetween(start: Long, end: Long): Double
}

@Dao
interface RentalDao {
    @Query("SELECT * FROM rentals ORDER BY dateDebut DESC")
    fun getAll(): Flow<List<Rental>>

    @Query("SELECT * FROM rentals WHERE vehicleId = :vehicleId ORDER BY dateDebut DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<Rental>>

    @Insert
    suspend fun insert(rental: Rental): Long

    @Update
    suspend fun update(rental: Rental)

    @Delete
    suspend fun delete(rental: Rental)
}

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenances ORDER BY date DESC")
    fun getAll(): Flow<List<Maintenance>>

    @Query("SELECT * FROM maintenances WHERE vehicleId = :vehicleId ORDER BY date DESC")
    fun getByVehicle(vehicleId: Long): Flow<List<Maintenance>>

    @Insert
    suspend fun insert(item: Maintenance): Long

    @Update
    suspend fun update(item: Maintenance)

    @Delete
    suspend fun delete(item: Maintenance)
}

@Dao
interface TaxDao {
    @Query("SELECT * FROM taxes ORDER BY dateEcheance DESC")
    fun getAll(): Flow<List<Tax>>

    @Insert
    suspend fun insert(tax: Tax): Long

    @Update
    suspend fun update(tax: Tax)

    @Delete
    suspend fun delete(tax: Tax)
}

@Dao
interface InsuranceDao {
    @Query("SELECT * FROM insurances ORDER BY dateFin DESC")
    fun getAll(): Flow<List<Insurance>>

    @Insert
    suspend fun insert(item: Insurance): Long

    @Update
    suspend fun update(item: Insurance)

    @Delete
    suspend fun delete(item: Insurance)
}

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY dateProchaine DESC")
    fun getAll(): Flow<List<Inspection>>

    @Insert
    suspend fun insert(item: Inspection): Long

    @Update
    suspend fun update(item: Inspection)

    @Delete
    suspend fun delete(item: Inspection)
}
