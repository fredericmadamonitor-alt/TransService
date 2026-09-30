package com.transservice.app.data

class Repository(db: AppDatabase) {
    val vehicleDao = db.vehicleDao()
    val tripDao = db.tripDao()
    val fuelDao = db.fuelDao()
    val rentalDao = db.rentalDao()
    val maintenanceDao = db.maintenanceDao()
    val taxDao = db.taxDao()
    val insuranceDao = db.insuranceDao()
    val inspectionDao = db.inspectionDao()

    // Véhicules
    fun getVehicles() = vehicleDao.getAll()
    suspend fun addVehicle(v: Vehicle) = vehicleDao.insert(v)
    suspend fun updateVehicle(v: Vehicle) = vehicleDao.update(v)
    suspend fun deleteVehicle(v: Vehicle) = vehicleDao.delete(v)

    // Trajets
    fun getTrips() = tripDao.getAll()
    suspend fun addTrip(t: Trip) = tripDao.insert(t)
    suspend fun updateTrip(t: Trip) = tripDao.update(t)
    suspend fun deleteTrip(t: Trip) = tripDao.delete(t)

    // Gasoil
    fun getFuelEntries() = fuelDao.getAll()
    suspend fun addFuel(f: FuelEntry) = fuelDao.insert(f)
    suspend fun updateFuel(f: FuelEntry) = fuelDao.update(f)
    suspend fun deleteFuel(f: FuelEntry) = fuelDao.delete(f)

    // Locations
    fun getRentals() = rentalDao.getAll()
    suspend fun addRental(r: Rental) = rentalDao.insert(r)
    suspend fun updateRental(r: Rental) = rentalDao.update(r)
    suspend fun deleteRental(r: Rental) = rentalDao.delete(r)

    // Entretien / Réparation
    fun getMaintenances() = maintenanceDao.getAll()
    suspend fun addMaintenance(m: Maintenance) = maintenanceDao.insert(m)
    suspend fun updateMaintenance(m: Maintenance) = maintenanceDao.update(m)
    suspend fun deleteMaintenance(m: Maintenance) = maintenanceDao.delete(m)

    // Impôts
    fun getTaxes() = taxDao.getAll()
    suspend fun addTax(t: Tax) = taxDao.insert(t)
    suspend fun updateTax(t: Tax) = taxDao.update(t)
    suspend fun deleteTax(t: Tax) = taxDao.delete(t)

    // Assurance
    fun getInsurances() = insuranceDao.getAll()
    suspend fun addInsurance(i: Insurance) = insuranceDao.insert(i)
    suspend fun updateInsurance(i: Insurance) = insuranceDao.update(i)
    suspend fun deleteInsurance(i: Insurance) = insuranceDao.delete(i)

    // Visite technique
    fun getInspections() = inspectionDao.getAll()
    suspend fun addInspection(i: Inspection) = inspectionDao.insert(i)
    suspend fun updateInspection(i: Inspection) = inspectionDao.update(i)
    suspend fun deleteInspection(i: Inspection) = inspectionDao.delete(i)
}
