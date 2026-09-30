package com.transservice.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Vehicle::class, Trip::class, FuelEntry::class, Rental::class,
        Maintenance::class, Tax::class, Insurance::class, Inspection::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao
    abstract fun fuelDao(): FuelDao
    abstract fun rentalDao(): RentalDao
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun taxDao(): TaxDao
    abstract fun insuranceDao(): InsuranceDao
    abstract fun inspectionDao(): InspectionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "transservice.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
