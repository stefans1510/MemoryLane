package com.example.memorylane.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memorylane.R
import com.example.memorylane.databinding.ActivityMapBinding
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.viewmodels.PlaceViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var placeViewModel: PlaceViewModel
    private lateinit var binding: ActivityMapBinding
    private lateinit var placesList: ArrayList<PlaceModel>
    private var placeId: String? = null
    private var placeModel: PlaceModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placeViewModel = PlaceViewModel(application)
        placesList = placeViewModel.getPLacesList()
        placeId = intent.getStringExtra("PLACE_ID")
        placeModel = placesList.firstOrNull { it.id.toString() == placeId }

        val toolbar: Toolbar = binding.toolbarMap
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            if (placeModel != null) {
                supportActionBar?.title = "View ${placeModel?.title} on Map"
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val supportMapFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        placeModel?.let {
            val position = LatLng(it.latitude, it.longitude)
            googleMap.addMarker(MarkerOptions().position(position).title(it.location))
            val latLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
            googleMap.animateCamera(latLngZoom)
        }
    }
}
