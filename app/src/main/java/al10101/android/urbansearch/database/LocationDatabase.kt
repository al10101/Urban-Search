package al10101.android.urbansearch.database

import al10101.android.urbansearch.Location
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ Location::class ], version=1)
@TypeConverters(LocationTypeConverters::class)
abstract class LocationDatabase: RoomDatabase() {

    abstract fun locationDao(): LocationDao

}