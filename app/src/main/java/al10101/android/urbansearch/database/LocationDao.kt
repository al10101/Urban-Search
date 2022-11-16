package al10101.android.urbansearch.database

import al10101.android.urbansearch.Location
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*

@Dao
interface LocationDao {

     @Query("SELECT * FROM location")
     fun getLocations(): LiveData<List<Location>>

     @Query("SELECT * FROM location WHERE uuid=(:uuid)")
     fun getLocation(uuid: UUID): LiveData<Location?>

     @Insert
     fun addLocation(location: Location)

}