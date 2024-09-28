package com.safe.resident.pro.app.fragments

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.safe.resident.pro.app.R
import com.safe.resident.pro.app.adapter.CustomSpinnerAdapter
import com.safe.resident.pro.app.data.Incident
import com.safe.resident.pro.app.databinding.FragmentAlertBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReportFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding : FragmentAlertBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap
    private var mapMarker: Marker? = null
    private var selectedIncident: String? = null
    private lateinit var database: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alert, container, false)
        val view = binding.root
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        database = FirebaseDatabase.getInstance().reference
        val categories = arrayOf(
            "Select Disturbance Type",
            "Person Assaulted",
            "Police Activity",
            "Accident",
            "Fire",
            "Armed Disturbance",
            "Missing People",
            "CIA's Leak"
        )
        val adapter = CustomSpinnerAdapter(requireContext(), categories)
        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle spinner item selection here
                selectedIncident = categories[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case when nothing is selected
            }
        }

        binding.submitBtn.setOnClickListener {
            if (!selectedIncident.equals("Select Disturbance Type")) {
                submitIncident(selectedIncident!!)
            } else {
                Toast.makeText(requireContext(), "Please select a disturbance type.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun submitIncident(selectedCategory: String) {
        // Get current location from mapMarker
        val currentLatLng = mapMarker?.position

        // Check if mapMarker is available and location is not null
        if (mapMarker != null && currentLatLng != null) {
            val sharedPrefs = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val userId = sharedPrefs.getString("email", "") ?: ""

            // Generate unique incident ID
            val incidentId = UUID.randomUUID().toString()
            // Create your incident object here with latLong, selectedCategory, and status = true
            val incident = Incident(
                incidentId,
                userId, // Replace with actual user ID
                selectedCategory,
                "${currentLatLng.latitude},${currentLatLng.longitude}",
                true,
                getCurrentDateTime()
            )
            database.child("incidents").child(incidentId).setValue(incident)
                .addOnSuccessListener {
                    // Handle success, maybe show a toast or navigate to another screen
                    Toast.makeText(requireContext(), "Incident submitted successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle failure
                    Toast.makeText(requireContext(), "Failed to submit incident. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Failed to get current location.", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        this.googleMap.uiSettings.isZoomControlsEnabled = true

        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val currentLatLng = LatLng(location.latitude, location.longitude)

                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
                googleMap.setOnCameraMoveListener {
                    // Check if mapMarker is not null before using it
                    mapMarker?.position = googleMap.cameraPosition.target
                }
                // Initialize mapMarker if it's null
                if (mapMarker == null) {
                    mapMarker = googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Current Location"))
                }

                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            )
        } else {
            // Handle case where permission is not granted
        }

        googleMap.setOnCameraMoveListener {
            mapMarker?.position = googleMap.cameraPosition.target
        }
    }
}