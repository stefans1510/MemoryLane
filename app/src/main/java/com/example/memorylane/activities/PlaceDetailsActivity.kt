package com.example.memorylane.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.memorylane.R
import com.example.memorylane.models.PlaceModel
import com.example.memorylane.viewmodels.PlaceViewModel

class PlaceDetailsActivity : AppCompatActivity() {
    private lateinit var ivPlaceImage: ImageView
    private lateinit var tvDescription: TextView
    private lateinit var tvLocation: TextView
    private lateinit var btnViewOnMap: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_details)

        val placeViewModel = PlaceViewModel(application)
        val placesList: ArrayList<PlaceModel> = placeViewModel.getPLacesList()
        val placeId = intent.getStringExtra("PLACE_ID")
        val placeModel = placesList.firstOrNull { it.id.toString() == placeId }

        val toolbar: Toolbar = findViewById(R.id.toolbar_place_details)
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

        ivPlaceImage = findViewById(R.id.iv_place_image)
        tvDescription = findViewById(R.id.tv_description)
        tvLocation = findViewById(R.id.tv_location)
        btnViewOnMap = findViewById(R.id.btn_view_on_map)

        if (placeModel != null) {
            ivPlaceImage.setImageURI(Uri.parse(placeModel.image))
            tvDescription.text = placeModel.description
            tvLocation.text = placeModel.location
        }

        btnViewOnMap.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("PLACE_ID", placeId)
            startActivity(intent)
        }
    }
}