package al10101.android.urbansearch.ui.map

import al10101.android.urbansearch.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import al10101.android.urbansearch.databinding.FragmentMapBinding
import al10101.android.urbansearch.ui.LoadingDialog
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import java.util.*

private const val TAG = "MapTag"

private const val REQUEST_USER_LOCATION_CODE = 99

open class MapFragment : Fragment(),
    OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener
{

    private lateinit var mapViewModel: MapViewModel
    private var _binding: FragmentMapBinding? = null

    private lateinit var loadingDialog: LoadingDialog
    private var dialogSet = false

    private lateinit var mMap: GoogleMap

    private lateinit var googleApiClient: GoogleApiClient
    private var googleApiClientSet = false

    private lateinit var locationRequest: LocationRequest
    private lateinit var lastLocation: Location
    private var currentUserLocationMarker: Marker? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.storeLocationButton.isEnabled = false
        binding.storeLocationButton.setOnClickListener {
            storeCurrentLocation()
        }

        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.show( getString(R.string.title_loading) )
        dialogSet = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = checkUserLocationPermission()
            Log.d(TAG, "onCreateView() -> Permission $permission")
        }

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            { locations ->
                locations?.let {
                    Log.i(TAG, "Got locations ${locations.size}")
                    updateMarkers(locations)
                }
            }
        )
    }

    private fun updateMarkers(locations: List<al10101.android.urbansearch.Location>) {

        locations.forEach { location ->

            val latLng = LatLng(location.latitude, location.longitude)
            val markerOptions = MarkerOptions().apply {
                position(latLng)
                title(location.date.toString())
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
            }
            mMap.addMarker(markerOptions)

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Manipulates the map once available
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        // PokemonGo style
        mMap.uiSettings.isCompassEnabled = false

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.style_json
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            buildGoogleApiClient()

            mMap.isMyLocationEnabled = true

        } else {
            Log.d(TAG, "onMapReady() -> Permissions not granted")
        }

    }

    private fun checkUserLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
           // If the permission is not granted, ask the user for the permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_USER_LOCATION_CODE
                )
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_USER_LOCATION_CODE
                )
            }
            return false
        } else {
            return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_USER_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (!googleApiClientSet) {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(context, "Permission Denied...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(requireContext())
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        googleApiClientSet = true
        Log.d(TAG, "GoogleApiClient defined")
        googleApiClient.connect()
    }

    override fun onConnected(p0: Bundle?) {

        // To request the current location of a moving object (in this case, the user) we need to
        // set a request constantly
        locationRequest = LocationRequest()
        locationRequest.interval = 1100 // milliseconds
        locationRequest.fastestInterval = 1100 // milliseconds
        locationRequest.smallestDisplacement = 5f // meters
        // PRIORITY_HIGH_ACCURACY or PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this
            )
            Log.d(TAG, "onConnected() -> requestLocationUpdates() just called")
        } else {
            Log.d(TAG, "onConnected() -> Permissions not granted")
        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        if (dialogSet) {
            dialogSet = false
            loadingDialog.hide()
            binding.storeLocationButton.isEnabled = true
        }

        // Retrieve latitude and longitude from current location
        lastLocation = location
        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)

        Log.d(TAG, "storeCurrentLocation() -> Lat: ${latLng.latitude}  Lng: ${latLng.longitude}")

        // Move the camera to the current location, PokemonGo style
        val cameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(18f) // The larger, the nearer
            .tilt(67.5f)
            .bearing(314f)
            .build()
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    private fun storeCurrentLocation() {

        val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)

        Log.d(TAG, "storeCurrentLocation() -> Lat: ${latLng.latitude}  Lng: ${latLng.longitude}")

        // Define the marker
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title("User Current Location")
            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        }

        // Reset the current marker, define as the new defined location. We do not move the camera this
        // time because it must be the same as the last current location
        currentUserLocationMarker?.remove()
        currentUserLocationMarker = mMap.addMarker(markerOptions)

        // Store location
        val location = al10101.android.urbansearch.Location(
            title = "Lat= ${latLng.latitude}  Lng= ${latLng.longitude}",
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
        mapViewModel.saveLocation(location)

        // Stop the location update
        if (googleApiClientSet) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }

    }

}