package al10101.android.urbansearch.ui.locations

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LocationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Not available yet"
    }
    val text: LiveData<String> = _text

}