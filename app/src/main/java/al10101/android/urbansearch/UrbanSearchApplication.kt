package al10101.android.urbansearch

import android.app.Application

class UrbanSearchApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        LocationRepository.initialize(this)
    }

}