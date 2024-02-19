package com.example.memorylane.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.memorylane.databinding.ActivityPlaceDetailsBinding
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.viewmodels.PlaceViewModel

class PlaceDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val placeViewModel = PlaceViewModel(application)
        val placesList: ArrayList<PlaceModel> = placeViewModel.getPLacesList()
        val placeId = intent.getStringExtra("PLACE_ID")
        val placeModel = placesList.firstOrNull { it.id.toString() == placeId }

        val toolbar: Toolbar = binding.toolbarPlaceDetails
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            if (placeModel != null) {
                supportActionBar?.title = "${placeModel.title} Details"
            }
        }

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (placeModel != null) {
            binding.ivPlaceImage.setImageURI(Uri.parse(placeModel.image))
            binding.tvDescription.text = placeModel.description
            binding.tvLocation.text = placeModel.location
        }

        binding.btnViewOnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("PLACE_ID", placeId)
            startActivity(intent)
        }
    }
}
