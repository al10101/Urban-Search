package al10101.android.urbansearch

import al10101.android.urbansearch.database.LocationDatabase
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "location-database"

class LocationRepository private constructor(context: Context) {

    private val database: LocationDatabase = Room.databaseBuilder(
        context.applicationContext,
        LocationDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val locationDao = database.locationDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getLocations(): LiveData<List<Location>> = locationDao.getLocations()

    fun getLocation(uuid: UUID): LiveData<Location?> = locationDao.getLocation(uuid)

    fun addLocation(location: Location) {
        executor.execute {
            locationDao.addLocation(location)
        }
    }

    companion object {

        private var INSTANCE: LocationRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = LocationRepository(context)
            }
        }

        fun get(): LocationRepository {
            return INSTANCE ?: throw  IllegalStateException("LocationRepository must be initialized")
        }

    }

}