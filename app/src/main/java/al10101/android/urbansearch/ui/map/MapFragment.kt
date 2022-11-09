package al10101.android.urbansearch.ui.map

import al10101.android.urbansearch.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import al10101.android.urbansearch.databinding.FragmentMapBinding
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

private const val MAP_TAG = "MapTag"

private const val REQUEST_USER_LOCATION_CODE = 99

open class MapFragment : Fragment(),
    OnMapReadyCallback,
    GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener
{

    private lateinit var mapViewModel: MapViewModel
    private var _binding: FragmentMapBinding? = null

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission = checkUserLocationPermission()
            Log.d(MAP_TAG, "onCreateView() -> Permission $permission")
        }

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.google_maps) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return root
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

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            buildGoogleApiClient()

            mMap.isMyLocationEnabled = true

        } else {
            Log.d(MAP_TAG, "onMapReady() -> Permissions not granted")
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
        Log.d(MAP_TAG, "GoogleApiClient defined")
        googleApiClient.connect()
    }

    override fun onConnected(p0: Bundle?) {

        // To request the current location of a moving object (in this case, the user) we need to
        // set a request constantly
        locationRequest = LocationRequest()
        locationRequest.interval = 1100 // milliseconds
        locationRequest.fastestInterval = 1100 // milliseconds
        // PRIORITY_HIGH_ACCURACY OR PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this
            )
            Log.d(MAP_TAG, "onConnected() -> requestLocationUpdates() just called")
        } else {
            Log.d(MAP_TAG, "onConnected() -> Permissions not granted")
        }

    }

    override fun onConnectionSuspended(p0: Int) {
        TODO("Not yet implemented")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {

        // Retrieve latitude and longitude from current location
        lastLocation = location
        val latLng = LatLng(location.latitude, location.longitude)

        Log.d(MAP_TAG, "Lat: ${latLng.latitude}  Lng: ${latLng.longitude}")

        // Define the marker
        val markerOptions = MarkerOptions().apply {
            position(latLng)
            title("User Current Location")
            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
        }

        // Reset the current marker, define as the new defined location and move the camera
        currentUserLocationMarker?.remove()
        currentUserLocationMarker = mMap.addMarker(markerOptions)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f)) // The larger, the further

        // Stop the location update
        if (googleApiClientSet) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this)
        }

    }

}