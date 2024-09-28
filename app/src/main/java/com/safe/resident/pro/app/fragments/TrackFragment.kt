package com.safe.resident.pro.app.fragments

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.safe.resident.pro.app.AccountActivity
import com.safe.resident.pro.app.MainActivity
import com.safe.resident.pro.app.R
import com.safe.resident.pro.app.data.Incident
import com.safe.resident.pro.app.databinding.FragmentTrackBinding

class TrackFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentTrackBinding
    private lateinit var googleMap: GoogleMap
    private var currentLocation: Location? = null
    private val radiusMeters = 1207.01 // 1.5 Miles area
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var database: DatabaseReference
    private val incidentsList = mutableListOf<Incident>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_track, container, false)
        val view = binding.root
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        database = FirebaseDatabase.getInstance().reference
        fetchIncidents()
        binding.ivUser.setOnClickListener {
            startActivity(Intent( requireActivity(), AccountActivity::class.java))
        }
        return view
    }
    private fun fetchIncidents() {
        database.child("incidents").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                incidentsList.clear()
                for (incidentSnapshot in snapshot.children) {
                    val incident = incidentSnapshot.getValue(Incident::class.java)
                    incident?.let {
                        incidentsList.add(it)
                    }
                }
                displayIncidentsOnMap()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
    private fun displayIncidentsOnMap() {
        for (incident in incidentsList) {
            incident.latLong?.split(",")?.let { latLong ->
                if (latLong.size == 2) {
                    val latitude = latLong[0].toDouble()
                    val longitude = latLong[1].toDouble()
                    val location = LatLng(latitude, longitude)
                    addCustomMarker(incident.incidentName, location)
                }
            }
        }
    }

    private fun addCustomMarker(incidentName: String?, location: LatLng) {
        if (isAdded) {
            val markerOptions = MarkerOptions().position(location)
            val drawableId = when (incidentName) {
                "Police Activity" -> R.drawable.police
                "Robbery" -> R.drawable.thief
                "Accident" -> R.drawable.accident
                "Fire" -> R.drawable.fire
                "Fighting" -> R.drawable.fight
                else -> R.drawable.mark_alert // Default drawable if incidentName doesn't match
            }
            val markerDrawable = ContextCompat.getDrawable(requireContext(), drawableId)
            markerDrawable?.let {
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(resources, drawableId))
                markerOptions.icon(bitmapDescriptor)
            }
            val marker = googleMap.addMarker(markerOptions)
            markerList.add(marker!!) // Keep track of added markers
        }
    }
    private val markerList = mutableListOf<Marker>()

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.apply {
                isMyLocationEnabled = true
                uiSettings.isMyLocationButtonEnabled = true
                setOnMyLocationChangeListener { location ->
                    currentLocation = location
                    updateMapLocation()
                }
            }
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            showExplanationDialog()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Needed")
            .setMessage("This app needs the location permission to provide location-based services. Please allow access to continue.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                requestLocationPermission()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap
        enableMyLocation()
        displayIncidentsOnMap()
    }

    private fun updateMapLocation() {
        currentLocation?.let {
            val currentLatLng = LatLng(it.latitude, it.longitude)
            googleMap.clear()
            // Add custom markers again after clearing the map
            displayIncidentsOnMap()

            googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))

            val circleOptions = CircleOptions()
                .center(currentLatLng)
                .radius(radiusMeters.toDouble())
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(70, 0, 0, 255))
            googleMap.addCircle(circleOptions)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            } else {
                showDenialDialog()
            }
        }
    }

    private fun showDenialDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Denied")
            .setMessage("Location permission is essential for this app to function. Please consider granting it in app settings.")
            .setPositiveButton("Settings") { dialog, _ ->
                // Open app settings here if desired
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
