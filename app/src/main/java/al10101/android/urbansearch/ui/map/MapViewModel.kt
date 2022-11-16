package al10101.android.urbansearch.ui.map

import al10101.android.urbansearch.Location
import al10101.android.urbansearch.LocationRepository
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {

    private val locationRepository = LocationRepository.get()
    val locationListLiveData = locationRepository.getLocations()

    fun saveLocation(location: Location) {
        locationRepository.addLocation(location)
    }

}